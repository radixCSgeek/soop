package net.csgeek.soop.task;

import static net.csgeek.soop.Constants.*;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import cascading.flow.Flow;
import cascading.flow.hadoop.ProcessFlow;
import net.csgeek.soop.DependencyParser;
import net.csgeek.soop.TaskEntry;
import riffle.process.DependencyIncoming;
import riffle.process.DependencyOutgoing;
import riffle.process.Process;
import riffle.process.ProcessComplete;

@Process
public class ToolRunnerTask implements TaskEntry {

	private String argStr = null;
	private Tool toolToRun = null;
	private DependencyParser dependencies = new DependencyParser();
	
	@Override
	public List<Flow<?>> getWorkflows() {
		try {
			argStr = System.getProperty(TASK_ARGS);
			argStr = dependencies.processDependencies(argStr);
			String factoryClassName = System.getProperty(TASK_COMMAND);
			Class<?> clazz = Class.forName(factoryClassName);
			toolToRun = Tool.class.cast(clazz.newInstance());
			Flow<?> f = new ProcessFlow<ToolRunnerTask>(factoryClassName, this);
			LinkedList<Flow<?>> flows = new LinkedList<Flow<?>>();
			flows.add(f);
			return flows;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@DependencyIncoming
	public List<String> incoming() {
		List<String> inputs = dependencies.getInputs();
		if(inputs.isEmpty()) inputs.add(CONFIG_FILE);
		return inputs;
	}
	
	@DependencyOutgoing
	public List<String> outgoing() {
		List<String> outputs = dependencies.getOutputs();
		if(outputs.isEmpty()) outputs.add("ToolRunner completed:  "+argStr);
		return outputs;
	}

	@ProcessComplete
	public void run() {
		try {
			ToolRunner.run(toolToRun, CommandLine.parse("x "+argStr).getArguments());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
