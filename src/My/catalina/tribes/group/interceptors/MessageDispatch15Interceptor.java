package My.catalina.tribes.group.interceptors;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import My.catalina.tribes.util.TcclThreadFactory;

/**
 * 
 * Same implementation as the MessageDispatchInterceptor
 * except is ues an atomic long for the currentSize calculation
 * and uses a thread pool for message sending.
 */

public class MessageDispatch15Interceptor 
	extends MessageDispatchInterceptor{

	protected AtomicLong currentSize = new AtomicLong(0);
	protected ThreadPoolExecutor executor = null;
    protected int maxThreads = 10;
    protected int maxSpareThreads = 2;
    protected long keepAliveTime = 5000;
    protected LinkedBlockingQueue<Runnable> runnablequeue = 
    										new LinkedBlockingQueue<Runnable>();

	
    
    
    
    public void startQueue() {
    	if ( run ) 
    		return;
    	
    	executor = new ThreadPoolExecutor(maxSpareThreads, maxThreads,
                keepAliveTime, TimeUnit.MILLISECONDS, runnablequeue,
                new TcclThreadFactory());
    	
    	run = true;
    }
}
