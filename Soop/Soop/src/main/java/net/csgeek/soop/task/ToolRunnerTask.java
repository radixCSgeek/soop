package net.csgeek.soop.task;

import static net.csgeek.soop.Constants.CONFIG_FILE;
import static net.csgeek.soop.Constants.TASK_ARGS;
import static net.csgeek.soop.Constants.TASK_COMMAND;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import net.csgeek.soop.DependencyParser;
import net.csgeek.soop.TaskEntry;

import org.apache.commons.exec.CommandLine;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import riffle.process.DependencyIncoming;
import riffle.process.DependencyOutgoing;
import riffle.process.Process;
import riffle.process.ProcessComplete;
import riffle.process.ProcessStop;
import cascading.flow.Flow;
import cascading.flow.hadoop.ProcessFlow;

@Process
public class ToolRunnerTask implements TaskEntry {

	private String argStr = null;
	private Tool toolToRun = null;
	private DependencyParser dependencies = new DependencyParser();
	private Method toolIncomingMethod = null;
	private Method toolOutgoingMethod = null;
	
	@Override
	public List<Flow<?>> getWorkflows() {
		try {
			argStr = System.getProperty(TASK_ARGS);
			argStr = dependencies.processDependencies(argStr);
			toolIncomingMethod = findMethodWith(toolToRun, DependencyIncoming.class, true);
			toolOutgoingMethod = findMethodWith(toolToRun, DependencyOutgoing.class, true);
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
	public Object incoming() {
		List<String> inputs = dependencies.getInputs();
		if(inputs.isEmpty()) {
			if(toolIncomingMethod!= null) {
				try {
					return toolIncomingMethod.invoke(toolToRun);
				} catch (Exception e) {
					throw new IllegalStateException("Failed to invoke @DependencyIncoming annotated Tool method", e);
				}
			} else {
				inputs.add(CONFIG_FILE);
			}
		}
		return inputs;
	}
	
	@DependencyOutgoing
	public Object outgoing() {
		List<String> outputs = dependencies.getOutputs();
		if(outputs.isEmpty()) {
			if(toolOutgoingMethod!= null) {
				try {
					return toolOutgoingMethod.invoke(toolToRun);
				} catch (Exception e) {
					throw new IllegalStateException("Failed to invoke @DependencyOutgoing annotated Tool method", e);
				}
			} else {
				 outputs.add("ToolRunner completed:  "+argStr);
			}
		}
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
	
	@ProcessStop
	public void stop() {
		//hmmm ... nothing we can do here really
	}
	
	
	/******* Borrowed with slight modifications from Riffle *******/
	
	 private Method findMethodWith( Object obj, Class<? extends Annotation> type, boolean isOptional )
	    {
	    Method[] methods = obj.getClass().getMethods();

	    for( Method method : methods )
	      {
	      if( method.getAnnotation( type ) == null )
	        continue;

	      int modifiers = method.getModifiers();

	      if( Modifier.isAbstract( modifiers ) )
	        throw new IllegalStateException( "@"+type.getSimpleName()+" annotated method: " + method.getName() + " must not be abstract" );

	      if( Modifier.isInterface( modifiers ) )
	        throw new IllegalStateException( "@"+type.getSimpleName()+" annotated method: " + method.getName() + " must be implemented" );

	      if( !Modifier.isPublic( modifiers ) )
	        throw new IllegalStateException( "@"+type.getSimpleName()+" annotated method: " + method.getName() + " must be public" );

	      return method;
	      }

	    if( !isOptional )
	      throw new IllegalStateException( "no method found declaring annotation: " + type.getName() );

	    return null;
	    }

}
