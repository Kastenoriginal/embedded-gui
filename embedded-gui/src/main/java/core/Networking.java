package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

	public static boolean connected = false;
	public static ResponseParser responseParser;

	private static ArrayList<String> pinsToSend;
	private static int count = 0;
	private static Socket socket;
	private static PrintWriter out;
	private static BufferedReader in;
	private int serverPort;
	private String command = "";
	private String serverIP;
	private Callable<String> callable;
	private static Thread statusThread;

	public Networking() {
	}

	public Networking(String command) {
		this.command = command;
	}

	public Networking(String command, String serverIP, int serverPort) {
		this.command = command;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		Networking.pinsToSend = new ArrayList<String>();
	}

	public void toggleConnectionStatus(final String serverIP, final int serverPort, final String connectionCommand) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		callable = new Networking(connectionCommand, serverIP, serverPort);
		Future<String> future = executor.submit(callable);
		try {
			//TODO handle future.get() if needed in future
			System.out.println(future.get());
			if (connected) {
				sendStatusRequest(serverIP, serverPort);
			}
		} catch (InterruptedException e) {
			System.out.println(e);
		} catch (ExecutionException e) {
			System.out.println(e);
		}
		executor.shutdown();
	}

	private void sendStatusRequest(final String serverIP, final int serverPort) {
		if (statusThread == null || statusThread.getName().equals("0")) {
			statusThread = new Thread(new Runnable() {
				public void run() {
					ExecutorService executor = Executors.newFixedThreadPool(2);
					while (connected) {
						callable = new Networking("request");
						Future<String> future = executor.submit(callable);
						try {
							String allPinStatus = future.get();
							if (allPinStatus == null) {
								count++;
								System.out.println("Unable to receive response from server for " + count
										+ " second(s).");
								if (count > 4) {
									disconnect();
								}
							} else {
								count = 0;
								parseAllPinStatus(allPinStatus);
							}
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							System.out.println(e);
						} catch (ExecutionException e) {
							System.out.println(e);
						}
					}
					executor.shutdown();
				}
			});
			if (!statusThread.isAlive()) {
				statusThread.setName("1");
				statusThread.start();
			}
		}
	}

	public void parseAllPinStatus(final String allPinStatus) {
		new Thread(new Runnable() {
			public void run() {
				if (allPinStatus != null && allPinStatus.startsWith("START;") && allPinStatus.endsWith("END")) {
					System.out.println("Received " + allPinStatus);
					String[] partialStatus = allPinStatus.split(";");
					for (String part : partialStatus) {
						if (part.length() > 10) {
							ResponseParser parser = new ResponseParser(part);
							responseParser = parser;
							System.out.println("Received status from Raspberry at: " + parser.getDay() + "."
									+ parser.getMonth() + "." + parser.getYear() + " " + parser.getHour() + ":"
									+ parser.getMinute() + ":" + parser.getSecond() + " with pin type "
									+ parser.getPinType() + " as " + parser.getIo() + " and value " + parser.getValue()
									+ " on pin " + parser.getPinNumber());
//							Main.connectButton.setText(parser.getPinType());
						}
					}
					//TODO ohandlovat to co prislo - update GUI
				}
			}
		}).start();
	}

	public String call() {
		try {
			String response;
			if (connected && command.equals("request")) {
				command = "";
				ArrayList<String> pinsToSend = Networking.pinsToSend;
				String pinsToRequest = "";
				if (!pinsToSend.isEmpty() || pinsToSend != null) {
					for (String pinToSend : pinsToSend) {
						pinsToRequest = pinsToRequest + ";" + pinToSend;
					}
				}
				System.out.println("SENDING " + pinsToRequest);
				out.println(getDateAndTime() + "REQUEST:990" + pinsToRequest);
				System.out.println("reading response");
				response = in.readLine();
				return response;
			} else if (!connected && command.equals("Connect")) {
				socket = new Socket(serverIP, serverPort);
				socket.setSoTimeout(5000);
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out.println(command);
				command = "";
				response = in.readLine();
				if (response.equals("connected to server.")) {
					connected = true;
				}
				return response;
			} else if (connected && command.equals("Disconnect")) {
				out.println(command);
				command = "";
				response = in.readLine();
				if (response.equals("disconnected from server.")) {
					disconnect();
				}
				return response;
			} else {
				return "No response from server.";
			}
		} catch (IOException e) {
			disconnect();
		}
		return "No response from server.";
	}

	public void togglePin(final Button button, final ComboBox<String> pinTypeComboBox, final TextField address,
			final String valueToSend, final String serverIP, final int serverPort, final String i2cMessage) {
		if (connected) {
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
						//TODO ohandlovat parsnuty string ako response z pinu - update GUI - to iste urobit po prijati pinov zo sekundovych rq
						System.out.println(response);
					} catch (UnknownHostException e) {
						toggleConnectionStatus(serverIP, serverPort, "Disconnect");
					} catch (IOException e) {
						toggleConnectionStatus(serverIP, serverPort, "Disconnect");
					}
				}
			}).start();
		}
	}

	public void addPinToSend(String pinId) {
		pinsToSend.add(pinId);
	}

	public void removePinToSend(String pinId) {
		pinsToSend.remove(pinId);
	}

	private String getDateAndTime() {
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}

	public void disconnect() {
		System.out.println("Trying to disconnect from server.");
		if (!socket.isClosed()) {
			try {
				socket.close();
				System.out.println("Connection succesfully closed.");
			} catch (IOException e) {
				System.out.println("Unable to close socket.");
			}
		} else {
			System.out.println("Connection is already closed.");
		}
		connected = false;
		statusThread.setName("0");
	}
}
