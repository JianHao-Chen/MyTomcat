package My.catalina.tribes.transport;

import java.util.LinkedList;
import java.util.List;

public class RxTaskPool {

	/**
     * A very simple thread pool class.  The pool size is set at
     * construction time and remains fixed.  Threads are cycled
     * through a FIFO idle queue.
     */
	
	List idle = new LinkedList();
    List used = new LinkedList();
	
    Object mutex = new Object();
    boolean running = true;
    
    private static int counter = 1;
    private int maxTasks;
    private int minTasks;
    
    private TaskCreator creator = null;
    
    private static synchronized int inc() {
        return counter++;
    }
	
	public RxTaskPool (int maxTasks, int minTasks, TaskCreator creator) throws Exception {
		// fill up the pool with worker threads
        this.maxTasks = maxTasks;
        this.minTasks = minTasks;
        this.creator = creator;
	}
	
	
	
	/**
     * Find an idle worker thread, if any.  Could return null.
     */
    public AbstractRxTask getRxTask(){
    	
    	AbstractRxTask worker = null;
    	
    	synchronized (mutex) {
    		while ( worker == null && running ) {
    			if (idle.size() > 0) {
    				try {
                        worker = (AbstractRxTask) idle.remove(0);
                    } catch (java.util.NoSuchElementException x) {
                        //this means that there are no available workers
                        worker = null;
                    }
    			}
    			else if ( used.size() < this.maxTasks && creator != null) {
    				worker = creator.createRxTask();
    				configureTask(worker);
    			}
    			else {
    				//....
    			}
    		}
    		if ( worker != null ) 
    			used.add(worker);
    	}
    	return (worker);
    }
    
    
    protected void configureTask(AbstractRxTask task) {
    	synchronized (task) {
    		task.setTaskPool(this);
    	}
    }
	
	
	public static interface TaskCreator  {
        public AbstractRxTask createRxTask();
    }
}
