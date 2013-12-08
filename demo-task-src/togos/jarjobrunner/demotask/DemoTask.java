package togos.jarjobrunner.demotask;

import java.util.Map;

import togos.jarjobrunner.Task;

public class DemoTask implements Task, Runnable
{
	protected Thread t;
	protected long startTime;
	protected String name;
	
	public DemoTask( Map<String, Object> context, String name ) {
		this.startTime = System.currentTimeMillis();
		this.name = name;
	}
	
	protected String getFullName() {
		return startTime + " " + name;
	}
	
	protected void log(String msg) {
		System.err.println(getFullName()+" "+msg);
	}
	
	@Override
	public synchronized void start() {
		if( t != null ) throw new RuntimeException("Already started!");
		
		t = new Thread(this);
		t.start();
	}
	
	@Override public void run() {
		while( !Thread.interrupted() ) {
			log("Getting stuff done!");
			try {
				Thread.sleep(2000);
			} catch( InterruptedException e ) {
				log("run() interrupted");
				Thread.currentThread().interrupt();
			}
		}
		log("run() exiting");
	}
	
	@Override public synchronized void abort() {
		if( t == null ) return;
		
		t.interrupt();
		try {
			t.join(2000);
		} catch( InterruptedException e ) {
			Thread.currentThread().interrupt();
		}
		if( t.isAlive() ) {
			log("join() never returned; killing");
			t.stop();
		}
		t = null;
	}
	
	@Override public synchronized void join() throws InterruptedException {
		if( t == null ) return;
		
		t.join();
	}
}
