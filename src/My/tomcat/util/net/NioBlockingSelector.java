package My.tomcat.util.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.MutableInteger;
import My.tomcat.util.net.NioEndpoint.KeyAttachment;

public class NioBlockingSelector {

	protected static Log log = LogFactory.getLog(NioBlockingSelector.class);
	
	private static int threadCounter = 0;
	
	protected Selector sharedSelector;

	protected BlockPoller poller;
	
	
	
	 public void open(Selector selector) {
	     sharedSelector = selector;
	     poller = new BlockPoller();
	     poller.selector = sharedSelector;
	     poller.setDaemon(true);
	     poller.setName("NioBlockingSelector.BlockPoller-"+(++threadCounter));
	     poller.start();
	 }
	
	
	
	 /**
     * Performs a blocking write using the bytebuffer for data to be written
     * If the <code>selector</code> parameter is null, then it will perform a busy write that could
     * take up a lot of CPU cycles.
     * @param buf ByteBuffer - the buffer containing the data, we will write as long as <code>(buf.hasRemaining()==true)</code>
     * @param socket SocketChannel - the socket to write data to
     * @param writeTimeout long - the timeout for this write operation in milliseconds, -1 means no timeout
     * @return int - returns the number of bytes written
     * @throws EOFException if write returns -1
     * @throws SocketTimeoutException if the write times out
     * @throws IOException if an IO Exception occurs in the underlying socket logic
     */
	public int write(ByteBuffer buf, NioChannel socket, long writeTimeout,MutableInteger lastWrite) throws IOException {
		
		SelectionKey key = socket.getIOChannel().keyFor(
							socket.getPoller().getSelector());
		
		if ( key == null ) 
			throw new IOException("Key no longer registered");
		
		KeyReference reference = new KeyReference();
		
		KeyAttachment att = (KeyAttachment) key.attachment();
		
		int written = 0;
		boolean timedout = false;
		
		int keycount = 1; //assume we can write
		long time = System.currentTimeMillis(); //start the timeout timer
		
		try {
			 while ( (!timedout) && buf.hasRemaining()) {
				 if (keycount > 0) { //only write if we were registered for a write
					 int cnt = socket.write(buf); //write the data
					 lastWrite.set(cnt);
					 
					 if (cnt == -1)
	                        throw new EOFException();
					 
					 written += cnt;
					 
					 if (cnt > 0) {
	                        time = System.currentTimeMillis(); //reset our timeout timer
	                        continue; //we successfully wrote, try again without a selector
	                    }
				 }
				 try {  //go into here, means cnt==0
				     /*
				      *  如果是来到这里,是因为SocketChannel不能把数据写到TCP send缓冲队列
				      *  里面,于是使用 BlockPoller 来监听可写事件的发生,并触发CountDownLatch
				      *  的countDown()方法,使阻塞于写的线程被唤醒。
				      */
				     if(att.getWriteLatch()==null || att.getWriteLatch().getCount()==0)
				         att.startWriteLatch(1);
				     
				     poller.add(att,SelectionKey.OP_WRITE,reference);
				     att.awaitWriteLatch(writeTimeout,TimeUnit.MILLISECONDS);
				 }
				 catch (InterruptedException ignore) {
	                 Thread.interrupted();
	             }
				 if ( att.getWriteLatch()!=null && att.getWriteLatch().getCount()> 0) {
				     // go into here means : we got interrupted, but we haven't 
				     // received notification from the poller.
				     keycount = 0;
				 }
				 else {
				     //latch countdown has happened
				     keycount = 1;
				     att.resetWriteLatch();
				 }
				 
				 if (writeTimeout > 0 && (keycount == 0))
				     timedout = (System.currentTimeMillis() - time) >= writeTimeout;

				 
			 } // while
			 
			 if (timedout) 
				 throw new SocketTimeoutException();
			 
			 
		}finally {
			// ...
		}
		return written;
	}
	
	
	
	protected class BlockPoller extends Thread {
	    protected boolean run = true;
	    protected Selector selector = null;
	    protected ConcurrentLinkedQueue<Runnable> events = new ConcurrentLinkedQueue<Runnable>();
	    
