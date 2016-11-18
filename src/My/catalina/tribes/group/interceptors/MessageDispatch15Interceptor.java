package My.catalina.tribes.group.interceptors;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;
import My.catalina.tribes.group.InterceptorPayload;
import My.catalina.tribes.transport.bio.util.LinkObject;
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

	
    public long getCurrentSize() {
        return currentSize.get();
    }
    
    public long addAndGetCurrentSize(long inc) {
        return currentSize.addAndGet(inc);
    }

    public long setAndGetCurrentSize(long value) {
        currentSize.set(value);
        return value;
    }
    
    
    public boolean addToQueue(ChannelMessage msg, Member[] destination, InterceptorPayload payload) {
    	final LinkObject obj = new LinkObject(msg,destination,payload);
    	Runnable r = new Runnable() {
            public void run() {
                sendAsyncData(obj);
            }
        };
        executor.execute(r);
        return true;
    }
    
    
    public void startQueue() {
    	if ( run ) 
    		return;
    	
    	executor = new ThreadPoolExecutor(maxSpareThreads, maxThreads,
                keepAliveTime, TimeUnit.MILLISECONDS, runnablequeue,
                new TcclThreadFactory());
    	
    	run = true;
    }
}
