package My.tomcat.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;


/**
 * NIO tailored thread pool, providing the following services:
 * <ul>
 * <li>Socket acceptor thread</li>
 * <li>Socket poller thread</li>
 * <li>Worker threads pool</li>
 * </ul>
 */

public class NioEndpoint {

	protected static Log log = LogFactory.getLog(NioEndpoint.class);
	
	
	// ----------------------- Fields -----------------------
	
	 /**
     * Track the initialization state of the endpoint.
     */
    protected boolean initialized = false;
    
    
    /**
     * Running state of the endpoint.
     */
    protected volatile boolean running = false;
    
    /**
     * Will be set to true whenever the endpoint is paused.
     */
    protected volatile boolean paused = false;
    
    
    
    /**
     * Sequence number used to generate thread names.
     */
    protected int sequence = 0;
    
    
    protected NioSelectorPool selectorPool = new NioSelectorPool();
    
    
    /**
     * Server socket "pointer".
     */
    protected ServerSocketChannel serverSock = null;
    
    
    /**
     * 
     */
    protected volatile CountDownLatch stopLatch = null;
    
    
    
    /**
     * Bytebuffer cache, each channel holds a set of buffers
     */
    // implements this latter.
    protected ConcurrentLinkedQueue<NioChannel> nioChannels = new ConcurrentLinkedQueue<NioChannel>(){
    	
    	protected AtomicInteger size = new AtomicInteger(0);
    	protected AtomicInteger bytes = new AtomicInteger(0);
    	
    	public boolean offer(NioChannel socket) {
    		
    		return false;
    	}
	};
	
	
	// ------------------- Properties -------------------
	/**
     * Server socket port.
     */
    protected int port;
    public int getPort() { return port; }
    public void setPort(int port ) { this.port=port; }

    
    
    /**
     * Address for the server socket.
     */
    protected InetAddress address;
    public InetAddress getAddress() { return address; }
    public void setAddress(InetAddress address) { this.address = address; }
	
	
    /**
     * Handling of accepted sockets.
     */
    protected Handler handler = null;
    public void setHandler(Handler handler ) { this.handler = handler; }
    public Handler getHandler() { return handler; }

	
    /**
     * Allows the server developer to specify the backlog that
     * should be used for server sockets. By default, this value
     * is 100.
     */
    protected int backlog = 100;
    public void setBacklog(int backlog) { if (backlog > 0) this.backlog = backlog; }
    public int getBacklog() { return backlog; }
	
    
    /**
     * Name of the thread pool, which will be used for naming child threads.
     */
    protected String name = "TP";
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    
    
    
    
    protected SocketProperties socketProperties = new SocketProperties();
    
    public SocketProperties getSocketProperties() {
        return socketProperties;
    }
    
    
    
    /**
     * Priority of the worker threads.
     */
    protected int threadPriority = Thread.NORM_PRIORITY;
    public void setThreadPriority(int threadPriority) { this.threadPriority = threadPriority; }
    public int getThreadPriority() { return threadPriority; }
    
    
    
    /**
     * Poller thread count.
     */
//    protected int pollerThreadCount = Runtime.getRuntime().availableProcessors();
    protected int pollerThreadCount = 1;
    public void setPollerThreadCount(int pollerThreadCount) { this.pollerThreadCount = pollerThreadCount; }
    public int getPollerThreadCount() { return pollerThreadCount; }
    
    
    /**
     * Acceptor thread count.
     */
    protected int acceptorThreadCount = 1;
    public void setAcceptorThreadCount(int acceptorThreadCount) { this.acceptorThreadCount = acceptorThreadCount; }
    public int getAcceptorThreadCount() { return acceptorThreadCount; }
    
    
    
    /**
     * External Executor based thread pool.
     */
    protected Executor executor = null;
    public void setExecutor(Executor executor) { this.executor = executor; }
    public Executor getExecutor() { return executor; }
    
    protected boolean useExecutor = true;
    public void setUseExecutor(boolean useexec) { useExecutor = useexec;}
    public boolean getUseExecutor() { return useExecutor || (executor!=null);}
    
    
    /**
     * The socket poller.
     */
    protected Poller[] pollers = null;
    protected AtomicInteger pollerRotater = new AtomicInteger(0);
    
    
    
    
	// -------------- Protected Methods -------------- 
    
    /**
     * Process the specified connection.
     */
    protected boolean setSocketOptions(SocketChannel socket) {
    	// Process the connection
    	
    	try {
    		socket.configureBlocking(false);
            Socket sock = socket.socket();
            socketProperties.setProperties(sock);
            
            NioChannel channel = nioChannels.poll();
            if ( channel == null ) {
            	
            	NioBufferHandler bufhandler = new NioBufferHandler(socketProperties.getAppReadBufSize(),
                        socketProperties.getAppWriteBufSize(),
                        socketProperties.getDirectBuffer());
            	
            	channel = new NioChannel(socket, bufhandler);
            }
            else {  
            	
            }
            
    	}
    	
    	return true;
    }
    
    
    
    
    
    
    
    
    // ------------------ Public Lifecycle Methods ------------------
    
    /**
     * Initialize the endpoint.
     */
    public void init()
        throws Exception {
    	
    	 if (initialized)
             return;

    	 serverSock = ServerSocketChannel.open();
    	 serverSock.socket().setPerformancePreferences(
    			 			socketProperties.getPerformanceConnectionTime(),
    			 			socketProperties.getPerformanceLatency(),
    			 			socketProperties.getPerformanceBandwidth());
    	 
    	 InetSocketAddress addr = (address!=null?new InetSocketAddress(address,port):new InetSocketAddress(port));
    	 serverSock.socket().bind(addr,backlog);
    	 serverSock.configureBlocking(true); 
    	 
    	 serverSock.socket().setSoTimeout(getSocketProperties().getSoTimeout());
    	 
    	 stopLatch = new CountDownLatch(pollerThreadCount);
    	 
    	 selectorPool.open();
    	 
    	 initialized = true;
    }
    
    
    
