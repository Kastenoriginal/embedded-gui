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

	public void toggleLed(final Button button, final String serverIP, final int serverPort) {
		new Thread(new Runnable() {
			public void run() {
				Socket socket;
				try {
					socket = new Socket(serverIP, serverPort);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					//TODO co sa posle + test
//					out.println(getDateAndTime() + "GPIO" + button.getText() + "TRUE/HIGH");
					socket.close();
				} catch (UnknownHostException e) {
					System.out.println(e);
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}).start();
	}

	//TODO remove
	public void testGpio11(final String serverIP, final int serverPort) {
		new Thread(new Runnable() {
			public void run() {
				Socket socket;
				try {
					socket = new Socket(serverIP, serverPort);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out.println(getDateAndTime() + "GPIO:111");
					System.out.println(in.readLine());
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
		DateFormat dateFormat = new SimpleDateFormat("dMMyyyyHHmmss");
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}
}
