package net.csgeek.soop;

import static net.csgeek.soop.Constants.CONFIG_FILE;
import static net.csgeek.soop.Constants.STATE_FILE;
import it.sauronsoftware.cron4j.Scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class Docket {
	private Scheduler sched;
	private static final Pattern SKIP = Pattern.compile("^(?:(?:#.*)|(?:\\s*))$");
	private static final Pattern LEADING_WHITESPACE = Pattern.compile("^\\s+\\S+.*$");
	
	public void load() throws IOException {
		initializeState();
		sched = new Scheduler();
		BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));
		String schedPattern = null;
		ArrayList<Task> tasks = new ArrayList<Task>();
		String line = null;
		while((line = reader.readLine()) != null) {
			if(SKIP.matcher(line).matches()) continue;
			if(LEADING_WHITESPACE.matcher(line).matches()) {
				tasks.add(new Task(line));
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

	private void initializeState() throws IOException {
		File stateFile = new File(STATE_FILE);
		if(stateFile.exists()) {
			System.getProperties().load(new FileReader(stateFile));
		}
		System.getProperties().store(new FileWriter(stateFile), String.valueOf(System.currentTimeMillis()));
	}

	private void scheduleLoadedEntries(String schedPattern,
			ArrayList<Task> tasks) {
		if(schedPattern != null && !tasks.isEmpty()) {
			sched.schedule(schedPattern, new Tasking(tasks));
		}
	}
	
	public void clear() {
		sched.stop();
		sched = null;
	}
	
	public static void main(String[] args) throws Exception {
		final Docket docket = new Docket();
		docket.load();
		Runtime.getRuntime().addShutdownHook(new Thread() {		
			@Override
			public void run() {
				docket.clear();
			}
		});
		Semaphore block = new Semaphore(0);
		block.acquire(); //Blocks forever with scheduler thread running
	}
}