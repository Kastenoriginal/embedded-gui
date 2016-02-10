package core;

import hashmaps.RaspberryHashMap;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sun.net.util.IPAddressUtil;

@SuppressWarnings("restriction")
public class Main extends Application implements EventHandler<ActionEvent> {

	private Button echoButton, requestButton;
	private GridPane gridPane;
	
	private static final int PORT = 18924;
	private String ip = "192.168.168.2";
	private Networking networking = new Networking();

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage primaryStage) throws Exception {
		BorderPane borderPane = new BorderPane();
		TextField textField = new TextField();;

		setIpAddressTextField(textField);
		setButtons();
		setLayout(borderPane, textField);
		
		
		Scene scene = new Scene(borderPane, 600, 800);
		scene.getStylesheets().add(getClass().getClassLoader().getResource("Custom style.css").toExternalForm());
		borderPane.requestFocus();
		primaryStage.setTitle("Embedded systems control");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}
	
	private void setIpAddressTextField(final TextField textField) {
		textField.setPromptText("Default IP address");
		setValidationBorder(getIp(), textField);
		textField.textProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (textField.getPromptText().startsWith("D")) {
					textField.setPromptText("Enter IP address");
				}
				setIp(newValue);
				setValidationBorder(getIp(), textField);
			}
		});
	}

	public void handle(ActionEvent event) {
		Networking networking = new Networking();
		if (event.getSource() == echoButton && isIpAddress(getIp())) {
			networking.sendEcho(getIp(), PORT);
		} else if (event.getSource() == requestButton && isIpAddress(getIp())) {
			//TODO ked vrati vsetky hodnoty zo servera tak ohandlovat GUI
			System.out.println(networking.sendStatusRequest(getIp(), PORT));
		}
	}

	private void setLayout(BorderPane borderPane, TextField textField) {
		HBox topBox = new HBox();
		topBox.setAlignment(Pos.CENTER);
		topBox.getChildren().add(textField);
//		topBox.setStyle("-fx-background-color: blue;");
		borderPane.setTop(topBox);

		VBox leftBox = new VBox();
		leftBox.setAlignment(Pos.TOP_LEFT);
		leftBox.getChildren().add(echoButton);
		leftBox.getChildren().add(requestButton);
//		leftBox.setStyle("-fx-background-color: red;");
		borderPane.setLeft(leftBox);

		gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);

		setGridElements();

		VBox centerBox = new VBox();
		centerBox.setAlignment(Pos.TOP_CENTER);
		centerBox.getChildren().add(gridPane);
//		centerBox.setStyle("-fx-background-color: green;");
		borderPane.setCenter(centerBox);
	}

	private void setGridElements() {
		ArrayList<Button> buttons = new ArrayList<Button>();
		ArrayList<ComboBox<String>> comboBoxes = new ArrayList<ComboBox<String>>();

		createButtonsAndComboBoxes(buttons, comboBoxes);
		disableButtonsAndComboBoxes(buttons, comboBoxes);
	}

	private void createButtonsAndComboBoxes(ArrayList<Button> buttons, final ArrayList<ComboBox<String>> comboBoxes) {
		int row = 1;
		int col = 1;
		int buttonId = 1;
		int comboBoxId = 1;

		RaspberryHashMap piMap = new RaspberryHashMap();
		piMap.createHashMap();

		for (int i = 1; i <= 80; i++) {
			if (col == 5) {
				col = 1;
				row++;
			}

			String[] pinTypes = piMap.getValueByKey(comboBoxId);
			ObservableList<String> options;
			if (pinTypes.length > 1) {
				options = FXCollections.observableArrayList(pinTypes[0], pinTypes[1]);
			} else {
				options = FXCollections.observableArrayList(pinTypes[0]);
			}

			final ComboBox<String> comboBox;
			final Button button;

			if (col == 1 || col == 4) {
				comboBox = new ComboBox<String>(options);
				comboBox.setId(String.valueOf(comboBoxId));
				comboBox.setPrefWidth(110);
				comboBox.getSelectionModel().selectFirst();
				comboBoxes.add(comboBox);
				comboBoxId++;
				gridPane.add(comboBox, col, row);
			} else {
				button = new Button();
				button.setId(String.valueOf(buttonId));
				button.setUserData("0");
				button.setStyle("-fx-font-size: 12");
				button.setMinSize(35, 35);
				button.setText(String.valueOf(buttonId));
				buttons.add(button);
				buttonId++;
				button.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent arg0) {
						toggleButton(button, comboBoxes.get(Integer.valueOf(button.getId())-1));
					}
				});
				gridPane.add(button, col, row);
			}
			col++;
		}
	}

	private void disableButtonsAndComboBoxes(ArrayList<Button> buttons, ArrayList<ComboBox<String>> comboBoxes) {
		for (int i = 0; i < 40; i++) {
			if (comboBoxes.get(i).getSelectionModel().getSelectedItem().equals("PWR5")
					|| comboBoxes.get(i).getSelectionModel().getSelectedItem().equals("PWR3")
					|| comboBoxes.get(i).getSelectionModel().getSelectedItem().equals("GND")) {
				comboBoxes.get(i).setDisable(true);
				buttons.get(i).setDisable(true);
			}
		}
	}

	private void toggleButton(Button button, ComboBox<String> comboBox) {
		String valueToSend;
		if (!isPressed(button)) {
			valueToSend = "1";
			setPressed(button, "1");
		} else {
			valueToSend = "0";
			setPressed(button, "0");
		}
		networking.toggleLed(button, comboBox, valueToSend, getIp(), PORT);
	}

	private void setButtons() {
		echoButton = new Button("Send echo");
		echoButton.setOnAction(this);
		requestButton = new Button("Request status");
		requestButton.setOnAction(this);
	}

	private void setValidationBorder(String IpAddress, TextField textField) {
		if (!isIpAddress(IpAddress)) {
			textField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
		} else {
			textField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
		}

	}

	private boolean isIpAddress(String ip) {
		try {
			if (ip == null || ip.isEmpty() || !IPAddressUtil.isIPv4LiteralAddress(ip) || ip.endsWith(".")) {
				return false;
			}

			String[] parts = ip.split("\\.");

			if (parts.length != 4) {
				return false;
			}

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}

			return true;
		} catch (NumberFormatException e) {
			System.out.println(e);
			return false;
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isPressed(Button button) {
		if (button.getUserData() == "0") {
			return false;
		} else {
			return true;
		}
	}

	public void setPressed(Button button, String value) {
		button.setUserData(value);
	}
}
