package My.catalina.tribes.io;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferPool15Impl implements BufferPool.BufferPoolAPI{

	protected int maxSize;
    protected AtomicInteger size = new AtomicInteger(0);
    protected ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
    
    public void setMaxSize(int bytes) {
        this.maxSize = bytes;
    }
    
    public XByteBuffer getBuffer(int minSize, boolean discard) {
    	XByteBuffer buffer = (XByteBuffer)queue.poll();
    	
    	if ( buffer != null ) 
    		size.addAndGet(-buffer.getCapacity());
    	
    	if ( buffer == null ) 
    		buffer = new XByteBuffer(minSize,discard);
    	else if ( buffer.getCapacity() <= minSize ) 
    		buffer.expand(minSize);
    	
    	buffer.setDiscard(discard);
    	buffer.reset();
    	return buffer;
    }
    
    public void returnBuffer(XByteBuffer buffer) {
    	if ( (size.get() + buffer.getCapacity()) <= maxSize ) {
            size.addAndGet(buffer.getCapacity());
            queue.offer(buffer);
        }
    }
    
    public void clear() {
        queue.clear();
        size.set(0);
    }

    public int getMaxSize() {
        return maxSize;
    }
}
