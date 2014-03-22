package net.csgeek.soop;

import java.util.List;

import cascading.flow.Flow;

public interface FlowFactory {
	public List<Flow<?>> getFlows();
}
