package My.catalina.tribes.transport.nio;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

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
