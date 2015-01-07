package piprint;
// START SNIPPET: serial-snippet


/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  SerialExample.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2014 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

/**
 * This example code demonstrates how to perform serial communications using the Raspberry Pi.
 * 
 * @author Robert Savage
 */
public class SerialExample {
	

	public static void main(String args[]) throws InterruptedException {
		
		System.out.println("compute image");

		BufferedImage img = null;
		try {
			img = ImageIO.read(SerialExample.class.getResource("basel.jpg"));
		} catch (IOException e) { }

		int imgH = img.getHeight();
		int imgW = img.getWidth();
		int resizedHeight = imgW / (imgH / 384);

		final BufferedImage monoImageresized = new BufferedImage(resizedHeight, 384, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = monoImageresized.createGraphics();
		g.drawImage(img, 0, 0, resizedHeight, 384, null);
		g.dispose();

		final BufferedImage monoImagerotated = new BufferedImage(384, resizedHeight, BufferedImage.TYPE_BYTE_GRAY);
		g = monoImagerotated.createGraphics();
		g.rotate(Math.PI/2);
		g.drawImage(monoImageresized, 0, -384, resizedHeight, 384, null);
		g.dispose();

		int[] pixList = new int[resizedHeight * 384 ];
		int[][] pixArray2D = new int[384][resizedHeight];
		int[][] pixArray2Ddest = new int[384][resizedHeight];
		int S_WIDTH = 384;
		int S_HEIGHT = resizedHeight;

		monoImagerotated.getData().getPixels(0, 0, 384, resizedHeight, pixList);

		for (int i = 0; i < pixList.length; i++) {
			pixArray2D[i%S_WIDTH][i/S_WIDTH] = (int) ((pixList[i]));
			pixArray2Ddest[i%S_WIDTH][i/S_WIDTH] = 0;
		}

		int width = S_WIDTH;
		int height = S_HEIGHT;
		int oldpixel, newpixel, error;
		boolean nbottom, nleft, nright, nright2;

		for (int y=0; y<height; y++) {
			nbottom=y<height-1;
			for (int x=0; x<width; x++) {
				nleft = x>0; 
				nright = x<width-1;
				nright2 = x<width-2;

				oldpixel = pixArray2Ddest[x][y] + pixArray2D[x][y];

				if (oldpixel<128) 
					newpixel = 0;
				else 
					newpixel = 255;

				pixArray2Ddest[x][y] = newpixel;
				error = oldpixel-newpixel;

				if (nright) 		pixArray2Ddest[x+1][y]	+= 7*error/16;
				if (nleft&&nbottom) pixArray2Ddest[x-1][y+1]+= 3*error/16;
				if (nbottom) 		pixArray2Ddest[x][y+1] 	+= 5*error/16;   
				if (nright&&nbottom)pixArray2Ddest[x+1][y+1]+=   error/16;

			}
		}

		for (int i = 0; i < pixList.length; i++) {
			pixList[i]  = pixArray2Ddest[i%S_WIDTH][i/S_WIDTH];
		}

		byte[] result = new byte[48*S_HEIGHT];
		int mask = 0x01;
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < 8; j++) {
				mask = 0b10000000 >>> j;
				result[i] =  (byte) (pixList[(i*8)+j] == 0 ? (result[i] | mask) : result[i]);
			}
		}


		// !! ATTENTION !!
		// By default, the serial port is configured as a console port 
		// for interacting with the Linux OS shell.  If you want to use 
		// the serial port in a software program, you must disable the 
		// OS from using this port.  Please see this blog article by  
		// Clayton Smith for step-by-step instructions on how to disable 
		// the OS console for this port:
		// http://www.irrational.net/2012/04/19/using-the-raspberry-pis-serial-port/

		System.out.println("<--Pi4J--> Serial Communication Example ... started.");
		System.out.println(" ... connect using settings: 19200, N, 8, 1.");

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();


		try {
			// open the default serial port provided on the GPIO header
			serial.open(Serial.DEFAULT_COM_PORT, 19200);

			// continuous loop to keep the program running until the user terminates the program
			//            for (;;) {
			try {
				// write a formatted string to the serial transmit buffer
				//                    serial.write("CURRENT TIME: %s", new Date().toString());
				// write a individual bytes to the serial transmit buffer
				//                    serial.write((byte) 13);
				//                    serial.write((byte) 10);
				// write a simple string to the serial transmit buffer
				//                    serial.write("Second Line");
				// write a individual characters to the serial transmit buffer
				//                    serial.write('\r');
				//                    serial.write('\n');
				// write a string terminating with CR+LF to the serial transmit buffer
				//                    serial.writeln("Third Line");
				byte widthBytes = 48;
				byte heigth = (byte) 120;
				byte[] printLine = new byte[] {0x12, 0x2A, heigth, widthBytes};

				serial.writeln("--- Test ---");
				Thread.sleep(500);

				serial.write(printLine);
				Thread.sleep(500);

				final long INTERVAL = 280000;
				for (int i = 0; i < heigth*widthBytes; i++) {

					long start = System.nanoTime();
					long end=0;
					char out;

					//					if (i%48 < 24) {
					//						out = (char) (i%48);
					//					} else {
					//						out = (char) (Math.random()*255);
					//					}
					//					out = (char) (Math.random()*255);
					out = (char) result[i];
					
					serial.write(out);

					do{
						end = System.nanoTime();
					}while(start + INTERVAL >= end);
				}


				Thread.sleep(500);

				for (int i = 0; i < 1; i++) {
					serial.write((char) 0x0A);

				}

			}
			catch(IllegalStateException ex){
				ex.printStackTrace();                    
			}

			System.out.println("print ?");

			// wait 1 second before continuing
			Thread.sleep(1000);
			//            }

		}
		catch(SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
	}
}

// END SNIPPET: serial-snippet
