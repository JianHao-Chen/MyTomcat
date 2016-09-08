package My.tomcat.util.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * NIO tailored thread pool, providing the following services:
 * <ul>
 * <li>Socket acceptor thread</li>
 * <li>Socket poller thread</li>
 * <li>Worker threads pool</li>
 * </ul>
 */

public class NioEndpoint {

	
	// ----------------------- Fields -----------------------
	
	 /**
     * Track the initialization state of the endpoint.
     */
    protected boolean initialized = false;
    
    
    
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
     * Poller thread count.
     */
    protected int pollerThreadCount = Runtime.getRuntime().availableProcessors();
    public void setPollerThreadCount(int pollerThreadCount) { this.pollerThreadCount = pollerThreadCount; }
    public int getPollerThreadCount() { return pollerThreadCount; }
    
    
    
    
    
    
    
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
