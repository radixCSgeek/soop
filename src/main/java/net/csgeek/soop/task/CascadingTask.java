package net.csgeek.soop.task;

import static net.csgeek.soop.Constants.TASK_COMMAND;

import java.util.List;

import net.csgeek.soop.TaskEntry;
import cascading.flow.Flow;

public class CascadingTask implements TaskEntry {

	public static interface FlowFactory {
		public List<Flow<?>> getFlows();
	}

	@Override
	public List<Flow<?>> getWorkflows() {
		try {
			String factoryClassName = System.getProperty(TASK_COMMAND);
			Class<?> clazz = Class.forName(factoryClassName);
			FlowFactory factory = FlowFactory.class.cast(clazz.newInstance());
			return factory.getFlows();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
