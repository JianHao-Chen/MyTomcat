package My.catalina.tribes.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TcclThreadFactory implements ThreadFactory{

	private static final AtomicInteger poolNumber = new AtomicInteger(1);
	
	private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
	
    public TcclThreadFactory() {
    	
    	group = Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }
	
	@Override
	public Thread newThread(Runnable r) {
		final Thread t = new Thread(group, r, namePrefix +
                threadNumber.getAndIncrement());
		
		return t;
	}

}
