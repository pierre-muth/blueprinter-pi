package piprint;

import java.util.concurrent.atomic.AtomicBoolean;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class VFDscreen {
	private final Serial serial;
	private DisplayImageThread displayThread;
	private AtomicBoolean reDisplay = new AtomicBoolean(false);

	public VFDscreen() {
		serial = SerialFactory.createInstance();
		serial.open("/dev/ttyUSB0", 115200);
	}

	public void display(String file) {
		DitheredImage image = new DitheredImage(file, 100, 64);
		if (displayThread != null && displayThread.isAlive()) {
			reDisplay.set(true);
		} else {
			reDisplay.set(true);
			displayThread = new DisplayImageThread(image);
			displayThread.start();
		}
	}
	
	private class DisplayImageThread extends Thread {
		private int width, length;
		private byte[] imageLow, imageHight;

		public DisplayImageThread(DitheredImage image) {
			byte[][] images = image.getImageInBytesForVFD();
			if (images == null) {
				System.out.println("Didn't find image");
				return;
			}
			
			this.imageLow = images[0];
			this.imageHight = images[1];
			this.width = image.getImageWidth();
			this.length = image.getImageLength();
		}

		@Override
		public void run () {
			byte resetCommand = 0x00;
			byte bitmapCommand = 0x06;
			byte controlCommand = 0x04;
			byte brightnessCommand = 0x01;

			serial.write(resetCommand);

			try { sleep(100);
			} catch (InterruptedException e) {}

			serial.write(brightnessCommand);
			serial.write((byte)0x10);
			
			if (imageHight == null || imageLow == null) {
				return;
			}

			while(reDisplay.get()) {
				reDisplay.set(false);

				serial.write(controlCommand);
				serial.write((byte)0b00010000); //layer 0
				serial.write(bitmapCommand);
				int i=0;
				for (int y = 0; y < 8; y++) {
					for (int x = 127; x >= 0; x--) {
						if (x < length && y < width/8 && i<1024) {
							serial.write(imageLow[(y) + (x*8)]);
						} else {
							serial.write((byte)0x00);
						}
						i++;
					}
				}

				for (int j = 1; j < 4; j++) {

					serial.write(controlCommand);
					serial.write((byte) (0b00010000+(4*j))); //layer 1 to 4
					serial.write(bitmapCommand);
					i=0;
					for (int y = 0; y < 8; y++) {
						for (int x = 127; x >= 0; x--) {
							if (x < length && y < width/8 && i<1024) {
								serial.write(imageHight[(y) + (x*8)]);
							} else {
								serial.write((byte)0x00);
							}
							i++;
						}
					}
				}


			}


		}

	}
}
