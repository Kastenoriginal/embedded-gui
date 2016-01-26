package core;

import javafx.scene.control.Button;

@SuppressWarnings("restriction")
public class PinType{
	
	public enum Type {
		PWR5, PWR3, GND, GPIO, I2C, SPI, UART, EEPROM;
	}
	
	public String getPinType(Button button){
		switch (Integer.valueOf(button.getText().trim())) {
		case 1:
			button.setId(Type.PWR3.toString());
			break;
		case 2:
			button.setId(Type.PWR5.toString());
			break;
		case 3:
			button.setId(Type.I2C.toString());
			break;
		case 4:
			button.setId(Type.PWR5.toString());
			break;
		case 5:
			button.setId(Type.I2C.toString());
			break;
		case 6:
			button.setId(Type.GND.toString());
			break;
		case 8:
//			button.setUserData();
			break;
		default:
			button.setId(Type.GPIO.toString());
			break;
		}return button.getId();
	}
}

