package net.csgeek.soop;

import static net.csgeek.soop.Constants.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.csgeek.soop.Driver.STATE;
import cascading.cascade.Cascade;
import cascading.cascade.CascadeConnector;
import cascading.cascade.CascadeDef;
import cascading.cascade.CascadeProps;
import cascading.flow.Flow;
import cascading.property.AppProps;

public class Tasking implements Runnable {

	private List<Task> tasks = new LinkedList<Task>();
	
	public Tasking(List<Task> taskList) {
		tasks = taskList;
	}
	
	
	@Override
	public void run() {
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
	    AppProps.setApplicationName(properties, "soop");
	    CascadeProps.setMaxConcurrentFlows(properties, 10); //TODO: Why isn't the default(0) working?
		CascadeConnector connector = new CascadeConnector(properties);
		Cascade workflow = connector.connect(def);
		workflow.writeDOT("web/workflow.dot");
		workflow.prepare();
		workflow.start();
		do {
		    for(Flow<?> f : workflow.getFlows()) {
			if(f.getFlowStats().isSuccessful()) {
			    Driver.statusMap.put(f.getName(), STATE.SUCCESSFUL);
			} else if(f.getFlowStats().isEngaged()) {
			    Driver.statusMap.put(f.getName(), STATE.STARTED);
			} else if(f.getFlowStats().isFailed()) {
			    Driver.statusMap.put(f.getName(), STATE.ERROR);			    
			}
		    }
		    
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
			//Eat it
		    }
		} while(!workflow.getStats().isFinished());
		for(Flow<?> f : workflow.getFlows()) {
		    Driver.statusMap.remove(f.getName());
		}

		workflow.cleanup();
		
		try {
			System.getProperties().store(new BufferedWriter(new FileWriter(STATE_FILE)), String.valueOf(System.currentTimeMillis()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
