package net.csgeek.soop;

import java.util.List;

import cascading.flow.Flow;

public interface TaskEntry {
	public List<Flow<?>> getWorkflows();
}
