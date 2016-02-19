package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

@SuppressWarnings("restriction")
public class Networking implements Callable<String> {

	private static boolean connected = false;
	private static Socket socket;
	private static PrintWriter out;
	private static BufferedReader in;
	private int serverPort;
	private String command = "";
	private String serverIP;
	private Callable<String> callable;

	public Networking() {
	}

	public Networking(String command) {
		this.command = command;
	}

	public Networking(String serverIP, int serverPort) {
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}

	public Networking(String command, String serverIP, int serverPort) {
		this.command = command;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}

	public void toggleConnectionStatus(final String serverIP, final int serverPort, final String connectionCommand,
			final Button button) {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		callable = new Networking(connectionCommand, serverIP, serverPort);
		Future<String> future = executor.submit(callable);
		try {
			//TODO handle future.get() if needed in future
			System.out.println(future.get());
		} catch (InterruptedException e) {
			System.out.println(e);
		} catch (ExecutionException e) {
			System.out.println(e);
		}
		executor.shutdown();
	}

	public void sendStatusRequest(final String serverIP, final int serverPort) {
		new Thread(new Runnable() {
			public void run() {
				if (isConnected()) {
					String command = "request";
					ExecutorService executor = Executors.newFixedThreadPool(3);
					callable = new Networking(command);
					Future<String> future = executor.submit(callable);
					try {
						//TODO handle status request
						System.out.println(future.get());
					} catch (InterruptedException e) {
						System.out.println(e);
					} catch (ExecutionException e) {
						System.out.println(e);
					}
					executor.shutdown();
				}
			}
		}).start();
	}
	
	public void receiveAllPinStatus(){
		new Thread(new Runnable() {
			public void run() {
				try {
					String allPinStatus = in.readLine();
					System.out.println(allPinStatus);
					if (allPinStatus.startsWith("START;") && allPinStatus.endsWith("END")) {
						String[] partialStatus = allPinStatus.split(";");
						for (int i = 0; i < partialStatus.length; i++) {
							Parser parser = new Parser(allPinStatus);
							System.out.println(allPinStatus);
							System.out.println(partialStatus[i]);
							//TODO getblabla
						}
						
						
						
						//TODO ohandlovat to co prislo
						//TODO dokoncit parser
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public String call() throws Exception {
		String response;
		if (isConnected() && command.equals("request")) {
			command = "";
			out.println(getDateAndTime() + "REQUEST:990");
			response = in.readLine();
			return response;
		} else if (!isConnected() && command.equals("Connect")) {
			socket = new Socket(serverIP, serverPort);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println(command);
			command = "";
			response = in.readLine();
			if (response.equals("connected to server.")) {
				setConnected(true);
			}
			return response;
		} else if (isConnected() && command.equals("Disconnect")) {
			out.println(command);
			command = "";
			response = in.readLine();
			if (response.equals("disconnected from server.")) {
				socket.close();
				setConnected(false);
			}
			return response;
		} else {
			return "No response from server.";
		}
	}

	public void togglePin(final Button button, final ComboBox<String> pinTypeComboBox, final TextField address,
			final String valueToSend, final String serverIP, final int serverPort, final String i2cMessage) {
		if (isConnected()) {
			new Thread(new Runnable() {
				public void run() {
					try {
						if (pinTypeComboBox.getSelectionModel().getSelectedItem().equals("I2C")) {
							if (Integer.valueOf(button.getText().trim()) < 10) {
								System.out.println("Sending: " + getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":0"
										+ button.getText().trim() + address.getText().trim() + i2cMessage);
								out.println(getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":0"
										+ button.getText().trim() + address.getText().trim() + i2cMessage);
							} else {
								System.out.println("Sending: " + getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":"
										+ button.getText().trim() + address.getText().trim() + i2cMessage);
								out.println(getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":"
										+ button.getText().trim() + address.getText().trim() + i2cMessage);
							}
						} else {
							if (Integer.valueOf(button.getText().trim()) < 10) {
								System.out.println("Sending: " + getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":0"
										+ button.getText().trim() + valueToSend);
								out.println(getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":0"
										+ button.getText().trim() + valueToSend);
							} else {
								System.out.println("Sending: " + getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":"
										+ button.getText().trim() + valueToSend);
								out.println(getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":"
										+ button.getText().trim() + valueToSend);
							}
						}
						String response = in.readLine();
						//TODO ohandlovat parsnuty string ako response z pinu
						System.out.println(response);
//						socket.close();
					} catch (UnknownHostException e) {
						System.out.println(e);
					} catch (IOException e) {
						System.out.println(e);
					}
				}
			}).start();
		}
	}

	private String getDateAndTime() {
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}
	
	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		Networking.connected = connected;
	}
}
