package net.csgeek.soop;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import net.csgeek.soop.web.SoopWebServer;

public class Driver {
    private static final Logger LOG = Logger.getLogger(Driver.class);
    private static Docket docket; // I'm sure this isn't the right way
					  // to do this

    public static void reloadDocket() {
	LOG.info("Reloading Docket");
	docket.clear();
	try {
	    docket.load();
	} catch (IOException e) {
	    throw new IllegalStateException("Could not reload docket", e);
	}
    }
    
    public static enum STATE { 	STARTED ("<div'>$1<br><progress max=100/></div>"), 
				SUCCESSFUL ("<div style='background: lightblue;'>$1</div>"), 
				ERROR ("<div style='background: red;'>$1</div>");
	
	private String replacement;
    	private STATE(String str) {
    	    replacement = str;
    	}
    	public String getReplacement() {
    	    return replacement;
    	}
    };
    
    public static ConcurrentHashMap<String, STATE> statusMap = new ConcurrentHashMap<String, STATE>();

    public static void main(String[] args) throws Exception {
	PropertyConfigurator.configure("log4j.properties"); //Need to do this to override log4j config picked up from a dependency
	LOG.info("Starting Soop ...");
	docket = new Docket();
	docket.load();
	Runtime.getRuntime().addShutdownHook(new Thread() {
	    @Override
	    public void run() {
		LOG.info("Soop shutting down");
		docket.clear();
	    }
	});
	SoopWebServer webserver = new SoopWebServer();
	webserver.launch();
	LOG.info("Soop started.");
	webserver.waitFor();
    }

}
