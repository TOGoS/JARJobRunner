package togos.jarjobrunner;

public interface Task
{
	public void start();
	public void abort();
	public void join() throws InterruptedException;
}
