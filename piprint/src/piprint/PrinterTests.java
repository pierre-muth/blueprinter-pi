package piprint;


public class PrinterTests {

	public static void main(String[] args) throws InterruptedException {

		System.out.println("BluePrinter-Pi Test");
		
		ThermalPrinter printer = new ThermalPrinter();
//		printer.configPrinterWithDefault();
		PrinterConfig pc = new PrinterConfig();
		pc.heatingMaxDot = 11;
		pc.heatTime = (byte) 60;
		pc.heatInterval = (byte) 60;
		
		pc.printDensity = 14;
		pc.printBreakTime = 0;
		
		printer.configPrinter(pc);
		
		DitheredImage image = new DitheredImage("atest1.jpg");
		printer.printImage(image);
		
		int timeSpent = 0;
		while(printer.isPrinting()) {
			Thread.sleep(1000);
			System.out.print(".");
			timeSpent++;
		}
		System.out.println("Printing total Time: "+timeSpent+"s");
		
	}

}