    /**
     * Start the NIO endpoint, creating acceptor, poller threads.
     */
    public void start()
    	throws Exception {
    	
    	// Initialize socket if not done before
        if (!initialized) {
        	init();
        }
        
        // running is volatile !!
        if (!running) {
        	 running = true;
             paused = false;
             
             // Create worker collection
             if (getUseExecutor()) {
            	 if ( executor == null ) {
            		 TaskQueue taskqueue = new TaskQueue();
            		 TaskThreadFactory tf = new TaskThreadFactory(getName() + "-exec-");
            		 executor = new ThreadPoolExecutor(5, 200, 60, TimeUnit.SECONDS,taskqueue, tf);
            		 
            		 taskqueue.setProperties( (ThreadPoolExecutor) executor, this);
            		 
            	 }
             }
             
             
             // Start poller threads
             pollers = new Poller[getPollerThreadCount()];
             for (int i=0; i<pollers.length; i++) {
            	 
            	 pollers[i] = new Poller();
            	 Thread pollerThread = new Thread(pollers[i], getName() + "-ClientPoller-"+i);
            	 pollerThread.setPriority(threadPriority);
                 pollerThread.setDaemon(true);
                 pollerThread.start();
             }
             
             
             // Start acceptor threads
             for (int i = 0; i < acceptorThreadCount; i++) {
            	 Thread acceptorThread = new Thread(new Acceptor(), getName() + "-Acceptor-" + i);
                 acceptorThread.setPriority(threadPriority);
                 acceptorThread.setDaemon(true);
                 acceptorThread.start();
             }
             
             
        }
    }
    
    
	// -------------------- Acceptor Inner Class --------------------
    /**
     * Server socket acceptor thread.
     */
    protected class Acceptor implements Runnable {
    	
    	 /**
         * The background thread that listens for incoming TCP/IP connections and
         * hands them off to an appropriate processor.
         */
    	public void run() {
    		
    		 // Loop until we receive a shutdown command
            while (running) {
            	
            	// Loop if endpoint is paused
            	while (paused) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            	
            	
            	try {
            		// Accept the next incoming connection from the server socket
            		SocketChannel socket = serverSock.accept();

            		// Hand this socket off to an appropriate processor
            		
            		// check running and paused again.
            		if ( running && (!paused) && socket != null ) {
            			
            		}
            	}
            	catch (SocketTimeoutException sx) {
            		log.error("SocketTimeoutException when serverSock.accept()");
            	}
            	catch (IOException e) {
            		log.error("IOException when serverSock.accept()");
				}
            }
    	}
    }
    
    
    
    
    
    
    
    
	// --------------- Poller Inner Classes ---------------
    
    /**
     * 
     * PollerEvent, cacheable object for poller events to avoid GC
     */
    public class PollerEvent implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    
    
    /**
     * Poller class.
     */
    public class Poller implements Runnable {
    	
    	protected Selector selector;
    	protected ConcurrentLinkedQueue<Runnable> events = new ConcurrentLinkedQueue<Runnable>();
    	
    	
    	protected AtomicLong wakeupCounter = new AtomicLong(0l);
    	
    	
    	public Poller() throws IOException {
            this.selector = Selector.open();
        }
    	
    	public Selector getSelector() { return selector;}
    	
    	
    	public void addEvent(Runnable event) {
             events.offer(event);
             if ( wakeupCounter.incrementAndGet() == 0 ) 
            	 selector.wakeup();
        }
    	 
    	 
    	public void wakeup() {
             selector.wakeup();
        }
    	

        public void run() {
        	
        }
    	
    	
    }
    
    
    
    
    
    
    
    
    
    // -------------- TaskQueue Inner Class --------------
    public static class TaskQueue extends LinkedBlockingQueue<Runnable> {
    	
    	 ThreadPoolExecutor parent = null;
         NioEndpoint endpoint = null;
         
         public TaskQueue() {
             super();
         }

         public TaskQueue(int initialCapacity) {
             super(initialCapacity);
         }
         
         public TaskQueue(Collection<? extends Runnable> c) {
             super(c);
         }
         
         public void setProperties(ThreadPoolExecutor tp, NioEndpoint ep) {
             parent = tp;
             this.endpoint = ep;
         }
         
         public boolean offer(Runnable o) {
        	 return false;
         }
    }
    
    
    
	// ------------------  ThreadFactory Inner Class ------------------
    class TaskThreadFactory implements ThreadFactory {
    	
    	 final ThreadGroup group;
         final AtomicInteger threadNumber = new AtomicInteger(1);
         final String namePrefix;
	
         TaskThreadFactory(String namePrefix) {
        	 
        	 group = Thread.currentThread().getThreadGroup();
        	 this.namePrefix = namePrefix;
         }
         
         
         
     	@Override
     	public Thread newThread(Runnable r) {
     		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
     		t.setDaemon(true);
            t.setPriority(getThreadPriority());
            return t;
		}
         
    }
    
    
	
	// ------------ Handler Inner Interface ------------
	
	 /**
     * Bare bones interface used for socket processing. Per thread data is to be
     * stored in the ThreadWithAttributes extra folders, or alternately in
     * thread local fields.
     */
	public interface Handler {
        public enum SocketState {
            OPEN, CLOSED, LONG
        }
        public SocketState process(NioChannel socket);
        public SocketState event(NioChannel socket, SocketStatus status);
        public void releaseCaches();
        public void release(NioChannel socket);
    }
	
	
}
