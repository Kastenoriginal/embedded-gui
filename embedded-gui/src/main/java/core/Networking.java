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

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

@SuppressWarnings("restriction")
public class Networking {

	public void sendEcho(final String serverIP, final int serverPort) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Socket socket = new Socket(serverIP, serverPort);
					socket.setSoTimeout(5000);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out.println("TOTO POSIELAM DO RPI");
					System.out.println("CO PRISLO: " + in.readLine());
					socket.close();
				} catch (UnknownHostException e) {
					System.out.println(e);
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}).start();
	}

	public String sendStatusRequest(final String serverIP, final int serverPort) {
		//TODO google: java thread return value
		final String response = null;
		new Thread(new Runnable() {
			public void run() {
				Socket socket;
				try {
					socket = new Socket(serverIP, serverPort);
					socket.setSoTimeout(5000);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out.println(getDateAndTime() + "REQUEST:990");
					//TODO ohandlovat spatnu vazbu
					System.out.println(in.readLine());
					socket.close();
				} catch (UnknownHostException e) {
					System.out.println(e);
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}).start();
		return response;
	}

	public void toggleLed(final Button button, final ComboBox<String> comboBox, final String valueToSend,
			final String serverIP, final int serverPort) {
		new Thread(new Runnable() {
			public void run() {
				Socket socket;
				try {
					socket = new Socket(serverIP, serverPort);
					socket.setSoTimeout(5000);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					if (Integer.valueOf(button.getText().trim()) < 10) {
						System.out.println("Sending: " + getDateAndTime()
								+ comboBox.getSelectionModel().getSelectedItem().toString() + ":" + "0"
								+ button.getText().trim() + valueToSend);
						out.println(getDateAndTime() + comboBox.getSelectionModel().getSelectedItem().toString() + ":"
								+ "0" + button.getText().trim() + valueToSend);
					} else {
						System.out.println("Sending: " + getDateAndTime()
								+ comboBox.getSelectionModel().getSelectedItem().toString() + ":"
								+ button.getText().trim() + valueToSend);
						out.println(getDateAndTime() + comboBox.getSelectionModel().getSelectedItem().toString() + ":"
								+ button.getText().trim() + valueToSend);
					}
					String response = in.readLine();
					//TODO ohandlovat parsnuty string
					System.out.println(response);
					socket.close();
				} catch (UnknownHostException e) {
					System.out.println(e);
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}).start();
	}

	private String getDateAndTime() {
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}
}
