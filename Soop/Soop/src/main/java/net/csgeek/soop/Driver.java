package net.csgeek.soop;

import java.io.IOException;

import net.csgeek.soop.web.SoopWebServer;

public class Driver {
	private static Docket docket; //I'm sure this isn't the right way to do this

	public static void reloadDocket() {
		docket.clear();
		try {
			docket.load();
		} catch (IOException e) {
			throw new IllegalStateException("Could not reload docket", e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		docket = new Docket();
		docket.load();
		Runtime.getRuntime().addShutdownHook(new Thread() {		
			@Override
			public void run() {
				docket.clear();
			}
		});
		SoopWebServer webserver = new SoopWebServer();
		webserver.launch();
	}

}
