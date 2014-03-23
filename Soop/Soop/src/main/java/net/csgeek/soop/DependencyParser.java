package net.csgeek.soop;

import static net.csgeek.soop.Constants.ACTION_PREFIX;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyParser {
	private static final Pattern INPUT = Pattern.compile(ACTION_PREFIX+"input=(\\S+)");
	private static final Pattern OUTPUT = Pattern.compile(ACTION_PREFIX+"output=(\\S+)");
	private LinkedList<String> inputs = new LinkedList<String>();
	private LinkedList<String> outputs = new LinkedList<String>();

	public String processDependencies(String argStr) {
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
		return argStr;
	}

	public LinkedList<String> getInputs() {
		return inputs;
	}

	public LinkedList<String> getOutputs() {
		return outputs;
	}

}
