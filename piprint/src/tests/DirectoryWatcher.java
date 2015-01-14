package tests;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirectoryWatcher {

	public static void main(String[] args) {
		WatchService watcher = null;
		WatchKey key = null;
		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Path dir = Paths.get("c:\\pierre");
		try {
			key = dir.register(watcher, ENTRY_CREATE);
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
					
				}
				akey.reset();
			} catch (InterruptedException x) {
				return;
			}
		}
	}

}
