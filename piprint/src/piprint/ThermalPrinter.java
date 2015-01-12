package piprint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class ThermalPrinter {
	private static final long BYTE_SENDING_TIME = 800000; // ns
	static final byte ESC = 27;
	static final byte _7 = 55;
	static final byte DC2 = 18;
	static final byte DIEZE = 35;
	private final Serial serial;
	private PrintImageThread pit;
	private PrinterConfig printerConfig;

	public ThermalPrinter() {
		serial = SerialFactory.createInstance();
		serial.open(Serial.DEFAULT_COM_PORT, 115200);
	}

	public void configPrinterWithDefault() {
		printerConfig = new PrinterConfig();
		configPrinter(printerConfig);
	}

	public void configPrinter(PrinterConfig config) {
		printerConfig = config;
		PrinterConfigThread pct = new PrinterConfigThread(printerConfig);
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
		private PrinterConfig config;
		private byte[] sequence;

		public PrinterConfigThread(PrinterConfig config) {
			this.config = config;

			sequence = new byte[] {
					ESC, _7, 
					config.heatingMaxDot, config.heatTime, config.heatInterval, 
					DC2, DIEZE,
					(byte) ((config.printBreakTime << 5) | config.printDensity),
					0x1D, 0x61, (byte) 0xFF // auto report ?
			};
		}

		@Override
		public void run () {

			System.out.println("Printer: start config");

			for (int i = 0; i < sequence.length; i++) {
				serial.write(sequence[i]);
			}

			System.out.println("Printer: end config");
		}
	}

	private class PrintImageThread extends Thread {
		private byte[] imageBytes;
		private int width, length;
		private AtomicBoolean hold = new AtomicBoolean();
		
		public PrintImageThread(byte[] img, int width, int length) {
			this.imageBytes = img;
			this.length = length;
			this.width = width;
			hold.set(false);
		}

		public PrintImageThread(DitheredImage image) {
			this.imageBytes = image.getImageInBytes();
			this.width = image.getImageWidth();
			this.length = image.getImageLength();
			hold.set(false);
		}
		public void hold() {
			hold.set(true);
		}

		@Override
		public void run () {
			long start;
			long end;
			byte[] printLineCommand ;
			int bitONCount = 0;
			int heatingStepsCount = 0;
			int heatingTimeNs = 0;
			int heatingDotsMax = ((ThermalPrinter.this.printerConfig.heatingMaxDot +1) *8);
			
			System.out.println("Printer: start print bitmap");

			try {
				sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
			for (int i = 0; i < length; i++) {
				
				if (i%255 == 0) {
					int remainingLines = length-i;
					if (remainingLines > 255) remainingLines = 255;
					printLineCommand = new byte[] {0x12, 0x2A, (byte) remainingLines, 48};
					
					for (int j = 0; j < printLineCommand.length; j++) {
						start = System.nanoTime();
						serial.write(printLineCommand[j]);
					}
				}
				
				bitONCount = heatingDotsMax -1;
				heatingStepsCount = 0;
				
				for (int j = 0; j < 48; j++) {
					byte imageByte = imageBytes[(i*48)+j];
					
					serial.write(imageByte);
					
					bitONCount += Utils.countBitOn(imageByte);
					
					if (bitONCount >= heatingDotsMax) {
						bitONCount = 0;
						heatingStepsCount++;
					}
				}
				
				System.out.print(heatingStepsCount);
				
				switch (heatingStepsCount) {
				case 0:
					heatingTimeNs = 1800000;
					break;
				case 1:
					heatingTimeNs = 2700000;
					break;
				case 2:
					heatingTimeNs = 7200000;
					break;
				case 3:
					heatingTimeNs = 12000000;
					break;
				case 4:
					heatingTimeNs = 15000000;
					break;
				default:
					heatingTimeNs = 15000000;
					break;
				}
				
				start = System.nanoTime();
				do {
					end = System.nanoTime();
				} while(start + (heatingTimeNs) >= end);
				
			}
			
			try {
				sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			String date = sdf.format(new Date());

			serial.write(date+"\n");
			serial.write((char) 0x0A);
			serial.write((char) 0x0A);

			System.out.println("Printer: end bitmap");


		}


	}

}
