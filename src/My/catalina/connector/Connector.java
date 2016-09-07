package My.catalina.connector;

import My.catalina.Container;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Service;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.catalina.util.LifecycleSupport;
import My.coyote.ProtocolHandler;

/**
 * Implementation of a Coyote connector for Tomcat 5.x.
 */

public class Connector implements Lifecycle
{
	
	private static Log log = LogFactory.getLog(Connector.class);
	

	// --------------------- Constructor ---------------------
	public Connector()throws Exception {
		this(null);
	}
	
	public Connector(String protocol) throws Exception {
		
		setProtocol(protocol);
		
		// Instantiate protocol handler
		try{
			Class clazz = Class.forName(protocolHandlerClassName);
			this.protocolHandler = (ProtocolHandler) clazz.newInstance();
		}catch (Exception e) {
			 log.error
              ("coyoteConnector.protocolHandlerInstantiationFailed", e);
		}
		
	}
	
	
	
	
	// -------------------- Instance Variables --------------------
	 /**
     * The <code>Service</code> we are associated with (if any).
     */
    protected Service service = null;


    /**
     * Do we allow TRACE ?
     */
    protected boolean allowTrace = false;
    
    /**
     * The Container used for processing requests received by this Connector.
     */
    protected Container container = null;
    
    
    /**
     * Use "/" as path for session cookies ?
     */
    protected boolean emptySessionPath = false;


    /**
     * The "enable DNS lookups" flag for this Connector.
     */
    protected boolean enableLookups = false;
    
    
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    
    /**
     * The port number on which we listen for requests.
     */
    protected int port = 0;
    
    
    
    /**
     * Coyote protocol handler.
     */
    protected ProtocolHandler protocolHandler = null;
    
    
    /**
     * Coyote Protocol handler class name.
     * Defaults to the Coyote HTTP/1.1 protocolHandler.
     */
    protected String protocolHandlerClassName =
        "My.coyote.http11.Http11Protocol";
    
    /**
     * Has this component been initialized yet?
     */
    protected boolean initialized = false;


    /**
     * Has this component been started yet?
     */
    protected boolean started = false;


    /**
     * The shutdown signal to our background thread
     */
    protected boolean stopped = false;
    
    
    /**
     * The background thread.
     */
    protected Thread thread = null;
    
    
 // ----------------------- Properties -----------------------
    /**
     * the <code>Service</code> with which we are associated (if any).
     */
    public Service getService() {
        return (this.service);
    }
    public void setService(Service service) {
        this.service = service;
    }
    
    
    /**
     * Is this connector available for processing requests?
     */
    public boolean isAvailable() {
        return (started);  
    }
    
    
    /**
     * Return the Container used for processing requests received by this
     * Connector.
     */
    public Container getContainer() {
        if( container==null ) {
           //handle this latter, now assume it will be not null
        }
        return (container);
    }
    
    /**
     * Set the Container used for processing requests received by this
     * Connector.
     *
     * @param container The new Container to use
     */
    public void setContainer(Container container) {
        this.container = container;
    }
    
    
    /**
     * Set the Coyote protocol which will be used by the connector.
     *
     * @param protocol The Coyote protocol name
     */
    public void setProtocol(String protocol) {
    	
    	// currently, just implements NIO protocol
    	setProtocolHandlerClassName(protocol);
    }
    
    
    
    /**
     * Set the class name of the Coyote protocol handler which will be used
     * by the connector.
     *
     * @param protocolHandlerClassName The new class name
     */
    public void setProtocolHandlerClassName(String protocolHandlerClassName) {

        this.protocolHandlerClassName = protocolHandlerClassName;

    }
    


	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LifecycleListener[] findLifecycleListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
     * Initialize this connector (create ServerSocket here!)
     */
    public void initialize() throws LifecycleException {
    	
    	 if (initialized) {
             if(log.isInfoEnabled())
                 log.info("coyoteConnector.alreadyInitialized");
            return;
         }
    	 
    	 this.initialized = true;
    	 
    	
    }
    
    
    
}
