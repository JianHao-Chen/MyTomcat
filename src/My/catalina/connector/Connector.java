package My.catalina.connector;

import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import My.catalina.Container;
import My.catalina.Host;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Service;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.IntrospectionUtils;
import My.tomcat.util.http.mapper.Mapper;
import My.tomcat.util.modeler.Registry;
import My.catalina.core.StandardEngine;
import My.catalina.util.LifecycleSupport;
import My.coyote.Adapter;
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
     * The request scheme that will be set on all requests received
     * through this connector.
     */
    protected String scheme = "http";
    
    
    /**
     * Coyote protocol handler.
     */
    protected ProtocolHandler protocolHandler = null;
    
    
    
    /**
     * Maximum size of a POST which will be automatically parsed by the
     * container. 2MB by default.
     */
    protected int maxPostSize = 2 * 1024 * 1024;
    
    
    
    
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
    
    
    /**
     * Coyote adapter.
     */
    protected Adapter adapter = null;
    
    
    /**
     * Mapper.
     */
    protected Mapper mapper = new Mapper();


    /**
     * Mapper listener.
     */
    protected MapperListener mapperListener = new MapperListener(mapper, this);
    
    
    /**
     * URI encoding.
     */
    protected String URIEncoding = null;
    
    
    
    protected static HashMap replacements = new HashMap();
    static {
        replacements.put("acceptCount", "backlog");
        replacements.put("connectionLinger", "soLinger");
        replacements.put("connectionTimeout", "soTimeout");
        replacements.put("connectionUploadTimeout", "timeout");
        replacements.put("clientAuth", "clientauth");
        replacements.put("keystoreFile", "keystore");
        replacements.put("randomFile", "randomfile");
        replacements.put("rootFile", "rootfile");
        replacements.put("keystorePass", "keypass");
        replacements.put("keystoreType", "keytype");
        replacements.put("sslProtocol", "protocol");
        replacements.put("sslProtocols", "protocols");
    }
    
    
    
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
     * Return the port number on which we listen for requests.
     */
    public int getPort() {
        return (this.port);
    }
    
    /**
     * Set the port number on which we listen for requests.
     *
     * @param port The new port number
     */
    public void setPort(int port) {
        this.port = port;
        setProperty("port", String.valueOf(port));
    }
    
    
    
    /**
     * Set a configured property.
     */
    public boolean setProperty(String name, String value) {
    	
    	return IntrospectionUtils.setProperty(protocolHandler, name, value);
    }
    
    
    /**
     * Return a configured property.
     */
    public Object getProperty(String name) {
        String repl = name;
        if (replacements.get(name) != null) {
            repl = (String) replacements.get(name);
        }
        return IntrospectionUtils.getProperty(protocolHandler, repl);
    }

    
    
    /**
     * Return the mapper.
     */
    public Mapper getMapper() {

        return (mapper);

    }
    
    
    /**
     * Return the scheme that will be assigned to requests received
     * through this connector.  Default value is "http".
     */
    public String getScheme() {

        return (this.scheme);

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
    
    
    
    /**
     * Return the "empty session path" flag.
     */
    public boolean getEmptySessionPath() {

        return (this.emptySessionPath);

    }


    /**
     * Set the "empty session path" flag.
     *
     * @param emptySessionPath The new "empty session path" flag value
     */
    public void setEmptySessionPath(boolean emptySessionPath) {

        this.emptySessionPath = emptySessionPath;
        setProperty("emptySessionPath", String.valueOf(emptySessionPath));
    }
    
    
    
    /**
     * Return the maximum size of a POST which will be automatically
     * parsed by the container.
     */
    public int getMaxPostSize() {

        return (maxPostSize);

    }


    /**
     * Set the maximum size of a POST which will be automatically
     * parsed by the container.
     *
     * @param maxPostSize The new maximum size in bytes of a POST which will
     * be automatically parsed by the container
     */
    public void setMaxPostSize(int maxPostSize) {

        this.maxPostSize = maxPostSize;
    }
    
    
	// ---------------------- Public Methods ---------------------
    
    /**
     * Create (or allocate) and return a Request object suitable for
     * specifying the contents of a Request to the responsible Container.
     */
    public Request createRequest() {
    	
    	Request request = new Request();
    	request.setConnector(this);
    	return (request);
    }
    
    /**
     * Create (or allocate) and return a Response object suitable for
     * receiving the contents of a Response from the responsible Container.
     */
    public Response createResponse() {

        Response response = new Response();
        response.setConnector(this);
        return (response);

    }
    
    
    
    /**
     * Return the character encoding to be used for the URI.
     */
    public String getURIEncoding() {

        return (this.URIEncoding);

    }


    /**
     * Set the URI encoding to be used for the URI.
     *
     * @param URIEncoding The new URI character encoding.
     */
    public void setURIEncoding(String URIEncoding) {

        this.URIEncoding = URIEncoding;
        setProperty("uRIEncoding", URIEncoding);

    }
    
    
    
    
    
    
	// ----------------- Lifecycle Methods -----------------


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
	
	
	// -------------------- JMX registration  --------------------
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;
    ObjectName controller;

    public ObjectName getController() {
        return controller;
    }

    public void setController(ObjectName controller) {
        this.controller = controller;
    }

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
        try {
            if( started ) {
                stop();
            }
        } catch( Throwable t ) {
            log.error( "Unregistering - can't stop", t);
        }
    }

    protected void findContainer() {
        try {
            // Register to the service
            ObjectName parentName=new ObjectName( domain + ":" +
                    "type=Service");

            if(log.isDebugEnabled())
                log.debug("Adding to " + parentName );
            if( mserver.isRegistered(parentName )) {
                mserver.invoke(parentName, "addConnector", new Object[] { this },
                        new String[] {"org.apache.catalina.connector.Connector"});
                // As a side effect we'll get the container field set
                // Also initialize will be called
                //return;
            }
            // XXX Go directly to the Engine
            // initialize(); - is called by addConnector
            ObjectName engName=new ObjectName( domain + ":" + "type=Engine");
            if( mserver.isRegistered(engName )) {
                Object obj=mserver.getAttribute(engName, "managedResource");
                if(log.isDebugEnabled())
                      log.debug("Found engine " + obj + " " + obj.getClass());
                container=(Container)obj;

                // Internal initialize - we now have the Engine
                initialize();

                if(log.isDebugEnabled())
                    log.debug("Initialized");
                // As a side effect we'll get the container field set
                // Also initialize will be called
                return;
            }
        } catch( Exception ex ) {
            log.error( "Error finding container " + ex);
        }
    }
    
    
    protected ObjectName createObjectName(String domain, String type)
    		throws MalformedObjectNameException {
		Object addressObj = getProperty("address");
		
		StringBuilder sb = new StringBuilder(domain);
		sb.append(":type=");
		sb.append(type);
		sb.append(",port=");
		sb.append(getPort());
		if (addressObj != null) {
		    String address = addressObj.toString();
		    if (address.length() > 0) {
		        sb.append(",address=");
		        sb.append(ObjectName.quote(address));
		    }
		}
		ObjectName _oname = new ObjectName(sb.toString());
		return _oname;
	}
	
	
	
	
	

	@Override
	public void start() throws LifecycleException {
		if( !initialized )
            initialize();
		
		 // Validate and update our current state
        if (started ) {
            if(log.isInfoEnabled())
                log.info("coyoteConnector.alreadyStarted");
            return;
        }
        
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;
        
        try {
            protocolHandler.start();
        }catch (Exception e) {
        	String msg = "Connector.protocolHandler Start Failed";
        	throw new LifecycleException(msg);
        }
        
        
        if( this.domain != null ) {
        	
        	mapperListener.setDomain( domain );
        	
        	mapperListener.init();
        	
        	try {
                ObjectName mapperOname = createObjectName(this.domain,"Mapper");
                if (log.isDebugEnabled())
                    log.debug(
                            "coyoteConnector.MapperRegistration");
                Registry.getRegistry(null, null).registerComponent
                    (mapper, mapperOname, "Mapper");
            } catch (Exception ex) {
                log.error("coyoteConnector.protocolRegistrationFailed");
            }
        	
        }

        
        
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
    	 
    	 
    	 if( oname == null && (container instanceof StandardEngine)) {
             try {
                 // we are loaded directly, via API - and no name was given to us
                 StandardEngine cb=(StandardEngine)container;
                 oname = createObjectName(cb.getName(), "Connector");
                 Registry.getRegistry(null, null)
                     .registerComponent(this, oname, null);
                 controller=oname;
                 
                 domain = oname.getDomain();
                 
             } catch (Exception e) {
                 log.error( "Error registering connector ", e);
             }
             if(log.isDebugEnabled())
                 log.debug("Creating name for connector " + oname);
         }
    	 
    	 
    	// Initializa adapter
    	adapter = new CoyoteAdapter(this);
    	
    	protocolHandler.setAdapter(adapter);
    	
    	
    	 try {
             protocolHandler.init();
         } catch (Exception e) {
             throw new LifecycleException
                ("coyoteConnector.protocolHandlerInitializationFailed");
         }
    }
    
    
    
}
