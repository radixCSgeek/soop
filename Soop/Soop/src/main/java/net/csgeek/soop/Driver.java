package net.csgeek.soop;

import net.csgeek.soop.web.SoopWebServer;

public class Driver {
	
	public static void main(String[] args) throws Exception {
		final Docket docket = new Docket();
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
