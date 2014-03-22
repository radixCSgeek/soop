package net.csgeek.soop.task;

import static net.csgeek.soop.Constants.TASK_COMMAND;

import java.util.List;

import net.csgeek.soop.FlowFactory;

public class Cascading implements FlowFactory {

	@Override
	public List<cascading.flow.Flow<?>> getFlows() {
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
