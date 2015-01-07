package piprint;


public class PrinterTests {

	public static void main(String[] args) throws InterruptedException {

		System.out.println("BluePrinter-Pi Test");
		
		ThermalPrinter printer = new ThermalPrinter();
		printer.configPrinterWithDefault();
		
		DitheredImage image = new DitheredImage("basel.jpg");
		printer.printImage(image);
		
		while(printer.isPrinting()) {
			Thread.sleep(500);
			System.out.print(".");
		}
		
	}

}
