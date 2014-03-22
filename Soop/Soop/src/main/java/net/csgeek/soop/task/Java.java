package net.csgeek.soop.task;

import static net.csgeek.soop.Constants.TASK_COMMAND;

import java.util.LinkedList;
import java.util.List;

import net.csgeek.soop.FlowFactory;
import cascading.flow.Flow;
import cascading.flow.hadoop.ProcessFlow;

public class Java implements FlowFactory {

	@Override
	public List<cascading.flow.Flow<?>> getFlows() {
		try {
			String factoryClassName = System.getProperty(TASK_COMMAND);
			Class<?> clazz = Class.forName(factoryClassName);
			Object riffleObj = clazz.newInstance();
			Flow<?> f = new ProcessFlow<Object>(factoryClassName, riffleObj);
			LinkedList<Flow<?>> flows = new LinkedList<Flow<?>>();
			flows.add(f);
			return flows;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
