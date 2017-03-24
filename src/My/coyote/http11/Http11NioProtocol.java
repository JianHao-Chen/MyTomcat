package My.coyote.http11;

import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import My.coyote.Adapter;
import My.coyote.ProtocolHandler;
import My.tomcat.util.net.NioChannel;
import My.tomcat.util.net.NioEndpoint;
import My.tomcat.util.net.NioEndpoint.Handler;
import My.tomcat.util.net.SocketStatus;

public class Http11NioProtocol implements ProtocolHandler{

	protected static My.juli.logging.Log log
    = My.juli.logging.LogFactory.getLog(Http11NioProtocol.class);
	
	
	//	------------ constructor ------------------------
	public Http11NioProtocol() {
		
		cHandler = new Http11ConnectionHandler( this );
		
	}
	
	
	
	// ------------ instance variables ------------
	
	protected NioEndpoint ep= new NioEndpoint();
	
	private Adapter adapter;
	
	private Http11ConnectionHandler cHandler;
	 
	protected Hashtable attributes = new Hashtable();
	
	
	private int maxHttpHeaderSize = 8 * 1024;
	
	private int maxKeepAliveRequests=100; // as in Apache HTTPD server
	private int timeout = 300000;   // 5 minutes as in Apache HTTPD server
	
	private int socketBuffer = 9000;
	
	// ------------------ properties -------------------------
	
	 /** Pass config info
     */
    public void setAttribute( String name, Object value ) {
        if( log.isTraceEnabled())
            log.trace("http11protocol.set attribute");

        attributes.put(name, value);
    }

    public Object getAttribute( String key ) {
        if( log.isTraceEnabled())
            log.trace("http11protocol.get attribute");
        
        return attributes.get(key);
    }

    public Iterator getAttributeNames() {
        return attributes.keySet().iterator();
    }
    
    
    
    
    // ---------------- ProtocolHandler Methods ----------------
    
    /** Start the protocol
     */
    public void init() throws Exception {
    	
    	ep.setName(getName());
        ep.setHandler(cHandler);
        
        ep.getSocketProperties().setRxBufSize(Math.max(ep.getSocketProperties().getRxBufSize(),getMaxHttpHeaderSize()));
        ep.getSocketProperties().setTxBufSize(Math.max(ep.getSocketProperties().getTxBufSize(),getMaxHttpHeaderSize()));
        
        try {
        	ep.init();
        }catch (Exception ex) {
        	log.error("http11protocol.endpoint.initerror");
        }
        
        if(log.isInfoEnabled())
            log.info("http11protocol.init");
    }
    
    
    public void start() throws Exception {
    	 try {
             ep.start();
         } catch (Exception ex) {
             log.error("http11protocol.endpoint.starterror");
             throw ex;
         }
         if(log.isInfoEnabled())
             log.info("http11protocol.start");
    }
    
    
    
    
	// --------------------- Connector Methods ---------------------
    
    /**
     * Set the associated adapter.
     *
     * @param adapter the new adapter
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }
    
    /**
     * Get the associated adapter.
     *
     * @return the associated adapter
     */
    public Adapter getAdapter() {
        return adapter;
    }
    
    
    
    
    // -------------------- Tcp setup --------------------
    
    public int getPort() {
        return ep.getPort();
    }

    public void setPort( int port ) {
        ep.setPort(port);
        setAttribute("port", "" + port);
    }
    
    public InetAddress getAddress() {
        return ep.getAddress();
    }

    public void setAddress(InetAddress ia) {
        ep.setAddress( ia );
        setAttribute("address", "" + ia);
    }
    
    
    public String getName() {
        String encodedAddr = "";
        if (getAddress() != null) {
            encodedAddr = "" + getAddress();
            if (encodedAddr.startsWith("/"))
                encodedAddr = encodedAddr.substring(1);
         //   encodedAddr = URLEncoder.encode(encodedAddr) + "-";
        }
        return ("http-" + encodedAddr + ep.getPort());
    }
    
    
    public int getMaxHttpHeaderSize() {
        return maxHttpHeaderSize;
    }

    public void setMaxHttpHeaderSize(int valueI) {
        maxHttpHeaderSize = valueI;
        setAttribute("maxHttpHeaderSize", "" + valueI);
    }
    

	// --------------------  Connection handler --------------------
	 
	 static class Http11ConnectionHandler implements Handler{
		 
		 protected Http11NioProtocol proto;
		 protected static int count = 0;
		 
		 protected ConcurrentHashMap<NioChannel, Http11NioProcessor> connections =
	            new ConcurrentHashMap<NioChannel, Http11NioProcessor>();
		 
		 
		 protected ConcurrentLinkedQueue<Http11NioProcessor> recycledProcessors = new ConcurrentLinkedQueue<Http11NioProcessor>() {
			 
			 protected AtomicInteger size = new AtomicInteger(0);
			 
			 
		 };
		 
		 
		 Http11ConnectionHandler(Http11NioProtocol proto) {
			 this.proto = proto;
	     }
		 
		@Override
		public SocketState process(NioChannel socket) {
			Http11NioProcessor processor = null;
			try {
				processor = connections.remove(socket);
				
				if (processor == null) {
					// get from recycledProcessors
				}
				
				if (processor == null) {
					processor = createProcessor();
				}
				
				
				SocketState state = processor.process(socket);
				
				if (state == SocketState.LONG) {
					
					// In the middle of processing a request/response. Keep the
                    // socket associated with the processor.
				    /*
				     * 表示处理request或者response之间，例如数据还没有接收或者发送完，
				     * 那么据需保持processor，并继续在poller上面注册 
				     */
					connections.put(socket, processor);
					socket.getPoller().add(socket);
				}
				else if (state == SocketState.OPEN) {
					/*
					 * 表示request已经搞定，但是还是keepalive的，那么回收processor对象，
					 * 然后再将channel注册到poller上面去poller继续等待
					 */
				}
				else {
					// Connection closed. OK to recycle the processor.
					release(socket, processor);
				}
				
				return state;
				
			}catch (java.net.SocketException e) {
				
			}catch (java.io.IOException e) {
				
			}
			
			
			return SocketState.CLOSED;
		}
		
		
		public Http11NioProcessor createProcessor() {
			
			Http11NioProcessor processor = new Http11NioProcessor(
		              proto.ep.getSocketProperties().getRxBufSize(),
		              proto.ep.getSocketProperties().getTxBufSize(), 
		              proto.maxHttpHeaderSize,
		              proto.ep);
			
			processor.setAdapter(proto.adapter);
			
			processor.setMaxKeepAliveRequests(proto.maxKeepAliveRequests);
			processor.setTimeout(proto.timeout);
			
			processor.setSocketBuffer(proto.socketBuffer);
			
			return processor;
			
		}
		
		
		
		/**
         * Use this only if the processor is not available, otherwise use
         * {@link #release(NioChannel, Http11NioProcessor).
         */
        public void release(NioChannel socket) {
        	Http11NioProcessor result = connections.remove(socket);
            if ( result != null ) {
                result.recycle();
                recycledProcessors.offer(result);
            }
        }
		
		
		public void release(NioChannel socket, Http11NioProcessor processor) {
			connections.remove(socket);
            processor.recycle();
            recycledProcessors.offer(processor);
		}
		
		
		
		@Override
		public SocketState event(NioChannel socket, SocketStatus status) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void releaseCaches() {
			// TODO Auto-generated method stub
			
		}
		
		 
		 
	 }
}





