package net.csgeek.soop;

import static net.csgeek.soop.Constants.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.io.MD5Hash;

import net.csgeek.soop.Driver.FlowState;
import cascading.cascade.Cascade;
import cascading.cascade.CascadeConnector;
import cascading.cascade.CascadeDef;
import cascading.cascade.CascadeProps;
import cascading.flow.Flow;
import cascading.property.AppProps;

public class Tasking implements Runnable {

	private List<Task> tasks = new LinkedList<Task>();
	private String schedule;
	private String dotFileName;
	
	public Tasking(List<Task> taskList, String sched) {
		tasks = new ArrayList<Task>(taskList);
		schedule = sched;
		dotFileName = "web/workflow/"+MD5Hash.digest(schedule)+".dot";
		Cascade workflow = getWorkflow();
		workflow.writeDOT(dotFileName);
	}
	
	
	public String getSchedule() {
	    return schedule;
	}


	public String getDotFileName() {
	    return dotFileName;
	}

	private String getFlowKey(Flow<?> f) {
	    return dotFileName+"@@@"+f.getName();
	}

	@Override
	public void run() {
		Cascade workflow = getWorkflow();
		workflow.writeDOT(dotFileName);
		workflow.prepare();
		workflow.start();
		do {
		    for(Flow<?> f : workflow.getFlows()) {
			Driver.statusMap.put(getFlowKey(f), FlowState.forStatus(f.getFlowStats()));
		    }
		    
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
			//Eat it
		    }
		} while(!workflow.getStats().isFinished());
		for(Flow<?> f : workflow.getFlows()) {
		    Driver.statusMap.remove(getFlowKey(f));
		}

		workflow.cleanup();
		
		try {
			System.getProperties().store(new BufferedWriter(new FileWriter(STATE_FILE)), String.valueOf(System.currentTimeMillis()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}


	private Cascade getWorkflow() {
	    CascadeDef def = new CascadeDef();
	    for(Task oneTask : tasks) {
	    	System.setProperty(TASK_COMMAND, oneTask.getCommand());
	    	System.setProperty(TASK_ARGS, oneTask.getArgs());
	    	for(Flow<?> f : oneTask.getFlows()) {
	    		def.addFlow(f);
	    	}
	    }
	    
	    Properties properties = new Properties();
	    AppProps.setApplicationJarClass( properties, Docket.class );
	    AppProps.setApplicationName(properties, "soop "+getSchedule());
	    CascadeProps.setMaxConcurrentFlows(properties, 10); //TODO: Why isn't the default(0) working?
	    CascadeConnector connector = new CascadeConnector(properties);
	    Cascade workflow = connector.connect(def);
	    return workflow;
	}

}
