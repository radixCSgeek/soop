package net.csgeek.soop;

import static net.csgeek.soop.Constants.CONFIG_FILE;
import static net.csgeek.soop.Constants.STATE_FILE;
import it.sauronsoftware.cron4j.Scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Docket {
	private Scheduler sched;
	private static final Pattern COMMENT = Pattern.compile("#.*$");
	private static final Pattern SKIP = Pattern.compile("^\\s*$");
	private static final Pattern LEADING_WHITESPACE = Pattern.compile("^\\s+\\S+.*$");
	
	private final Logger LOG = Logger.getLogger(Docket.class);
	
	public void load() throws IOException {
		System.out.println("Loding docket file: "+CONFIG_FILE);
		ClassLoader cLoader = getDylibClassLoader();
		initializeState();
		sched = new Scheduler();
		BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));
		String schedPattern = null;
		ArrayList<Task> tasks = new ArrayList<Task>();
		String line = null;
		while((line = reader.readLine()) != null) {
			line = COMMENT.matcher(line).replaceFirst("");
			if(SKIP.matcher(line).matches()) continue;
			if(LEADING_WHITESPACE.matcher(line).matches()) {
				tasks.add(new Task(line.trim(), cLoader));
			} else {
				scheduleLoadedEntries(schedPattern, tasks);
				tasks.clear();
				schedPattern = line;
			}
		}
		scheduleLoadedEntries(schedPattern, tasks);
		reader.close();
		sched.start();
	}

	private ClassLoader getDylibClassLoader()
			throws MalformedURLException {
		File dylib = new File("dylib");
		if(dylib.exists() && dylib.isDirectory()) {
			File[] libs = dylib.listFiles();
			URL[] urls = new URL[libs.length];
			for(int i = 0; i < libs.length; i++) {
				urls[i] = libs[i].toURI().toURL();
			}
			return new URLClassLoader(urls, Docket.class.getClassLoader());
		}
		return Docket.class.getClassLoader();
	}

	private void initializeState() throws IOException {
		File stateFile = new File(STATE_FILE);
		if(stateFile.exists()) {
			System.getProperties().load(new FileReader(stateFile));
		}
		System.getProperties().store(new FileWriter(stateFile), String.valueOf(System.currentTimeMillis()));
	}

	private void scheduleLoadedEntries(String schedPattern, ArrayList<Task> tasks) {
		if(schedPattern != null && !tasks.isEmpty()) {
			System.out.println("Scheduled "+tasks.size()+" tasks for "+schedPattern);
			sched.schedule(schedPattern, new Tasking(tasks));
		}
	}
	
	public void clear() {
		System.out.println("Clearing docket");
		sched.stop();
		sched = null;
	}	
}
