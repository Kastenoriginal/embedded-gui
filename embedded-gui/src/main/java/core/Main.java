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
		TextField textField = new TextField();
		;

		setIpAddressTextField(textField);
		setButtons();
		setLayout(borderPane, textField);

		Scene scene = new Scene(borderPane, 800, 900);
		scene.getStylesheets().add(getClass().getClassLoader().getResource("Custom style.css").toExternalForm());
		borderPane.requestFocus();
		primaryStage.setTitle("Embedded systems control");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	private void setIpAddressTextField(final TextField textField) {
		textField.setPromptText("Default IP address");
		setIpValidationBorder(getIp(), textField);
		textField.textProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (textField.getPromptText().startsWith("D")) {
					textField.setPromptText("Enter IP address");
				}
				setIp(newValue);
				setIpValidationBorder(getIp(), textField);
			}
		});
	}

	public void handle(ActionEvent event) {
		Networking networking = new Networking();
		if (event.getSource() == echoButton && isIpAddress(getIp())) {
			networking.sendEcho(getIp(), PORT);
		} else if (event.getSource() == requestButton && isIpAddress(getIp())) {
			//TODO ked vrati vsetky hodnoty zo servera tak ohandlovat GUI
			networking.sendStatusRequest(getIp(), PORT);
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
		ArrayList<ComboBox<String>> pinTypeComboBoxes = new ArrayList<ComboBox<String>>();
		ArrayList<TextField> textFields = new ArrayList<TextField>();
		ArrayList<ComboBox<String>> inputOutputComboBoxes = new ArrayList<ComboBox<String>>();

		createGridElements(buttons, pinTypeComboBoxes, inputOutputComboBoxes, textFields);
		disableGridElements(buttons, pinTypeComboBoxes, inputOutputComboBoxes, textFields);
	}

	private void createGridElements(final ArrayList<Button> buttons,
			final ArrayList<ComboBox<String>> pinTypeComboBoxes,
			final ArrayList<ComboBox<String>> inputOutputComboBoxes, final ArrayList<TextField> textFields) {
		int row = 1;
		int col = 1;
		int buttonId = 1;
		int pinTypeComboBoxId = 1;
		int inputOutputComboBoxId = 1;
		int textFieldId = 1;

		RaspberryHashMap piMap = new RaspberryHashMap();
		piMap.createHashMap();

		for (int i = 1; i <= 160; i++) {
			if (col == 9) {
				col = 1;
				row++;
			}
			if (pinTypeComboBoxId > 40) {
				pinTypeComboBoxId = 40;
			}
			String[] pinTypes = piMap.getValueByKey(pinTypeComboBoxId);
			ObservableList<String> pinTypeOtions;
			if (pinTypes.length > 1) {
				pinTypeOtions = FXCollections.observableArrayList(pinTypes[0], pinTypes[1]);
			} else {
				pinTypeOtions = FXCollections.observableArrayList(pinTypes[0]);
			}

			final ComboBox<String> pinTypeComboBox;
			final ComboBox<String> inputOutputComboBox;
			final Button button;
			final TextField textField;

			if (col == 3 || col == 6) {
				pinTypeComboBox = new ComboBox<String>(pinTypeOtions);
				pinTypeComboBox.setId(String.valueOf(pinTypeComboBoxId));
				pinTypeComboBox.setPrefWidth(110);
				pinTypeComboBox.getSelectionModel().selectFirst();
				pinTypeComboBoxes.add(pinTypeComboBox);
				pinTypeComboBoxId++;
				pinTypeComboBox.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent arg0) {
						setElementsVisibility(pinTypeComboBox,
								inputOutputComboBoxes.get(Integer.valueOf(pinTypeComboBox.getId()) - 1),
								textFields.get(Integer.valueOf(pinTypeComboBox.getId()) - 1),
								buttons.get(Integer.valueOf(pinTypeComboBox.getId()) - 1));
					}
				});
				gridPane.add(pinTypeComboBox, col, row);
			} else if (col == 1 || col == 8) {
				textField = new TextField();
				textField.setId(String.valueOf(textFieldId));
				textField.setPrefWidth(80);
				textField.setVisible(false);
				textField.setPromptText("Address");
				setAddressValidationBorder("", textField);
				textFields.add(textField);
				textFieldId++;
				textField.textProperty().addListener(new ChangeListener<String>() {
					public void changed(ObservableValue<? extends String> observableValue, String oldValue,
							String newValue) {
						setAddressValidationBorder(newValue, textField);
					}
				});
				//TODO zistit ci adresa bavi
				gridPane.add(textField, col, row);
			} else if (col == 2 || col == 7) {
				ObservableList<String> inputOutputOtions = FXCollections.observableArrayList("OUT", "IN");
				inputOutputComboBox = new ComboBox<String>(inputOutputOtions);
				inputOutputComboBox.setId(String.valueOf(inputOutputComboBoxId));
				inputOutputComboBox.setPrefWidth(80);
				inputOutputComboBox.getSelectionModel().selectFirst();
				inputOutputComboBoxes.add(inputOutputComboBox);
				inputOutputComboBoxId++;
				inputOutputComboBox.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent arg0) {
						if (inputOutputComboBox.getSelectionModel().getSelectedItem().equals("IN")) {
							buttons.get(Integer.valueOf(inputOutputComboBox.getId()) - 1).setDisable(true);
						} else {
							buttons.get(Integer.valueOf(inputOutputComboBox.getId()) - 1).setDisable(false);
						}
					}
				});
				gridPane.add(inputOutputComboBox, col, row);
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
						if (isIpAddress(getIp())
								&& ((inputOutputComboBoxes.get(Integer.valueOf(button.getId()) - 1).getSelectionModel()
										.getSelectedItem().equals("OUT")) || (pinTypeComboBoxes
										.get(Integer.valueOf(button.getId()) - 1).getSelectionModel().getSelectedItem()
										.equals("I2C") && isValidInput(textFields.get(
										Integer.valueOf(button.getId()) - 1).getText())))) {
							toggleButton(button, pinTypeComboBoxes.get(Integer.valueOf(button.getId()) - 1),
									textFields.get(Integer.valueOf(button.getId()) - 1));
						}
					}
				});
				gridPane.add(button, col, row);
			}
			col++;
		}
	}

	private boolean isValidInput(String input) {
		if (!(input.length() == 4) || input.isEmpty() || !input.startsWith("0")) {
			return false;
		} else
			return true;
	}

	private void disableGridElements(ArrayList<Button> buttons, ArrayList<ComboBox<String>> pinTypeComboBoxes,
			final ArrayList<ComboBox<String>> inputOutputComboBoxes, ArrayList<TextField> textFields) {
		for (int i = 0; i < 40; i++) {
			if (pinTypeComboBoxes.get(i).getSelectionModel().getSelectedItem().equals("PWR5")
					|| pinTypeComboBoxes.get(i).getSelectionModel().getSelectedItem().equals("PWR3")
					|| pinTypeComboBoxes.get(i).getSelectionModel().getSelectedItem().equals("GND")
					|| pinTypeComboBoxes.get(i).getSelectionModel().getSelectedItem().equals("EEPROM")) {
				pinTypeComboBoxes.get(i).setDisable(true);
				inputOutputComboBoxes.get(i).setVisible(false);
				buttons.get(i).setDisable(true);
			}
		}
	}

	private void setElementsVisibility(ComboBox<String> pinTypeComboBox, ComboBox<String> inputOutputComboBox,
			TextField textField, Button button) {
		if (pinTypeComboBox.getSelectionModel().getSelectedItem().equals("I2C")) {
			textField.setVisible(true);
			inputOutputComboBox.setVisible(false);
			button.setDisable(false);
		} else if (pinTypeComboBox.getSelectionModel().getSelectedItem().equals("SPI")) {
			textField.setVisible(false);
			inputOutputComboBox.setVisible(false);
			button.setDisable(false);
		} else if (pinTypeComboBox.getSelectionModel().getSelectedItem().equals("UART")) {
			textField.setVisible(false);
			inputOutputComboBox.setVisible(false);
			button.setDisable(false);
		} else {
			textField.setVisible(false);
			inputOutputComboBox.setVisible(true);
			if (inputOutputComboBox.getSelectionModel().getSelectedItem().equals("OUT")) {
				button.setDisable(false);
			} else {
				button.setDisable(true);
			}
		}
	}

	private void toggleButton(Button button, ComboBox<String> pinTypeComboBox, TextField textField) {
		String valueToSend;
		if (!isPressed(button)) {
			valueToSend = "1";
			setPressed(button, "1");
		} else {
			valueToSend = "0";
			setPressed(button, "0");
		}
		networking.togglePin(button, pinTypeComboBox, textField, valueToSend, getIp(), PORT);
	}

	private void setButtons() {
		echoButton = new Button("Send echo");
		echoButton.setOnAction(this);
		requestButton = new Button("Request status");
		requestButton.setOnAction(this);
	}

	private void setIpValidationBorder(String IpAddress, TextField textField) {
		if (!isIpAddress(IpAddress)) {
			textField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
		} else {
			textField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
		}
	}

	private void setAddressValidationBorder(String input, TextField textField) {
		if (isValidInput(input)) {
			textField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
		} else {
			textField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
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
