package My.catalina.tribes.transport.nio;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.ChannelReceiver;
import My.catalina.tribes.io.ListenCallback;
import My.catalina.tribes.io.ObjectReader;
import My.catalina.tribes.transport.AbstractRxTask;
import My.catalina.tribes.transport.ReceiverBase;
import My.catalina.tribes.transport.RxTaskPool;

public class NioReceiver extends ReceiverBase implements Runnable, ChannelReceiver, ListenCallback{

	protected static My.juli.logging.Log log = 
		My.juli.logging.LogFactory.getLog(NioReceiver.class);
	
	private Selector selector = null;
    private ServerSocketChannel serverChannel = null;
    
    protected LinkedList events = new LinkedList();
    
    public NioReceiver() {
    }
    
    
    /**
     * start cluster receiver
     * @throws Exception
     * @see org.apache.catalina.tribes.ClusterReceiver#start()
     */
    public void start() throws IOException {
    	super.start();
    	try {
    		setPool(new RxTaskPool(getMaxThreads(),getMinThreads(),this));
    	}
    	catch (Exception x) {
    		log.fatal("ThreadPool can initilzed. Listener not started", x);
            if ( x instanceof IOException ) throw (IOException)x;
            else throw new IOException(x.getMessage());
    	}
    	
    	try {
    		getBind();
    		bind();
    		Thread t = new Thread(this, "NioReceiver");
    		t.setDaemon(true);
            t.start();
    	}
    	catch (Exception x) {
            log.fatal("Unable to start cluster receiver", x);
            if ( x instanceof IOException ) throw (IOException)x;
            else throw new IOException(x.getMessage());
        }
    }
    
    
    public AbstractRxTask createRxTask() {
    	NioReplicationTask thread = new NioReplicationTask(this,this);
        thread.setUseBufferPool(this.getUseBufferPool());
        thread.setRxBufSize(getRxBufSize());
        thread.setOptions(getWorkerThreadOptions());
        return thread;
    }
    
    
    protected void bind() throws IOException {
    	// allocate an unbound server socket channel
        serverChannel = ServerSocketChannel.open();
        
        // Get the associated ServerSocket to bind it with
        ServerSocket serverSocket = serverChannel.socket();
        
        // create a new Selector for use below
        selector = Selector.open();
        
        // set the port the server channel will listen to
        bind(serverSocket,getPort(),getAutoBind());
        
        // set non-blocking mode for the listening socket
        serverChannel.configureBlocking(false);
        
        // register the ServerSocketChannel with the Selector
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    
    
    
    public void events() {
    	if ( events.size() == 0 ) 
    		return;
    }
    
    
    
    
    
    protected long lastCheck = System.currentTimeMillis();
    protected void socketTimeouts() {
    	long now = System.currentTimeMillis();
    	if ( (now-lastCheck) < getSelectorTimeout() ) 
    		return;
    	
    	//timeout
        Selector tmpsel = selector;
        Set keys =  (isListening()&&tmpsel!=null)?tmpsel.keys():null;
        if ( keys == null ) return;
        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
        	SelectionKey key = (SelectionKey) iter.next();
        	try {
        		
        		if ( key.interestOps() == 0 ) {
        			
        		}
        	}
        	catch ( CancelledKeyException ckx ) {
        		
        	}
        }
        lastCheck = System.currentTimeMillis();
        
    }
	
	
	@Override
	public void heartbeat() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void messageDataReceived(ChannelMessage data) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
     * get data from channel and store in byte array
     * send it to cluster
     */
	protected void listen() throws Exception {
		if (doListen()) {
			log.warn("ServerSocketChannel already started");
            return;
		}
		setListen(true);
		
		while (doListen() && selector != null) {
			// this may block for a long time, upon return the
            // selected set contains keys of the ready channels
			try {
				events();
				socketTimeouts();
				int n = selector.select(getSelectorTimeout());
				if (n == 0) {
					continue; // nothing to do
				}
				
				 // get an iterator over the set of selected keys
                Iterator it = selector.selectedKeys().iterator();
                // look at each key in the selected set
                while (it.hasNext()) {
                	SelectionKey key = (SelectionKey) it.next();
                	// Is a new connection coming in?
                    if (key.isAcceptable()) {
                    	ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel channel = server.accept();
                        channel.socket().setReceiveBufferSize(getRxBufSize());
                        channel.socket().setSendBufferSize(getTxBufSize());
                        channel.socket().setTcpNoDelay(getTcpNoDelay());
                        channel.socket().setKeepAlive(getSoKeepAlive());
                        channel.socket().setOOBInline(getOoBInline());
                        channel.socket().setReuseAddress(getSoReuseAddress());
                        channel.socket().setSoLinger(getSoLingerOn(),getSoLingerTime());
                        channel.socket().setTrafficClass(getSoTrafficClass());
                        channel.socket().setSoTimeout(getTimeout());
                        Object attach = new ObjectReader(channel);
                        registerChannel(selector,
                                        channel,
                                        SelectionKey.OP_READ,
                                        attach);
                    }
                    // is there data to read on this channel?
                    if (key.isReadable()) {
                        readDataFromSocket(key);
                    } else {
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                    }
                    
                    // remove key from selected set, it's been handled
                    it.remove();
                }
			}
			catch (java.nio.channels.ClosedSelectorException cse) {
				
			}
			catch (java.nio.channels.CancelledKeyException nx) {
                log.warn("Replication client disconnected, error when polling key. Ignoring client.");
            }
			catch (Throwable x) {
				try {
                    log.error("Unable to process request in NioReceiver", x);
                }catch ( Throwable tx ) {
                    //in case an out of memory error, will affect the logging framework as well
                    tx.printStackTrace();
                }
			}
		}
	}
	
	
	
	/**
     * Register the given channel with the given selector for
     * the given operations of interest
     */
    protected void registerChannel(Selector selector,
                                   SelectableChannel channel,
                                   int ops,
                                   Object attach) throws Exception {
    	
    	
    	if (channel == null)return; // could happen
        // set the new channel non-blocking
        channel.configureBlocking(false);
        // register it with the selector
        channel.register(selector, ops, attach);
    }
    
    
    /**
     * Sample data handler method for a channel with data ready to read.
     * @param key A SelectionKey object associated with a channel
     *  determined by the selector to be ready for reading.  If the
     *  channel returns an EOF condition, it is closed here, which
     *  automatically invalidates the associated key.  The selector
     *  will then de-register the channel on the next select call.
     */
    protected void readDataFromSocket(SelectionKey key) throws Exception {
    	
    	NioReplicationTask task = (NioReplicationTask) getTaskPool().getRxTask();
    	if (task == null) {
    		// No threads/tasks available, do nothing, the selection
            // loop will keep calling this method until a
            // thread becomes available, the thread pool itself has a waiting mechanism
            // so we will not wait here.
            if (log.isDebugEnabled()) 
            	log.debug("No TcpReplicationThread available");
    	}
    	else {
    		 // invoking this wakes up the worker thread then returns
            //add task to thread pool
            task.serviceChannel(key);
            getExecutor().execute(task);
    	}
    }
	

	/**
     * Start thread and listen
     */
	public void run() {
		try {
			listen();
		} catch (Exception x) {
			log.error("Unable to run replication listener.", x);
		}
	}

}
