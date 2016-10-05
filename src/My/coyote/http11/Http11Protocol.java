package My.coyote.http11;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import My.coyote.ActionCode;
import My.coyote.ActionHook;
import My.coyote.Adapter;
import My.coyote.ProtocolHandler;
import My.tomcat.util.net.JIoEndpoint;
import My.tomcat.util.net.JIoEndpoint.Handler;

public class Http11Protocol implements ProtocolHandler{

	protected static My.juli.logging.Log log
    = My.juli.logging.LogFactory.getLog(Http11Protocol.class);
	
	
	
	// ------------------ Constructor ---------------------
	
	public Http11Protocol() {
		setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
        setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        //setServerSoTimeout(Constants.DEFAULT_SERVER_SOCKET_TIMEOUT);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
	}
	
	
	
	
	// -------------------- Fields ----------------------
	
	protected Http11ConnectionHandler cHandler = new Http11ConnectionHandler(this);
	
	protected JIoEndpoint endpoint = new JIoEndpoint();
	
	
	
	// ----------- ProtocolHandler Implementation -----------
	
	protected HashMap<String, Object> attributes = new HashMap<String, Object>();

	
	 /**
     * Pass config info
     */
    public void setAttribute(String name, Object value) {
    	attributes.put(name, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public Iterator getAttributeNames() {
        return attributes.keySet().iterator();
    }
    
    
    /**
     * The adapter, used to call the connector.
     */
    protected Adapter adapter;
    public void setAdapter(Adapter adapter) { this.adapter = adapter; }
    public Adapter getAdapter() { return adapter; }
    
    
    public void init() throws Exception {
    	
    	endpoint.setName(getName());
        endpoint.setHandler(cHandler);
    	
        try {
            endpoint.init();
        } catch (Exception ex) {
            log.error("http11protocol.endpoint.initerror");
            throw ex;
        }
    }
    
    
    public void start() throws Exception {
    	
    	 try {
             endpoint.start();
         } catch (Exception ex) {
             log.error("http11protocol.endpoint.starterror");
             throw ex;
         }
    }
    
    
    public String getName() {
        String encodedAddr = "";
       /* if (getAddress() != null) {
            encodedAddr = "" + getAddress();
            if (encodedAddr.startsWith("/"))
                encodedAddr = encodedAddr.substring(1);
            encodedAddr = URLEncoder.encode(encodedAddr) + "-";
        }*/
        return ("http-" + encodedAddr + endpoint.getPort());
    }
    
    
	// -------------------------- Properties --------------------
    
    /**
     * Processor cache.
     */
    protected int processorCache = -1;
    public int getProcessorCache() { return this.processorCache; }
    public void setProcessorCache(int processorCache) { this.processorCache = processorCache; }

    protected int socketBuffer = 9000;
    public int getSocketBuffer() { return socketBuffer; }
    public void setSocketBuffer(int socketBuffer) { this.socketBuffer = socketBuffer; }

    /**
     * Maximum number of requests which can be performed over a keepalive 
     * connection. The default is the same as for Apache HTTP Server.
     */
    protected int maxKeepAliveRequests = 100;
    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }
    public void setMaxKeepAliveRequests(int mkar) { maxKeepAliveRequests = mkar; }

    protected int keepAliveTimeout = -1;
    public int getKeepAliveTimeout() { return keepAliveTimeout; }
    public void setKeepAliveTimeout(int timeout) { keepAliveTimeout = timeout; }

    protected int timeout = 300000;
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    protected int maxSavePostSize = 4 * 1024;
    public int getMaxSavePostSize() { return maxSavePostSize; }
    public void setMaxSavePostSize(int valueI) { maxSavePostSize = valueI; }

    protected int maxHttpHeaderSize = 8 * 1024;
    public int getMaxHttpHeaderSize() { return maxHttpHeaderSize; }
    public void setMaxHttpHeaderSize(int valueI) { maxHttpHeaderSize = valueI; }

    protected String server;
    public void setServer( String server ) { this.server = server; }
    public String getServer() { return server; }
    
    
    /**
     * If true, the regular socket timeout will be used for the full duration
     * of the connection.
     */
    protected boolean disableUploadTimeout = true;
    public boolean getDisableUploadTimeout() { return disableUploadTimeout; }
    public void setDisableUploadTimeout(boolean isDisabled) { disableUploadTimeout = isDisabled; }

    
    
    public int getMaxThreads() { return endpoint.getMaxThreads(); }
    public void setMaxThreads(int maxThreads) { endpoint.setMaxThreads(maxThreads); }

    public int getThreadPriority() { return endpoint.getThreadPriority(); }
    public void setThreadPriority(int threadPriority) { endpoint.setThreadPriority(threadPriority); }

    public int getBacklog() { return endpoint.getBacklog(); }
    public void setBacklog(int backlog) { endpoint.setBacklog(backlog); }

    public int getPort() { return endpoint.getPort(); }
    public void setPort(int port) { endpoint.setPort(port); }

    public InetAddress getAddress() { return endpoint.getAddress(); }
    public void setAddress(InetAddress ia) { endpoint.setAddress(ia); }

    public boolean getTcpNoDelay() { return endpoint.getTcpNoDelay(); }
    public void setTcpNoDelay(boolean tcpNoDelay) { endpoint.setTcpNoDelay(tcpNoDelay); }

    public int getSoLinger() { return endpoint.getSoLinger(); }
    public void setSoLinger(int soLinger) { endpoint.setSoLinger(soLinger); }

    public int getSoTimeout() { return endpoint.getSoTimeout(); }
    public void setSoTimeout(int soTimeout) { endpoint.setSoTimeout(soTimeout); }

    public int getUnlockTimeout() { return endpoint.getUnlockTimeout(); }
    public void setUnlockTimeout(int unlockTimeout) {
        endpoint.setUnlockTimeout(unlockTimeout);
    }
    
    
    /**
     * Return the Keep-Alive policy for the connection.
     */
    public boolean getKeepAlive() {
        return ((maxKeepAliveRequests != 0) && (maxKeepAliveRequests != 1));
    }
    
    /**
     * Set the keep-alive policy for this connection.
     */
    public void setKeepAlive(boolean keepAlive) {
        if (!keepAlive) {
            setMaxKeepAliveRequests(1);
        }
    }
    
	// ------  Http11ConnectionHandler Inner Class ------
    
    protected static class Http11ConnectionHandler implements Handler {
    	
    	protected Http11Protocol proto;
        protected AtomicLong registerCount = new AtomicLong(0);
        
        
        protected ConcurrentLinkedQueue<Http11Processor> recycledProcessors = 
            new ConcurrentLinkedQueue<Http11Processor>() {
            protected AtomicInteger size = new AtomicInteger(0);
            public boolean offer(Http11Processor processor) {
                boolean offer = (proto.processorCache == -1) ? true : (size.get() < proto.processorCache);
                //avoid over growing our cache or add after we have stopped
                boolean result = false;
                if ( offer ) {
                    result = super.offer(processor);
                    if ( result ) {
                        size.incrementAndGet();
                    }
                }
                
                return result;
            }
            
            public Http11Processor poll() {
                Http11Processor result = super.poll();
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
        
        
        Http11ConnectionHandler(Http11Protocol proto) {
            this.proto = proto;
        }
        
        public boolean process(Socket socket) {
        	Http11Processor processor = null;
        	try {
        		
        		if (processor == null) {
                    processor = createProcessor();
                }
        		
        		if (processor instanceof ActionHook) {
        			((ActionHook) processor).action(ActionCode.ACTION_START, null);
        		}
        		
        		processor.process(socket);
                return false;
        	}
        	catch(java.net.SocketException e) {
        		
        	}catch (java.io.IOException e) {
        		
        	}
        	finally {
        		if (processor instanceof ActionHook) {
                    ((ActionHook) processor).action(ActionCode.ACTION_STOP, null);
                }
        		
        		recycledProcessors.offer(processor);
        	}
        	
        	return false;
        }
        
        
        protected Http11Processor createProcessor() {
        	 Http11Processor processor =
                new Http11Processor(proto.maxHttpHeaderSize, proto.endpoint);
        	
        	 processor.setAdapter(proto.adapter);
             processor.setMaxKeepAliveRequests(proto.maxKeepAliveRequests);
             processor.setKeepAliveTimeout(proto.keepAliveTimeout);
             processor.setTimeout(proto.timeout);
             processor.setDisableUploadTimeout(proto.disableUploadTimeout);
             processor.setSocketBuffer(proto.socketBuffer);
             processor.setServer(proto.server);
             
             return processor;
        }
        
        
    }
}
