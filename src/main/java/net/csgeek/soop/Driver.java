package net.csgeek.soop;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cascading.stats.FlowStats;
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
    
    public static enum FlowState { 	
	STARTED ("<div'>$1<br><progress max=100/></div>"), 
	SUCCESSFUL ("<div style='background: lightblue;'>$1</div>"), 
	ERROR ("<div style='background: red;'>$1</div>"),
	UNDEFINED ("$1");
	
	private String replacement;
    	private FlowState(String str) {
    	    replacement = str;
    	}
    	public String getReplacement() {
    	    return replacement;
    	}
    	protected FlowState setProgress(int p) {
    	    replacement = replacement.replace("progress", "progress value="+p);
    	    return this;
    	}
    	public static FlowState forStatus(FlowStats stats) {
		if(stats.isSuccessful()) {
		    return SUCCESSFUL;
		} else if(stats.isEngaged()) {
		    //TODO: Calculate actual percentage from FlowStepStats
		    return STARTED.setProgress(50);
		} else if(stats.isFailed()) {
		    return ERROR;			    
		} else return UNDEFINED;
    	}
    };
    
    public static ConcurrentHashMap<String, FlowState> statusMap = new ConcurrentHashMap<String, FlowState>();

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
