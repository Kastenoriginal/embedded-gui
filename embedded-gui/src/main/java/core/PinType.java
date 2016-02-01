package core;

import javafx.scene.control.Button;

@SuppressWarnings("restriction")
public class PinType{
	
	public enum Type {
		PWR5, PWR3, GND, GPIO, I2C, SPI, UART, EEPROM;
	}
	
	//TODO cez hashmap
	
	public String getPinType(Button button){
		switch (Integer.valueOf(button.getText().trim())) {
		default:
			button.setId(Type.GPIO.toString());
			break;
		}return button.getId();
	}
}

