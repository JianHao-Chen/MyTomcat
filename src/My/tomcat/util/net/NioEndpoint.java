package My.tomcat.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
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
import My.tomcat.util.net.NioEndpoint.Handler.SocketState;
import My.tomcat.util.net.SecureNioChannel.ApplicationBufferHandler;


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
	
	// ---------------------- Constants ----------------------
	
	public static final int OP_REGISTER = 0x100; //register interest op
    public static final int OP_CALLBACK = 0x200; //callback interest op
    
    
	
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
     * Keep track of how many threads are in use
     */
    protected AtomicInteger activeSocketProcessors = new AtomicInteger(0);
    
    
    /**
     * Cache for SocketProcessor objects
     */
    protected ConcurrentLinkedQueue<SocketProcessor> processorCache = new ConcurrentLinkedQueue<SocketProcessor>() {
    	
    	protected AtomicInteger size = new AtomicInteger(0);
    	
    	public boolean offer(SocketProcessor sc) {
    		sc.reset(null,null);
    		boolean offer = socketProperties.getProcessorCache()==-1?true:size.get()<socketProperties.getProcessorCache();
    		 if ( running && (!paused) && (offer) ) {
                 boolean result = super.offer(sc);
                 if ( result ) {
                     size.incrementAndGet();
                 }
                 return result;
             }
             else return false;
    	}
    	
    	
    	public SocketProcessor poll() {
            SocketProcessor result = super.poll();
            if ( result != null ) {
                size.decrementAndGet();
            }
            return result;
        }
    	
    	
    	public void clear() {
            super.clear();
            size.set(0);
        }
    };
    
    
    /**
     * Cache for poller events
     */
    protected ConcurrentLinkedQueue<PollerEvent> eventCache = new ConcurrentLinkedQueue<PollerEvent>() {
    	
    	protected AtomicInteger size = new AtomicInteger(0);
    	
    	public boolean offer(PollerEvent pe) {
    		// check if we should offer this object to queue,
    		// default size is 500.
    		boolean offer = socketProperties.getEventCache()==-1?true:size.get()<socketProperties.getEventCache();
    		
    		if ( running && (!paused) && (offer) ) {
    			boolean result = super.offer(pe);
    			
    			if ( result ) 
    				size.incrementAndGet();
    			
    			return result;
    		}
    		else
    			return false;
    	}
    	
    };
    
    
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
	
	
	/**
     * Cache for key attachment objects
     */
	protected ConcurrentLinkedQueue<KeyAttachment> keyCache = new ConcurrentLinkedQueue<KeyAttachment>() {
		
		protected AtomicInteger size = new AtomicInteger(0);
		
		 public boolean offer(KeyAttachment ka) {
			 
			 return false;
		 }
		 
		 public KeyAttachment poll() {
			 KeyAttachment result = super.poll();
			 if ( result != null ) {
	                size.decrementAndGet();
	            }
	         return result;
		 }
		 
		 public void clear() {
			 super.clear();
	         size.set(0);
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
     * Socket timeout.
     */
    public int getSoTimeout() { return socketProperties.getSoTimeout(); }
    public void setSoTimeout(int soTimeout) { socketProperties.setSoTimeout(soTimeout); }
    
    
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
    
    
    
    protected long selectorTimeout = 1000;
    public void setSelectorTimeout(long timeout){ this.selectorTimeout = timeout;}
    public long getSelectorTimeout(){ return this.selectorTimeout; }
    
    
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
     * Returns true if a worker thread is available for processing.
     * @return boolean
     */
    protected boolean isWorkerAvailable() {
    	if ( executor != null ) 
            return true;
        
        return false;
    }
    
    
    
    public NioSelectorPool getSelectorPool() {
        return selectorPool;
    }
    
    
    /**
     * The socket poller.
     */
    protected Poller[] pollers = null;
//    protected AtomicInteger pollerRotater = new AtomicInteger(0);
    
    /**
     *  currently, assume just have one poller, so just return pollers[0];
     */
    public Poller getPoller0() {
    	return pollers[0];
    }
    
    
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
            	
            	// normal tcp setup
            	NioBufferHandler bufhandler = 
            		new NioBufferHandler(
            						socketProperties.getAppReadBufSize(),
            						socketProperties.getAppWriteBufSize(),
            						socketProperties.getDirectBuffer()
            		);
            	
            	
            	channel = new NioChannel(socket, bufhandler);
            }
            else {  
            	
            }
            
            getPoller0().register(channel);
            
    	}catch (Throwable t) {
    		return false;
    	}
    	
    	return true;
    }
    
    
    
    /**
     * Process given socket.
     */
    protected boolean processSocket(NioChannel socket) {
        return processSocket(socket,null);
    }
    
    /**
     * Process given socket for an event.
     */
    protected boolean processSocket(NioChannel socket, SocketStatus status) {
        return processSocket(socket,status,true);
    }
    
    protected boolean processSocket(NioChannel socket, SocketStatus status, boolean dispatch) {
    	try {
    		KeyAttachment attachment = (KeyAttachment)socket.getAttachment(false);
    		if (executor == null) {
    			
    		}
    		else{
    			SocketProcessor socketProcessor = processorCache.poll();
    			if ( socketProcessor == null )
    				socketProcessor = new SocketProcessor(socket,status);
    			else
    				socketProcessor.reset(socket, status);
    			
    			if(dispatch) 
    				executor.execute(socketProcessor);
    			else
    				socketProcessor.run();
    		}
    		
    	}catch (Throwable t) {
    		
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
            			if (!setSocketOptions(socket)) {
            				
            				try {
                                socket.socket().close();
                                socket.close();
                            } catch (IOException ix) {
                                if (log.isDebugEnabled())
                                    log.debug("", ix);
                            }                          
            			}
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

    	protected NioChannel socket;
        protected int interestOps;
        protected KeyAttachment key;
        
        public PollerEvent(NioChannel ch, KeyAttachment k, int intOps) {
        	reset(ch, k, intOps);
        }
        
        
        public void reset() {
            reset(null, null, 0);
        }
        
        public void reset(NioChannel ch, KeyAttachment k, int intOps) {
            socket = ch;
            interestOps = intOps;
            key = k;
        }
        
        
		@Override
		public void run() {
			if ( interestOps == OP_REGISTER ) {
				try {
					socket.getIOChannel().register(
						key.getPoller().getSelector(), SelectionKey.OP_READ, key);
				}
				catch (Exception x) {
					
				}
			}else{
				final SelectionKey key = socket.getIOChannel().keyFor(
											socket.getPoller().getSelector());
				
				try {
					boolean cancel = false;
					if (key != null) {
						final KeyAttachment att = (KeyAttachment) key.attachment();
						if ( att!=null ) {
							att.access();//to prevent timeout
							int ops = key.interestOps() | interestOps;
                            att.interestOps(ops);
                            key.interestOps(ops);
						}
					}
					else {
                        cancel = true;
                    }
					
					if ( cancel )
						socket.getPoller().cancelledKey(key,SocketStatus.ERROR,false);
				}
				catch (CancelledKeyException ckx) {
					
				}
			}
			
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
    	
    	
    	 /**
         * Add specified socket and associated pool to the poller. The socket will
         * be added to a temporary array, and polled first after a maximum amount
         * of time equal to pollTime (in most cases, latency will be much lower,
         * however).
         *
         * @param socket to add to the poller
         */
        public void add(final NioChannel socket) {
        	add(socket,SelectionKey.OP_READ);
        }
        
        public void add(final NioChannel socket, final int interestOps) {
        	PollerEvent r = eventCache.poll();
        	if ( r==null) 
        		r = new PollerEvent(socket,null,interestOps);
        	else 
        		r.reset(socket,null,interestOps);
        	
        	addEvent(r);
        }
    	
    	
    	
    	public void addEvent(Runnable event) {
             events.offer(event);
             if ( wakeupCounter.incrementAndGet() == 0 ) 
            	 selector.wakeup();
        }
    	
    	public boolean events() {
    		boolean result = false;
    		Runnable r = null;
    		result = events.size()>0;
    		while((r = (Runnable)events.poll()) != null){
    			try{
    				r.run();
    				
    				// recycle the object
    				if(r instanceof PollerEvent)
    					((PollerEvent) r).reset();
    				
    				eventCache.offer((PollerEvent)r);
    			}
    			catch ( Throwable x ) {
    				
    			}
    		}
    		return result;
    	}
    	
    	
    	protected boolean processKey(SelectionKey sk, KeyAttachment attachment) {
    		boolean result = true;
    		
    		try {
    			// if endpoint is close....
    			if ( false ) {
    				
    			}
    			else if(sk.isValid() && attachment!=null){
    				//make sure we don't time out valid sockets
    				attachment.access();
    				NioChannel channel = attachment.getChannel();
    				if (sk.isReadable() || sk.isWritable() ) {
    					if ( isWorkerAvailable() ) {
    						
    						// clear the interest set
    						unreg(sk, attachment,sk.readyOps());
    						
    						boolean close = (!processSocket(channel));
    						
    					}
    				}
    				
    			}
    		}catch ( CancelledKeyException ckx ) {
    			
    		}
    		catch (Throwable t) {
    			
    		}
    		
    		return result;
    	}
    	
    	
    	public void register(final NioChannel socket){
    		
    		socket.setPoller(this);
    		KeyAttachment key = keyCache.poll();
    		final KeyAttachment ka = key!=null?key:new KeyAttachment();
    		ka.reset(this, socket, getSocketProperties().getSoTimeout());
    		ka.interestOps(SelectionKey.OP_READ);
    		
    		PollerEvent pollerEvent = eventCache.poll();
    		if ( pollerEvent==null) 
    			pollerEvent = new PollerEvent(socket,ka,OP_REGISTER);
    		else
    			pollerEvent.reset(socket, ka, OP_REGISTER);
    		
    		addEvent(pollerEvent);
    	}
    	
    	
    	 public void cancelledKey(SelectionKey key, SocketStatus status, boolean dispatch) {
    		 
    		 try {
    			 if ( key == null ) 
    				 return;//nothing to do
    			 
    			 KeyAttachment ka = (KeyAttachment) key.attachment();
    			 // handle comet event here , implements latter.
    			 
    			 
    			 key.attach(null);
    			 if (ka!=null) 
    				 handler.release(ka.getChannel());
    			 
    			 if (key.isValid()) 
    				 key.cancel();
    			 
    			 if (key.channel().isOpen()){ 
    				try {
    					 key.channel().close();
    				}
    			 	catch (Exception ignore){}
    			 }
    			 	
    			 try {
    			 	if (ka!=null) 
    			 		ka.channel.close(true);
    			 }
    			 catch (Exception ignore){}
    			 
    			 if (ka!=null) 
    				 ka.reset();
    			 
    		 }catch (Throwable e) {
    			// Ignore
    		 }
    	 }
    	
    	
    	
    	protected void unreg(SelectionKey sk, KeyAttachment attachment, int readyOps) {
            //must do this unreg, so that we don't have multiple threads messing with the socket
            reg(sk,attachment,sk.interestOps()& (~readyOps));
        }
    	
    	protected void reg(SelectionKey sk, KeyAttachment attachment, int intops) {
            sk.interestOps(intops); 
            attachment.interestOps(intops);
            //attachment.setCometOps(intops);
        }
    	 
    	 
    	public void wakeup() {
             selector.wakeup();
        }
    	

        public void run() {
        	
        	// Loop until we receive a shutdown command
        	while (running) {
        		
        		try {
        			// Loop if endpoint is paused
        			while (paused ) {
        				
        				try {
        					Thread.sleep(100);
        				}catch (InterruptedException e) {
                            // Ignore
                        }
        			}
        			
        			boolean hasEvents = false;
        			hasEvents = (hasEvents | events());
        			
        			int keyCount = 0;
        			
        			try {
        				 if (wakeupCounter.getAndSet(-1)>0) {
        					//if we are here, means we have other stuff to do
                             //do a non blocking select
                             keyCount = selector.selectNow();
        				 }else {
        					 // set to -1 is for when an event is added,
        					 // then the wakeUp will be invoke
                             keyCount = selector.select(selectorTimeout);
        				 }
        				 wakeupCounter.set(0);
        			}
        			catch (Throwable x) {
        				
        			}
        			
        			
        			//either we timed out or we woke up, process events first
                    if ( keyCount == 0 ) hasEvents = (hasEvents | events());
                    
                    Iterator iterator = keyCount > 0 ? selector.selectedKeys().iterator() : null;
                    
                    /* Walk through the collection of ready keys and 
                     * dispatch any active event.
                     */
                    while (iterator != null && iterator.hasNext()) {
                    	SelectionKey sk = (SelectionKey) iterator.next();
                    	KeyAttachment attachment = (KeyAttachment)sk.attachment();
                    	// Attachment may be null if another thread has called
                        // cancelledKey()
                    	if (attachment == null) {
                    		iterator.remove();
                    	}
                    	else{
                    		attachment.access();
                    		iterator.remove();
                    		processKey(sk, attachment);
                    	}
                    	
                    }
                    
        		}catch(Throwable x){
        			
        		}
        	}
        }
    	
    	
    }
    
    
    
	// --------------------- Key Attachment Class ---------------------
    public static class KeyAttachment {
    	
    	protected NioChannel channel = null;
    	public NioChannel getChannel() { return channel;}
        public void setChannel(NioChannel channel) { this.channel = channel;}   
    	
    	protected Poller poller = null;
    	public Poller getPoller() { return poller;}
        public void setPoller(Poller poller){this.poller = poller;}     
        
    	protected long timeout = -1;
    	public void setTimeout(long timeout) {this.timeout = timeout;}
        public long getTimeout() {return this.timeout;}
        
        protected int interestOps = 0;
        public int interestOps() { return interestOps;}
        public int interestOps(int ops) { this.interestOps  = ops; return ops; }
        
        
        protected long lastAccess = -1;
        public long getLastAccess() { return lastAccess; }
        public void access() { access(System.currentTimeMillis()); }
        public void access(long access) { lastAccess = access; }
        
        
        public void reset() {
            reset(null,null,-1);
        }
        
        public void reset(Poller poller, NioChannel channel, long soTimeout) {
    		 this.channel = channel;
             this.poller = poller;
             timeout = soTimeout;
             lastAccess = System.currentTimeMillis();
             
    	}
    }
    
    
    
	// ------------ SocketProcessor Inner Class ------------
    
    protected class SocketProcessor implements Runnable {
    	
    	protected NioChannel socket = null;
        protected SocketStatus status = null; 
        
        public SocketProcessor(NioChannel socket, SocketStatus status) {
            reset(socket,status);
        }
        
        public void reset(NioChannel socket, SocketStatus status) {
            this.socket = socket;
            this.status = status;
        }
        
        
        public void run() {
        	synchronized (socket) {
        		activeSocketProcessors.addAndGet(1);
        		
        		SelectionKey key = null;
        		try {
        			key = socket.getIOChannel().keyFor(socket.getPoller().getSelector());
        			
        			SocketState state = null;
        			boolean closed = false;
        			// Process the request from this socket
        			if(status==null){
        				state = handler.process(socket);
        				closed = state == SocketState.CLOSED;
        			}else{
        				;
        			}
        			
        			if (closed) {
        				// Close socket and pool
        				try {
        					KeyAttachment ka = null;
                            if (key!=null) {
                            	ka = (KeyAttachment) key.attachment();
                            	
                            	socket.getPoller().
                            		cancelledKey(key, SocketStatus.ERROR, false);
                            }
                            
                            if (socket!=null) 
                            	nioChannels.offer(socket);
                            
                            socket = null;
                            
                            if ( ka!=null ) 
                            	keyCache.offer(ka);
                            
                            ka = null;
        				}
        				catch ( Exception x ) {
        					
        				}
        			}
        		}catch(CancelledKeyException cx) {
        			
        		}
        		
        		catch (OutOfMemoryError oom) {
        			
        		}
        		
        		catch ( Throwable t ) {
        			
        		}
        		
        		finally {
        			socket = null;
                    status = null;
                    
                    //return to cache
                    processorCache.offer(this);
                    NioEndpoint.this.activeSocketProcessors.addAndGet(-1);
        		}
        		
        	}
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
    
    
	// ------------------ Application Buffer Handler ------------------
    public class NioBufferHandler implements ApplicationBufferHandler{
    	
    	protected ByteBuffer readbuf = null;
        protected ByteBuffer writebuf = null;
        
    	public NioBufferHandler(int readsize, int writesize, boolean direct) {
    		if ( direct ) {
                readbuf = ByteBuffer.allocateDirect(readsize);
                writebuf = ByteBuffer.allocateDirect(writesize);
            }else {
                readbuf = ByteBuffer.allocate(readsize);
                writebuf = ByteBuffer.allocate(writesize);
            }
    	}
    	
    	public ByteBuffer expand(ByteBuffer buffer, int remaining) {return buffer;}
        public ByteBuffer getReadBuffer() {return readbuf;}
        public ByteBuffer getWriteBuffer() {return writebuf;}
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
