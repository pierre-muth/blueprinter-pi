package tests;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import piprint.PrinterConfig;
import piprint.ThermalPrinter;
import piprint.VFDscreen;

public class PrinterTests {

	public static void main(String[] args) throws InterruptedException {
		WatchService watcher = null;
		WatchKey key = null;

		System.out.println("BluePrinter-Pi Test");
		
		VFDscreen screen = new VFDscreen();
		screen.display("basel.jpg");
		
		PrinterConfig pc = new PrinterConfig();
		pc.heatingMaxDot = 7;
		pc.heatTime = (byte) 60;
		pc.heatInterval = (byte) 250;
		pc.printDensity = 14;
		pc.printBreakTime = 0;
		
		ThermalPrinter printer = new ThermalPrinter();
		printer.configPrinter(pc);
//		printer.printImage("family.jpg");
		
		/*
		
		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Path dir = Paths.get("/home/pi/project/piprint/bin/piprint");
		try {
			key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
		}


		WatchKey akey;
		for(;;) {
			try {
				akey = watcher.take();
				for (WatchEvent<?> event: key.pollEvents()) {
					
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>)event;
					System.out.println(">"+event.kind()+", "+ev.context());
					
					DitheredImage image = new DitheredImage(ev.context().toString());
					printer.printImage(image);
					
					int timeSpent = 0;
					while(printer.isPrinting()) {
						Thread.sleep(1000);
						System.out.print(".");
						timeSpent++;
					}
					System.out.println("Printing total Time: "+timeSpent+"s");
					
				}
				akey.reset();
			} catch (InterruptedException x) {
				return;
			}
		}
		
		*/
		
	}

}
