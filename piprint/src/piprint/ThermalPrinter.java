package piprint;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class ThermalPrinter {
	private static final long BYTE_SENDING_TIME = 400000; // ns
	private final Serial serial;
	private PrintImageThread pit;
	
	
	public ThermalPrinter() {
		serial = SerialFactory.createInstance();
		serial.open(Serial.DEFAULT_COM_PORT, 19200);
	}
	
	public void configPrinterWithDefault() {
		PrinterConfig pc = new PrinterConfig();
		configPrinter(pc);
	}
	
	public void configPrinter(PrinterConfig config) {
		PrinterConfigThread pct = new PrinterConfigThread(config);
		pct.start();
	}
	
	public void printImage(byte[] img, int width, int length) {
		pit = new PrintImageThread(img, width, length);
		pit.start();
	}
	
	public void printImage(DitheredImage image) {
		pit = new PrintImageThread(image);
		pit.start();
	}
	
	public boolean isPrinting() {
		return pit != null && pit.isAlive();
	}
	
	private class PrinterConfigThread extends Thread {
		static final byte ESC = 27;
		static final byte _7 = 55;
		static final byte DC2 = 18;
		static final byte DIEZE = 35;
		
		private PrinterConfig config;
		private byte[] sequence;
		
		public PrinterConfigThread(PrinterConfig config) {
			this.config = config;
			
			sequence = new byte[] {
					ESC, _7, 
					config.heatingDot, config.heatTime, config.heatInterval, 
					DC2, DIEZE,
					(byte) ((config.printBreakTime << 5) | config.printDensity)
			};
		}
		
		@Override
		public void run () {
			long start;
			long end;
			
			System.out.println("Printer: start config");
			
			for (int i = 0; i < sequence.length; i++) {
				start = System.nanoTime();
				serial.write(sequence[i]);
				do {
					end = System.nanoTime();
				} while(start + BYTE_SENDING_TIME >= end);
			}
			
			System.out.println("Printer: end config");
		}
	}
	
	private class PrintImageThread extends Thread {
		private byte[] imageBytes;
		private int width, length;
		
		public PrintImageThread(byte[] img, int width, int length) {
			this.imageBytes = img;
			this.length = length;
			this.width = width;
			
		}
		
		public PrintImageThread(DitheredImage image) {
			this.imageBytes = image.getImageInBytes();
			this.width = image.getImageWidth();
			this.length = image.getImageLength();
		}
		
		@Override
		public void run () {
			long start;
			long end;
			byte[] printLineCommand = new byte[] {
					0x12, 0x2A, 120, 48};
			
			System.out.println("Printer: start bitmap");
			
			serial.write(printLineCommand);
			try {
				sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for (int i = 0; i < 120*48; i++) {
				start = System.nanoTime();
				serial.write(imageBytes[i]);
				do {
					end = System.nanoTime();
				} while(start + BYTE_SENDING_TIME >= end);
			}
			
			try {
				sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			serial.write((char) 0x0A);
			serial.write((char) 0x0A);
			
			System.out.println("Printer: end bitmap");
			
			
		}
		
		
	}

}
