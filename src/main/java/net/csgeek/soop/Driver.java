package net.csgeek.soop;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.csgeek.soop.web.SoopWebServer;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cascading.stats.FlowStats;
import cascading.stats.FlowStepStats;

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
	STARTED ("<div>$1<br><progress %p></progress></div>"), 
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
    	protected FlowState setProgress(double p) {
    	    replacement = replacement.replace("%p", "value="+p);
    	    return this;
    	}
    	public static FlowState forStatus(FlowStats stats) {
		if(stats.isSuccessful()) {
		    return SUCCESSFUL;
		} else if(stats.isEngaged()) {
		    double progress = 0.0;
		    List<FlowStepStats> flowStepStats = stats.getFlowStepStats();
		    if(flowStepStats != null && !flowStepStats.isEmpty()) {
        		for(FlowStepStats fs : flowStepStats){
        		    if(fs.isEngaged()) {
        			progress += 0.5;
        		    } else if(fs.isSuccessful() | fs.isSkipped()) {
        			progress += 1.0;
        		    }
        		    progress /= flowStepStats.size();
        		}
        		return STARTED.setProgress(progress);
		    } else {
			return STARTED;
		    }
		} else if(stats.isFailed()) {
		    return ERROR;			    
		} else return UNDEFINED;
    	}
    };
    
    public static ConcurrentHashMap<String, FlowState> statusMap = new ConcurrentHashMap<String, FlowState>();
    public static ConcurrentLinkedQueue<Tasking> taskList = new ConcurrentLinkedQueue<Tasking>();

    public static void main(String[] args) throws Exception {
	PropertyConfigurator.configure("log4j.properties"); //Need to do this to override log4j config picked up from a dependency
	LOG.info("Starting Soop ...");
	File dotDir = new File("web/workflow");
	if(dotDir.exists()) {
	    FileUtils.deleteDirectory(dotDir);
	}
	dotDir.mkdirs();
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
	FileUtils.deleteDirectory(dotDir);
    }

}
