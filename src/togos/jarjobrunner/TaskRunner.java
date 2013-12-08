package togos.jarjobrunner;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class TaskRunner implements Runnable
{
	final BlobOracle bo;
	
	public TaskRunner( BlobOracle bo ) {
		this.bo = bo;
	}
	
	URL resolveJar( String urn ) {
		try {
			return bo.getBlobUrl(urn, urn.replace(":","-") + ".jar");
		} catch( MalformedURLException e ) {
			throw new RuntimeException(e);
		}
	}
	
	public Task getTask( TaskDef taskDef ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		URL jarUrl = resolveJar( taskDef.jarUri );
		ClassLoader cl = new URLClassLoader( new URL[]{ jarUrl } );
		
		Class<?>[] constructorArgClasses = new Class[taskDef.additionalConstructorArguments.length+1];
		Object[] constructorArguments = new Object[taskDef.additionalConstructorArguments.length+1];
		constructorArgClasses[0] = Map.class;
		for( int i=0; i<taskDef.additionalConstructorArguments.length; ++i ) {
			Object o = taskDef.additionalConstructorArguments[i];
			Class<?> c = o instanceof Number ? Number.class : o instanceof String ? String.class : null;
			if( c == null ) throw new InstantiationException("Couldn't determine generic class for argument "+i+", which is a "+o.getClass());
			constructorArgClasses[i+1] = c;
			constructorArguments[i+1] = o;
		}
		
		return Task.class.cast(cl.loadClass(taskDef.className).getConstructor(constructorArgClasses).newInstance(constructorArguments));
	}
	
	protected void runTaskUntilInterrupted( TaskDef td ) {
		Task t;
		try {
			t = getTask(td);
		} catch( Exception e ) {
			System.err.println("Error loading task: "+td);
			e.printStackTrace();
			try {
				Thread.sleep(1000);
			} catch( InterruptedException ie ) {
				Thread.currentThread().interrupt();
			}
			return;
		}
		
		t.start();
		try {
			t.join();
		} catch( InterruptedException e ) {
			System.err.println("Aborting task...");
			t.abort();
		}
	}
	
	protected Thread t;
	protected TaskDef currentTaskDef = null;
	protected boolean keepRunning = true;
	
	public void run() {
		while( keepRunning ) {
			TaskDef taskDef = null;
			synchronized(this) {
				while( keepRunning && (taskDef = currentTaskDef) == null ) try {
					wait();
				} catch( InterruptedException e ) { }
				// Clear interrupt flag before exiting the synchronized block!
				Thread.interrupted();
			}
			
			if( taskDef != null ) {
				runTaskUntilInterrupted(taskDef);
			}
		}
	}
	
	protected void notifinterrupt() {
	}
	
	public synchronized void start() {
		if( t != null ) throw new RuntimeException("Already started!");
		
		t = new Thread(this);
		t.start();
	}
	
	public synchronized void abort() {
		keepRunning = false;
		
		if( t != null ) t.interrupt();
		
		this.notifyAll();
	}
	
	public synchronized void setTaskDef( TaskDef td ) {
		if( currentTaskDef == td ) return;
		if( currentTaskDef == null || !this.currentTaskDef.equals(td) ) {
			// Order is rather important, here!
			t.interrupt();
			this.currentTaskDef = td;
			this.notifyAll();
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		TaskRunner tr = new TaskRunner(new BlobOracle("http://robert.nuke24.net:8080/uri-res/"));
		tr.start();
		
		String jarUrn = "urn:bitprint:ORHU37M7DAUIKUP7QWFPFHS5GUZ5HOJV.7RIPEWGOA7EDY2Q2WE6XQCVP3XHWDT2J5XJ5C2A";
		
		tr.setTaskDef(new TaskDef(jarUrn, "togos.jarjobrunner.demotask.DemoTask", new Object[]{ "one" }));
		Thread.sleep(5000);
		System.err.println("Switching the task!");
		tr.setTaskDef(new TaskDef(jarUrn, "togos.jarjobrunner.demotask.DemoTask", new Object[]{ "one" }));
		Thread.sleep(5000);
		System.err.println("Switching the task more!");
		tr.setTaskDef(new TaskDef(jarUrn, "togos.jarjobrunner.demotask.DemoTask", new Object[]{ "two" }));
		Thread.sleep(5000);
		tr.abort();
	}
}
