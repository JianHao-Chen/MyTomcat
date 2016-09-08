package My.coyote.http11;

import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Iterator;

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
		 
		 
		 Http11ConnectionHandler(Http11NioProtocol proto) {
			 this.proto = proto;
	     }
		 
		@Override
		public SocketState process(NioChannel socket) {
			// TODO Auto-generated method stub
			return null;
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
		@Override
		public void release(NioChannel socket) {
			// TODO Auto-generated method stub
			
		}
		 
		 
	 }
}





