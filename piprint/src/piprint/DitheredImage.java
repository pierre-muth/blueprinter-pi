package piprint;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DitheredImage {
	private String imageFileName;
	private byte[] result;
	private int resizedImageLength;
	private int resizedImageWidth = 384;

	public DitheredImage(String imageFileName) {
		this.imageFileName = imageFileName;

		System.out.println("compute image");

		BufferedImage img = null;
		try {
			img = ImageIO.read(DitheredImage.class.getResource(imageFileName));
		} catch (IOException e) { }

		int imgH = img.getHeight();
		int imgW = img.getWidth();
		double ratio = (double)imgH / (double)resizedImageWidth;
		resizedImageLength = (int) ((double)imgW / ratio);

		final BufferedImage monoImageresized = new BufferedImage(resizedImageLength, resizedImageWidth, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = monoImageresized.createGraphics();
		g.drawImage(img, 0, 0, resizedImageLength, resizedImageWidth, null);
		g.dispose();

		final BufferedImage monoImageRotated = new BufferedImage(resizedImageWidth, resizedImageLength, BufferedImage.TYPE_BYTE_GRAY);
		g = monoImageRotated.createGraphics();
		g.rotate(Math.PI/2);
		g.drawImage(monoImageresized, 0, -resizedImageWidth, resizedImageLength, resizedImageWidth, null);
		g.dispose();

		int[] pixList = new int[resizedImageLength * resizedImageWidth ];
		int[][] pixArray2D = new int[resizedImageWidth][resizedImageLength];
		int[][] pixArray2Ddest = new int[resizedImageWidth][resizedImageLength];

		monoImageRotated.getData().getPixels(0, 0, resizedImageWidth, resizedImageLength, pixList);

		for (int i = 0; i < pixList.length; i++) {
			pixArray2D[i%resizedImageWidth][i/resizedImageWidth] = (int) ((pixList[i]));
			pixArray2Ddest[i%resizedImageWidth][i/resizedImageWidth] = 0;
		}

		int oldpixel, newpixel, error;
		boolean nbottom, nleft, nright;

		for (int y=0; y<resizedImageLength; y++) {
			nbottom=y<resizedImageLength-1;
			for (int x=0; x<resizedImageWidth; x++) {
				nleft = x>0; 
				nright = x<resizedImageWidth-1;
				oldpixel = pixArray2Ddest[x][y] + pixArray2D[x][y];

				if (oldpixel<128) newpixel = 0;
				else newpixel = 255;

				pixArray2Ddest[x][y] = newpixel;
				error = oldpixel-newpixel;

				if (nright) 		pixArray2Ddest[x+1][y]	+= 7*error/16;
				if (nleft&&nbottom) pixArray2Ddest[x-1][y+1]+= 3*error/16;
				if (nbottom) 		pixArray2Ddest[x][y+1] 	+= 5*error/16;   
				if (nright&&nbottom)pixArray2Ddest[x+1][y+1]+=   error/16;
			}
		}

		for (int i = 0; i < pixList.length; i++) {
			pixList[i]  = pixArray2Ddest[i%resizedImageWidth][i/resizedImageWidth];
		}

		result = new byte[(resizedImageWidth/8)*resizedImageLength];
		int mask = 0x01;
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < 8; j++) {
				mask = 0b10000000 >>> j;
				if ( (byte)(pixList[(i*8)+j]) == 0 ) {
					result[i] = (byte) (result[i] | mask);
				}
			}
		}

	}

	public byte[] getImageInBytes() {
		return result;
	}
	
	public int getImageLength() {
		return resizedImageLength;
	}
	
	public int getImageWidth() {
		return resizedImageWidth;
	}

}
