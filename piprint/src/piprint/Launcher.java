package piprint;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Launcher {
	WatchService watcher = null; 
	WatchKey key = null;
	VFDscreen screen;
	ThermalPrinter printer;
	String fileToPrint;
	GpioController gpio;
	GpioPinDigitalInput printButton;

	public Launcher () {
		//init button
		gpio = GpioFactory.getInstance();
		printButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
		printButton.addListener(new ButtonListener());

		screen = new VFDscreen();
		screen.display("piprintlogo.png");
		
		PrinterConfig pc = new PrinterConfig();
		pc.heatingMaxDot = 7;
		pc.heatTime = (byte) 60;
		pc.heatInterval = (byte) 250;
		pc.printDensity = 14;
		pc.printBreakTime = 0;
		
		printer = new ThermalPrinter();
		printer.configPrinter(pc);

		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Path dir = Paths.get("/home/pi/project/piprint/bin/piprint");

		try {
			key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
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

					fileToPrint = ev.context().toString();
					System.out.println(">"+event.kind()+", "+ev.context());
					screen.display(fileToPrint);
				}
				akey.reset();
			} catch (InterruptedException x) {
				return;
			}
		}

	}

	public static void main(String[] args) {
		System.out.println("BluePrinter-Pi Test");
		new Launcher();
	}


	class ButtonListener implements GpioPinListenerDigital {
		
		@Override
		public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
			if (event.getState().isLow()) return;
			System.out.println("pressed !");
			
			if (printer != null && fileToPrint != null) {
				printer.printImage(fileToPrint);
			}
			
			
		}
}

}
