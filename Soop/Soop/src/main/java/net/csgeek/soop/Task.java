package net.csgeek.soop;

import static net.csgeek.soop.Constants.PACKAGE;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cascading.flow.Flow;

public class Task {
	private static final Pattern VAR = Pattern.compile("\\$\\$\\{(\\S+)\\}");
	
	private FlowFactory factory;
	private String command;
	private String args;
	
	public Task(String entry) {
		String[] parts = entry.trim().split("\\s+", 2);
		String[] commandParts = parts[0].split(":");
		String taskName = commandParts[0];
		command = commandParts[1];
		args = parts[1];
		
		try {
			String factoryClassName = taskName;
			if(!taskName.contains(".")) {
				factoryClassName = PACKAGE+".task."+taskName;
			}
			Class<?> clazz = Class.forName(factoryClassName);
			factory = FlowFactory.class.cast(clazz.newInstance());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private String replaceVars(String line) {
		Matcher m = VAR.matcher(line);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String property = System.getProperty(m.group(1));
			m.appendReplacement(sb, property==null?"":property);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public List<Flow<?>> getFlows() {
		return factory.getFlows();
	}

	public String getCommand() {
		return command;
	}

	public String getArgs() {
		return replaceVars(args);
	}
}
