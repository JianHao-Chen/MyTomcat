package My.catalina.tribes.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import My.catalina.tribes.ChannelReceiver;
import My.catalina.tribes.MessageListener;
import My.catalina.tribes.io.ListenCallback;

public abstract class ReceiverBase 
	implements ChannelReceiver, ListenCallback, RxTaskPool.TaskCreator{

	protected static My.juli.logging.Log log = 
		My.juli.logging.LogFactory.getLog(ReceiverBase.class);
	
	private MessageListener listener;
	private String host = "auto";
    private InetAddress bind;
    private int port  = 4000;
    private int rxBufSize = 43800;
    private int txBufSize = 25188;
    private boolean listen = false;
    private RxTaskPool pool;
    private boolean direct = true;
    private long tcpSelectorTimeout = 5000;
    
    //how many times to search for an available socket
    private int autoBind = 100;
    private int maxThreads = Integer.MAX_VALUE;
    private int minThreads = 6;
    private int maxTasks = 100;
    private int minTasks = 10;
    private boolean tcpNoDelay = true;
    private boolean soKeepAlive = false;
    private boolean ooBInline = true;
    private boolean soReuseAddress = true;
    private boolean soLingerOn = true;
    private int soLingerTime = 3;
    private int soTrafficClass = 0x04 | 0x08 | 0x010;
    private int timeout = 3000; //3 seconds
    private boolean useBufferPool = true;
    
    private ExecutorService executor;
    
    public ReceiverBase() {
    }
    
    public void start() throws IOException {
        if ( executor == null ) {
            executor = new ThreadPoolExecutor(minThreads,maxThreads,60,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
        }
    }
    
    public void stop() {
        if ( executor != null ) executor.shutdownNow();//ignore left overs
        executor = null;
    }
    
    
    /**
     * getMessageListener
     *
     * @return MessageListener
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public MessageListener getMessageListener() {
        return listener;
    }
    /**
     * setMessageListener
     *
     * @param listener MessageListener
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }
    
    
    /**
     * @return Returns the bind.
     */
    public InetAddress getBind() {
    	if (bind == null) {
    		try {
    			if ("auto".equals(host)) {
    				host = java.net.InetAddress.getLocalHost().getHostAddress();
    			}
    			bind = java.net.InetAddress.getByName(host);
    		}
    		catch (IOException ioe) {
                log.error("Failed bind replication listener on address:"+ host, ioe);
            }
    	}
    	return bind;
    }
    
    /**
     * recursive bind to find the next available port
     */
    protected int bind(ServerSocket socket, int portstart, int retries) throws IOException {
    	
    	InetSocketAddress addr = null;
    	while ( retries > 0 ) {
    		try {
    			addr = new InetSocketAddress(getBind(), portstart);
    			socket.bind(addr);
    			setPort(portstart);
    			log.info("Receiver Server Socket bound to:"+addr);
    			return 0;
    		}
    		catch ( IOException x) {
    			retries--;
                if ( retries <= 0 ) {
                    log.info("Unable to bind server socket to:"+addr+" throwing error.");
                    throw x;
                }
                portstart++;
                try {Thread.sleep(25);}catch( InterruptedException ti){Thread.currentThread().interrupted();}
                retries = bind(socket,portstart,retries);
    		}
    	}
    	return retries;
    }
    
    
    
    
    public int getMaxThreads() {
        return maxThreads;
    }
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
    
    
    public int getMinThreads() {
        return minThreads;
    }
    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }
    
    public void setPool(RxTaskPool pool) {
        this.pool = pool;
    }
    public RxTaskPool getTaskPool() {
        return pool;
    }
    
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getAutoBind() {
        return autoBind;
    }
    public void setAutoBind(int autoBind) {
        this.autoBind = autoBind;
        if ( this.autoBind <= 0 ) 
        	this.autoBind = 1;
    }
    
    
    
}
