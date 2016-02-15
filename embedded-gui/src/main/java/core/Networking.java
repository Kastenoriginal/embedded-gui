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

	private boolean connected = false;
	Socket socket;
	PrintWriter out;
	BufferedReader in;

	public Networking() {
	}

	public Networking(String ipAddress, int port) {
	}

	public void toggleConnectionStatus(final String serverIP, final int serverPort, final String connectionCommand,
			final Button button) {
		new Thread(new Runnable() {
			public void run() {
				try {
					if (!isConnected() && connectionCommand.equals("Connect")) {
						socket = new Socket(serverIP, serverPort);
						socket.setSoTimeout(500);
						out = new PrintWriter(socket.getOutputStream(), true);
						out.println(connectionCommand);
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						if (in.readLine().equals("connected to server.")) {
							setConnected(true);
							button.setText("Disconnect");
							button.setDisable(false);
						}
					} else if (isConnected() && connectionCommand.equals("Disconnect")) {
						out = new PrintWriter(socket.getOutputStream(), true);
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						out.println(connectionCommand);
						System.out.println("Connection status: " + in.readLine());
						if (in.readLine().equals("disconnected from server.")) {
							socket.close();
							setConnected(false);
							button.setText("Connect");
							button.setDisable(false);
						}
					}
				} catch (UnknownHostException e) {
					System.out.println(e);
					button.setDisable(false);
				} catch (IOException e) {
					System.out.println(e);
					button.setDisable(false);
				}
			}
		}).start();
	}

	public void sendStatusRequest(final String serverIP, final int serverPort) {
		if (isConnected()) {
			ExecutorService executor = Executors.newFixedThreadPool(3);
			Callable<String> callable = new Networking();
			Future<String> future = executor.submit(callable);
			try {
				System.out.println(future.get());
			} catch (InterruptedException e) {
				System.out.println(e);
			} catch (ExecutionException e) {
				System.out.println(e);
			}
			executor.shutdown();
		}
	}

	public String call() throws Exception {
//		Socket socket = new Socket(ipAddress, port);
//		socket.setSoTimeout(5000);
//		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out.println(getDateAndTime() + "REQUEST:990");
		String response = in.readLine();
		socket.close();
		return response;
	}

	public void togglePin(final Button button, final ComboBox<String> pinTypeComboBox, final TextField address,
			final String valueToSend, final String serverIP, final int serverPort) {
		if (isConnected()) {
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

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