	    protected AtomicInteger wakeupCounter = new AtomicInteger(0);
	    
	    public void disable(){
	        run = false; selector.wakeup();
	    }
	    
	    public void cancelKey(final SelectionKey key) {
	        Runnable r = new Runnable() {
                public void run() {
                    key.cancel();
                }
            };
            events.offer(r);
            wakeup();
	    }
	    
	    public void wakeup() {
            if (wakeupCounter.addAndGet(1)==0) selector.wakeup();
        }
	    
	    public void cancel(SelectionKey sk, KeyAttachment key, int ops){
	        if (sk!=null) {
                sk.cancel();
                sk.attach(null);
                if (SelectionKey.OP_WRITE==(ops&SelectionKey.OP_WRITE)) countDown(key.getWriteLatch());
                if (SelectionKey.OP_READ==(ops&SelectionKey.OP_READ))countDown(key.getReadLatch());
            }
	    }
	    
	    
	    
	    public void add(final KeyAttachment key, final int ops, final KeyReference ref) {
	        Runnable r = new Runnable() {
	            public void run() {
	                if ( key == null ) return;
	                NioChannel nch = key.getChannel();
	                if ( nch == null ) return;
	                SocketChannel ch = nch.getIOChannel();
	                if ( ch == null ) return;
	                SelectionKey sk = ch.keyFor(selector);
	                try {
	                    if (sk == null) {
	                        sk = ch.register(selector, ops, key);
	                        ref.key = sk;
	                    }
	                    else if (!sk.isValid()) {
	                        
	                    }
	                    else{
	                        
	                    }
	                }
	                catch (CancelledKeyException cx) {
                        cancel(sk,key,ops);
                    }catch (ClosedChannelException cx) {
                        cancel(sk,key,ops);
                    }
	                
	            }
	        };
	        events.offer(r);
	        wakeup();
	    }
	    
	    
	    public boolean events() {
	        boolean result = false;
            Runnable r = null;
            result = (events.size() > 0);
            while ( (r = events.poll()) != null ) {
                r.run();
                result = true;
            }
            return result;
	    }
	    
	    
	    public void run() {
	        while (run) {
	            try {
	                events();
	                int keyCount = 0;
	                try {
	                    int i = wakeupCounter.get();
	                    if (i>0)
	                        keyCount = selector.selectNow();
	                    else {
	                        wakeupCounter.set(-1);
                            keyCount = selector.select(1000);
	                    }
	                    wakeupCounter.set(0);
	                }
	                catch ( NullPointerException x ) {
	                    //
	                }
	                catch ( CancelledKeyException x ) {
	                    //
	                }
	                
	                
	                Iterator<SelectionKey> iterator = keyCount > 0 ? selector.selectedKeys().iterator() : null;
	                
	                // Walk through the collection of ready keys and dispatch
                    // any active event.
	                while (run && iterator != null && iterator.hasNext()) {
	                    SelectionKey sk = iterator.next();
                        KeyAttachment attachment = (KeyAttachment)sk.attachment();
                        try {
                            attachment.access();
                            iterator.remove(); ;
                            sk.interestOps(sk.interestOps() & (~sk.readyOps()));
                            if ( sk.isReadable() ) {
                                countDown(attachment.getReadLatch());
                            }
                            if (sk.isWritable()) {
                                countDown(attachment.getWriteLatch());
                            }
                        }
                        catch (CancelledKeyException ckx) {
                            
                        }
	                }
	               
	            }
	            catch ( Throwable t ) {
	                
	            }
	        }
	        events.clear();
	    }
	    
	    public void countDown(CountDownLatch latch) {
	        if ( latch == null ) return;
            latch.countDown();
	    }
	    
	}
	
	
	public class KeyReference {
        SelectionKey key = null;
        
        @Override
        public void finalize() {
            if (key!=null && key.isValid()) {
                log.warn("Possible key leak, cancelling key in the finalizer.");
                try {
                	key.cancel();
                }
                catch (Exception ignore){}
            }
            key = null;
        }
    }
}
