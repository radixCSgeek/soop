package net.csgeek.soop.task;

import static net.csgeek.soop.Constants.TASK_ARGS;
import static net.csgeek.soop.Constants.TASK_COMMAND;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.csgeek.soop.FlowFactory;
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
	private static final Pattern SETTER = Pattern.compile("^\\$\\$([^\\s=]+)=(.+)$");
	private static final Pattern INPUT = Pattern.compile("\\!INPUT=(\\S+)");
	private static final Pattern OUTPUT = Pattern.compile("\\!OUTPUT=(\\S+)");
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
		m = OUTPUT.matcher(argStr);
		while(m.find()) {
			outputs.add(m.group(1));
		}
		argStr = m.replaceAll("");
	}

	@ProcessStart
	public void start()
	{
		ProcessBuilder builder = new ProcessBuilder(command+" "+argStr);
		try {
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
			proc.waitFor();
			String line = null;
			while((line = fromProc.readLine()) != null) {
				Matcher m = SETTER.matcher(line);
				if(m.matches()) {
					System.setProperty(m.group(1), m.group(2));
				}
			}
			fromProc.close();
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
