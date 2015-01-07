package piprint;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class ThermalPrinter {
	
	private final Serial serial;
	
	public ThermalPrinter() {
		serial = SerialFactory.createInstance();
		serial.open(Serial.DEFAULT_COM_PORT, 19200);
	}
	
	public void printImage(byte[] img, int width, int length) {
		PrintImageThread pit = new PrintImageThread(img, width, length);
		pit.start();
	}
	
	
	private class PrintImageThread extends Thread {
		private long lastSendingTime;
		byte[] img;
		int width, length;
		
		public PrintImageThread(byte[] img, int width, int length) {
			this.img = img;
			this.length = length;
			this.width = width;
		}
		
		@Override
		public void run () {
			
			
			
			
			
		}
		
		
	}

}
