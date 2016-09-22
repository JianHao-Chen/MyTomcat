package My.catalina.core;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import My.catalina.Engine;
import My.catalina.LifecycleException;
import My.catalina.Service;
import My.catalina.Valve;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.modeler.Registry;

/**
 * Standard implementation of the <b>Engine</b> interface.  Each
 * child container must be a Host implementation to process the specific
 * fully qualified host name of that virtual host.
 */

public class StandardEngine extends ContainerBase implements Engine{

	private static Log log = LogFactory.getLog(StandardEngine.class);
	
	// --------------------- Constructors ---------------------
	
	public StandardEngine() {
		super();
        pipeline.setBasic(new StandardEngineValve());
	}
	
	
	 // ---------------- Instance Variables ----------------
	
	/**
     * Host name to use when no server host, or an unknown host,
     * is specified in the request.
     */
    private String defaultHost = null;
	
	
	 /**
     * The <code>Service</code> that owns this Engine, if any.
     */
    private Service service = null;
	
	
	
	 // ------------------- Properties -------------------
	
	 /**
     * Return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService() {

        return (this.service);

    }


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service) {
        this.service = service;
    }
    
    
    /**
     * Return the default host.
     */
    public String getDefaultHost() {

        return (defaultHost);

    }


    /**
     * Set the default host.
     *
     * @param host The new default host
     */
    public void setDefaultHost(String host) {

        String oldDefaultHost = this.defaultHost;
        if (host == null) {
            this.defaultHost = null;
        } else {
            this.defaultHost = host.toLowerCase();
        }

    }
    
    
    
    public void init() {
    	
    	if( initialized ) 
    		return;
    	
        initialized=true;
        
        if( oname==null ) {
        	
        	// for debug
            MBeanServer server = Registry.getRegistry(null, null)
                    .getMBeanServer();
        	
        	 try {
        		 
        		 if (domain==null) {
                     domain=getName();
                 }
        		 
        		 oname=new ObjectName(domain + ":type=Engine");
        		 
        		 controller=oname;
                 Registry.getRegistry(null, null)
                     .registerComponent(this, oname, null);

                 
        	 }catch( Throwable t ) {
        		 
        	 }
        	 
        	 
        }
    }
    
    
    public String getDomain() {
        if (domain!=null) {
            return domain;
        } else { 
            return getName();
        }
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    
    /**
     * Start this Engine component.
     */
    public void start() throws LifecycleException {
    	if( started ) 
            return;
    	
    	if( !initialized ) 
    		init();
    	
    	
    	log.info( "Starting Servlet Engine");
    	
    	super.start();
    }

}
