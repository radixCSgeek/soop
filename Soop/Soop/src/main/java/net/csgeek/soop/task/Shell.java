package net.csgeek.soop.task;

import static net.csgeek.soop.Constants.ACTION_PREFIX;
import static net.csgeek.soop.Constants.CONFIG_FILE;
import static net.csgeek.soop.Constants.TASK_ARGS;
import static net.csgeek.soop.Constants.TASK_COMMAND;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.csgeek.soop.FlowFactory;

import org.apache.commons.exec.CommandLine;

import riffle.process.DependencyIncoming;
import riffle.process.DependencyOutgoing;
import riffle.process.Process;
import riffle.process.ProcessComplete;
import riffle.process.ProcessStart;
import riffle.process.ProcessStop;
import cascading.flow.Flow;
import cascading.flow.hadoop.ProcessFlow;

@Process
public class Shell implements FlowFactory {

	private String command;
	private String argStr;
	private java.lang.Process proc = null;
	private BufferedReader fromProc = null;
	private static final Pattern SETTER = Pattern.compile("^"+ACTION_PREFIX+"\\$\\$([^\\s=]+)=(.+)$");
	private static final Pattern INPUT = Pattern.compile(ACTION_PREFIX+"input=(\\S+)");
	private static final Pattern OUTPUT = Pattern.compile(ACTION_PREFIX+"output=(\\S+)");
	private LinkedList<String> inputs = new LinkedList<String>();
	private LinkedList<String> outputs = new LinkedList<String>();
	
	@Override
	public List<Flow<?>> getFlows() {
		command = System.getProperty(TASK_COMMAND);
		argStr = System.getProperty(TASK_ARGS);
		String name = command+" "+argStr;
		processDependencies();
		LinkedList<Flow<?>> flows = new LinkedList<Flow<?>>();
		flows.add(new ProcessFlow<Shell>(name, this));
		return flows;
	}

	private void processDependencies() {
		Matcher m = INPUT.matcher(argStr);
		while(m.find()) {
			inputs.add(m.group(1));
		}
		argStr = m.replaceAll("");
		
		if(inputs.isEmpty()) inputs.add(CONFIG_FILE);
		
		m = OUTPUT.matcher(argStr);
		while(m.find()) {
			outputs.add(m.group(1));
		}
		argStr = m.replaceAll("");
		if(outputs.isEmpty()) outputs.add("shell completed: "+command+" "+argStr);
	}
	
	@DependencyIncoming
	public List<String> incoming() {
		return inputs;
	}
	
	@DependencyOutgoing
	public List<String> outgoing() {
		return outputs;
	}

	@ProcessStart
	public void start()
	{
//		CommandLine commandline = CommandLine.parse(command+" "+argStr);
//		DefaultExecutor executor = new DefaultExecutor();
//		ExecuteStreamHandler streamHandler = new Exec
//		executor.execute(commandline);
//		executor.
		
		CommandLine cl = CommandLine.parse(command+" "+argStr);
		LinkedList<String> commandline = new LinkedList<String>(Arrays.asList(cl.getArguments()));
		commandline.addFirst(cl.getExecutable());
		ProcessBuilder builder = new ProcessBuilder(commandline);
		try {
			builder.redirectErrorStream(true);
			proc = builder.start();
			fromProc = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@ProcessComplete
	public void complete()
	{
		if(proc == null) start();
		try {
			String line = null;
			while((line = fromProc.readLine()) != null) {
				Matcher m = SETTER.matcher(line);
				if(m.matches()) {
					System.setProperty(m.group(1), m.group(2));
				}
			}
			fromProc.close();
			proc = null;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	@ProcessStop
	public void stop()
	{
		if(proc != null) {
			proc.destroy();
			proc = null;
		}
	}
}
