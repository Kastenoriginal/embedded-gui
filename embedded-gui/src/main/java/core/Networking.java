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
	private String command = "";
	private String serverIP;
	private int serverPort;
	private String connectionButtonCommand;
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
			String response = future.get();
			setConnectionButtonCommand(response);
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
				if (connected) {
					String command = "request";
					ExecutorService executor = Executors.newFixedThreadPool(3);
					callable = new Networking(command);
					Future<String> future = executor.submit(callable);
					try {
						//TODO handle
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

	public String call() throws Exception {
		String response;
		if (connected && command.equals("request")) {
			command = "";
			out.println(getDateAndTime() + "REQUEST:990");
			response = in.readLine();
			return response;
		} else if (!connected && command.equals("Connect")) {
			socket = new Socket(serverIP, serverPort);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println(command);
			command = "";
			response = in.readLine();
			if (response.equals("connected to server.")) {
				connected = true;
				response = response + "&Disconnect";
			}
			return response;
		} else if (connected && command.equals("Disconnect")) {
			out.println(command);
			command = "";
			response = in.readLine();
			if (response.equals("disconnected from server.")) {
				socket.close();
				connected = false;
				response = response + "&Connect";
			}
			return response;
		} else {
			return "NUUUUULLLLLLLL";
		}
	}

	public void togglePin(final Button button, final ComboBox<String> pinTypeComboBox, final TextField address,
			final String valueToSend, final String serverIP, final int serverPort) {
		if (connected) {
			new Thread(new Runnable() {
				public void run() {
//					Socket socket;
					try {
//						socket = new Socket(serverIP, serverPort);
//						socket.setSoTimeout(500);
//						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						if (pinTypeComboBox.getSelectionModel().getSelectedItem().equals("I2C")) {
							if (Integer.valueOf(button.getText().trim()) < 10) {
								System.out.println("Sending: " + getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":0"
										+ button.getText().trim() + address.getText().trim() + valueToSend);
								out.println(getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":0"
										+ button.getText().trim() + address.getText().trim() + valueToSend);
							} else {
								System.out.println("Sending: " + getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":"
										+ button.getText().trim() + address.getText().trim() + valueToSend);
								out.println(getDateAndTime()
										+ pinTypeComboBox.getSelectionModel().getSelectedItem().toString() + ":"
										+ button.getText().trim() + address.getText().trim() + valueToSend);
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
						//TODO ohandlovat parsnuty string
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

	public String getConnectionButtonCommand() {
		return connectionButtonCommand;
	}

	public void setConnectionButtonCommand(String connectionButtonCommand) {
		this.connectionButtonCommand = connectionButtonCommand;
	}
}
