package My.catalina.tribes.transport.nio;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.ChannelReceiver;
import My.catalina.tribes.io.ListenCallback;
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
	public AbstractRxTask createRxTask() {
		// TODO Auto-generated method stub
		return null;
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
			}
			catch (Throwable x) {
				
			}
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
