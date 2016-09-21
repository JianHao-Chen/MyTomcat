package My.catalina.core;

import java.io.Serializable;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.modeler.Registry;

public class StandardContext 
	extends ContainerBase
	implements Context, Serializable{

	private static transient Log log = LogFactory.getLog(StandardContext.class);
	
	// --------------------- Constructors ---------------------
	
	 /**
     * Create a new StandardContext component with the default basic Valve.
     */
    public StandardContext() {

    	super();
        pipeline.setBasic(new StandardContextValve());
        broadcaster = new NotificationBroadcasterSupport();
    }
	
	
	
	// --------------------- Instance Variables ---------------------
	
	/**
     * Associated host name.
     */
    private String hostName;
    
    
    /**
     * The document root for this web application.
     */
    private String docBase = null;
    
    
    /**
     * The path to a file to save this Context information.
     */
    private String configFile = null;


    /**
     * The "correctly configured" flag for this Context.
     */
    private boolean configured = false;

    
    
    /**
     * The watched resources for this application.
     */
    private String watchedResources[] = new String[0];

    private final Object watchedResourcesLock = new Object();
    
    
    /**
     * The request processing pause flag (while reloading occurs)
     */
    private boolean paused = false;
    
    
    
    
    /**
     * The application available flag for this Context.
     */
    private boolean available = false;
    
    
    /**
     * The set of instantiated application lifecycle listener objects</code>.
     */
    private transient Object applicationLifecycleListenersObjects[] = 
        new Object[0];
    
    
    /**
     * The set of instantiated application event listener objects</code>.
     */
    private transient Object applicationEventListenersObjects[] = 
        new Object[0];
    
    
    
    /**
     * The broadcaster that sends j2ee notifications. 
     */
    private NotificationBroadcasterSupport broadcaster = null;
    
    
    /**
     * The notification sequence number.
     */
    private long sequenceNumber = 0;
    
    
    /** 
     * Name of the engine. If null, the domain is used.
     */ 
    private String engineName = null;
    private String j2EEApplication="none";
    private String j2EEServer="none";
    
    /**
     * The welcome files for this application.
     */
    private String welcomeFiles[] = new String[0];
    
	// ------------------- Context Properties -------------------
    

    /**
     * Return the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationEventListeners() {
        return (applicationEventListenersObjects);
    }



    /**
     * Store the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationEventListeners(Object listeners[]) {
        applicationEventListenersObjects = listeners;
    }


	 /**
     * Return the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationLifecycleListeners() {
        return (applicationLifecycleListenersObjects);
    }


	/**
     * Store the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationLifecycleListeners(Object listeners[]) {
        applicationLifecycleListenersObjects = listeners;
    }


    /**
     * Return the application available flag for this Context.
     */
    public boolean getAvailable() {

        return (this.available);

    }


    /**
     * Set the application available flag for this Context.
     *
     * @param available The new application available flag
     */
    public void setAvailable(boolean available) {

        boolean oldAvailable = this.available;
        this.available = available;
    }
    
    
    
    /**
     * Return an object which may be utilized for mapping to this component.
     */
    public Object getMappingObject() {
        return this;
    }
    
    /**
     * FIXME: Fooling introspection ...
     */
    public Context findMappingObject() {
        return (Context) getMappingObject();
    }
    
    
    /**
     * Return the naming resources associated with this web application.
     */
    public javax.naming.directory.DirContext getStaticResources() {

        return getResources();

    }


    /**
     * Return the naming resources associated with this web application.
     * FIXME: Fooling introspection ... 
     */
    public javax.naming.directory.DirContext findStaticResources() {

        return getResources();

    }
    
    
    /**
     * Return the "correctly configured" flag for this Context.
     */
    public boolean getConfigured() {

        return (this.configured);

    }
    
    
    /**
     * Set the "correctly configured" flag for this Context.  This can be
     * set to false by startup listeners that detect a fatal configuration
     * error to avoid the application from being made available.
     *
     * @param configured The new correctly configured flag
     */
    public void setConfigured(boolean configured) {

        boolean oldConfigured = this.configured;
        this.configured = configured; 
    }
    
    

    /**
     * Return the path to a file to save this Context information.
     */
    public String getConfigFile() {

        return (this.configFile);

    }


    /**
     * Set the path to a file to save this Context information.
     *
     * @param configFile The path to a file to save this Context information.
     */
    public void setConfigFile(String configFile) {

        this.configFile = configFile;
    }
    
    
    
    /**
     * Add a new watched resource to the set recognized by this Context.
     *
     * @param name New watched resource file name
     */
    public void addWatchedResource(String name) {

        synchronized (watchedResourcesLock) {
            String results[] = new String[watchedResources.length + 1];
            for (int i = 0; i < watchedResources.length; i++)
                results[i] = watchedResources[i];
            results[watchedResources.length] = name;
            watchedResources = results;
        }
        fireContainerEvent("addWatchedResource", name);

    }
    
    
    /**
     * Return the set of watched resources for this Context. If none are 
     * defined, a zero length array will be returned.
     */
    public String[] findWatchedResources() {
        synchronized (watchedResourcesLock) {
            return watchedResources;
        }
    }
    
    
    
    
    /**
     * Return the context path for this Context.
     */
    public String getPath() {

        return (getName());

    }

    
    /**
     * Set the context path for this Context.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The context path is used as the "name" of
     * a Context, because it must be unique.
     *
     * @param path The new context path
     */
    public void setPath(String path) {
        // XXX Use host in name
        setName(path);

    }
    
    
    /**
     * Return the request processing paused flag for this Context.
     */
    public boolean getPaused() {

        return (this.paused);

    }
    
    
    
    /**
     * Return the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getDocBase() {

        return (this.docBase);

    }


    /**
     * Set the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param docBase The new document root
     */
    public void setDocBase(String docBase) {

        this.docBase = docBase;

    }
    
    
    
    
    public String getJ2EEApplication() {
        return j2EEApplication;
    }

    public void setJ2EEApplication(String j2EEApplication) {
        this.j2EEApplication = j2EEApplication;
    }

    public String getJ2EEServer() {
        return j2EEServer;
    }

    public void setJ2EEServer(String j2EEServer) {
        this.j2EEServer = j2EEServer;
    }
    
    
    
    public void init() throws Exception {
    	
    	if( this.getParent() == null ) {
    		//;
    	}
    	
    	super.init();
    	
    	// Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(INIT_EVENT, null);
        
     // Send j2ee.state.starting notification 
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.starting", 
                                                        this.getObjectName(), 
                                                        sequenceNumber++);
            broadcaster.sendNotification(notification);
        }
    }
    
    
    /**
     * Start this Context component.
     */
    public synchronized void start() throws LifecycleException {
    	
    	if (started) 
    		return;
    	
    	 if( !initialized ) { 
    		 
    		 try {
    			 init();
    		 } catch( Exception ex ) {
    			 throw new LifecycleException("Error initializaing ", ex);
    		 }
    	 }
    	 
    	 
    	// Set JMX object name for proper pipeline registration
         preRegisterJMX();
    	 
         if ((oname != null) && 
                 (Registry.getRegistry(null, null).getMBeanServer().isRegistered(oname))) {
                 // As things depend on the JMX registration, the context
                 // must be reregistered again once properly initialized
                 Registry.getRegistry(null, null).unregisterComponent(oname);
          }


         // Notify our interested LifecycleListeners
         lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);
         
         
         setAvailable(false);
         setConfigured(false);
         
         boolean ok = true;
         
         
         
         /*
          *  realm part!
          *  
          *  implements latter
          */
         
         
         
         if (getLoader() == null) {
        	 
        	 // implement latter.
         }
         
         
         
         try {
        	 if (ok) {
        		 started = true;
        		 
        		 // Start our subordinate components
        		 
        		 
        		 
        		// Start our child containers, if any
        		Container children[] = findChildren();
        		for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof Lifecycle)
                        ((Lifecycle) children[i]).start();
                }
        		
        		
        		// Start the Valves in our pipeline (including the basic)
        		if (pipeline instanceof Lifecycle) {
        			((Lifecycle) pipeline).start();
        		}
        		
        		// Notify our interested LifecycleListeners
                lifecycle.fireLifecycleEvent(START_EVENT, null);
        		
        		 
        	 }
         }
         finally {
        	 ;
         }
         
         // JMX registration
         registerJMX();
  
    }
    
    private void registerJMX() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking for " + oname );
            }
            
            
            // for debug
            MBeanServer server = Registry.getRegistry(null, null)
                    .getMBeanServer();
            
            
            if(! Registry.getRegistry(null, null)
                .getMBeanServer().isRegistered(oname)) {
                controller = oname;
                Registry.getRegistry(null, null)
                    .registerComponent(this, oname, null);
                
                // Send j2ee.object.created notification 
                if (this.getObjectName() != null) {
                    Notification notification = new Notification(
                                                        "j2ee.object.created", 
                                                        this.getObjectName(), 
                                                        sequenceNumber++);
                    broadcaster.sendNotification(notification);
                }
            }
            Container children[] = findChildren();
            
            for (int i=0; children!=null && i<children.length; i++) {
                ((StandardWrapper)children[i]).registerJMX( this );
            }
            
        } catch (Exception ex) {
            if(log.isInfoEnabled())
                log.info("Error registering wrapper with jmx " + this + " " +
                    oname + " " + ex.toString(), ex );
        }
    }
    
    
    
    
    
    public ObjectName createObjectName(String hostDomain, ObjectName parentName)
    throws MalformedObjectNameException{
    	
		String onameStr;
		StandardHost hst=(StandardHost)getParent();
		
		String pathName=getName();
		String hostName=getParent().getName();
		String name= "//" + ((hostName==null)? "DEFAULT" : hostName) +
		        (("".equals(pathName))?"/":pathName );
		
		String suffix=",J2EEApplication=" +
		        getJ2EEApplication() + ",J2EEServer=" +
		        getJ2EEServer();
		
		onameStr="j2eeType=WebModule,name=" + name + suffix;
		if( log.isDebugEnabled())
		    log.debug("Registering " + onameStr + " for " + oname);
		
		// default case - no domain explictely set.
		if( getDomain() == null ) 
			domain=hst.getDomain();
		
		ObjectName oname=new ObjectName(getDomain() + ":" + onameStr);
		return oname;        
	}    
    
    
    private void preRegisterJMX() {
    	
    	try {
    		StandardHost host = (StandardHost) getParent();
    		if ((oname == null) 
                    || (oname.getKeyProperty("j2eeType") == null)) {
    			oname = createObjectName(host.getDomain(), host.getJmxName());
                controller = oname;
    		}
    	}catch(Exception ex) {
    		
    	}
    }
    
    
    
    
}
