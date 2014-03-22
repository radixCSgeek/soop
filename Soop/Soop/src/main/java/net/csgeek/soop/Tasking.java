package net.csgeek.soop;

import static net.csgeek.soop.Constants.STATE_FILE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cascading.cascade.Cascade;
import cascading.cascade.CascadeConnector;
import cascading.cascade.CascadeDef;
import cascading.flow.Flow;

public class Tasking implements Runnable {

	private List<Task> tasks = new LinkedList<Task>();
	
	public Tasking(List<Task> taskList) {
		tasks = taskList;
	}
	
	
	@Override
	public void run() {
		CascadeDef def = new CascadeDef();
		for(Task oneTask : tasks) {
			for(Flow<?> f : oneTask.factory.getFlows()) {
				def.addFlow(f);
			}
		}
		CascadeConnector connector = new CascadeConnector();
		Cascade workflow = connector.connect(def);
		workflow.prepare();
		workflow.start();
		workflow.complete();
		workflow.cleanup();
		try {
			System.getProperties().store(new BufferedWriter(new FileWriter(STATE_FILE)), String.valueOf(System.currentTimeMillis()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
