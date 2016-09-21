package My.catalina.core;

import java.util.Map;
import java.util.WeakHashMap;

import javax.management.ObjectName;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Host;
import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.tomcat.util.modeler.Registry;

/**
 * Standard implementation of the <b>Host</b> interface.  Each
 * child container must be a Context implementation to process the
 * requests directed to a particular web application.
 */

public class StandardHost extends ContainerBase implements Host{

	private static My.juli.logging.Log log=
		My.juli.logging.LogFactory.getLog( StandardHost.class );
	
	// ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardHost component with the default basic Valve.
     */
    public StandardHost() {

        super();
        pipeline.setBasic(new StandardHostValve());

    }
    
	// ---------------- Instance Variables ----------------
    
    /**
     * The application root for this Host.
     */
    private String appBase = "webapps";


    /**
     * The auto deploy flag for this Host.
     */
    private boolean autoDeploy = true;
    
    
    /**
     * The Java class name of the default context configuration class
     * for deployed web applications.
     */
    private String configClass =
        "My.catalina.startup.ContextConfig";
    
    
    /**
     * The deploy on startup flag for this Host.
     */
    private boolean deployOnStartup = true;
    
    
    /**
     * deploy Context XML config files property.
     */
    private boolean deployXML = true;
    
    
    /**
     * Unpack WARs property.
     */
    private boolean unpackWARs = true;
    
    /**
     * Attribute value used to turn on/off XML namespace awarenes.
     */
     private boolean xmlNamespaceAware = false;
     
     /**
      * Attribute value used to turn on/off XML validation
      */
      private boolean xmlValidation = false;
     
     
    
    /**
     * Track the class loaders for the child web applications so memory leaks
     * can be detected.
     */
    private Map<ClassLoader, String> childClassLoaders =
        new WeakHashMap<ClassLoader, String>();
    
	// ------------------------ Properties ------------------------
    
    /**
     * Return the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getAppBase() {

        return (this.appBase);

    }


    /**
     * Set the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param appBase The new application root
     */
    public void setAppBase(String appBase) {

        String oldAppBase = this.appBase;
        this.appBase = appBase;
    }
    
    
    /**
     * Return the value of the auto deploy flag.  If true, it indicates that 
     * this host's child webapps will be dynamically deployed.
     */
    public boolean getAutoDeploy() {

        return (this.autoDeploy);

    }


    /**
     * Set the auto deploy flag value for this host.
     * 
     * @param autoDeploy The new auto deploy flag
     */
    public void setAutoDeploy(boolean autoDeploy) {

        boolean oldAutoDeploy = this.autoDeploy;
        this.autoDeploy = autoDeploy;

    }


    /**
     * Return the Java class name of the context configuration class
     * for new web applications.
     */
    public String getConfigClass() {

        return (this.configClass);

    }


    /**
     * Set the Java class name of the context configuration class
     * for new web applications.
     *
     * @param configClass The new context configuration class
     */
    public void setConfigClass(String configClass) {

        String oldConfigClass = this.configClass;
        this.configClass = configClass;   
    }
    
    


    /**
     * Return the value of the deploy on startup flag.  If true, it indicates 
     * that this host's child webapps should be discovred and automatically 
     * deployed at startup time.
     */
    public boolean getDeployOnStartup() {

        return (this.deployOnStartup);

    }


    /**
     * Set the deploy on startup flag value for this host.
     * 
     * @param deployOnStartup The new deploy on startup flag
     */
    public void setDeployOnStartup(boolean deployOnStartup) {

        boolean oldDeployOnStartup = this.deployOnStartup;
        this.deployOnStartup = deployOnStartup;
    }
    
    
    
    
    /**
     * Deploy XML Context config files flag accessor.
     */
    public boolean isDeployXML() {

        return (deployXML);

    }
    
    
    /**
     * Deploy XML Context config files flag mutator.
     */
    public void setDeployXML(boolean deployXML) {

        this.deployXML = deployXML;

    }
    
    /**
     * Unpack WARs flag accessor.
     */
    public boolean isUnpackWARs() {

        return (unpackWARs);

    }


    /**
     * Unpack WARs flag mutator.
     */
    public void setUnpackWARs(boolean unpackWARs) {

        this.unpackWARs = unpackWARs;

    }
    
    
    /**
     * Get the server.xml &lt;host&gt; attribute's xmlNamespaceAware.
     * @return true if namespace awarenes is enabled.
     *
     */
    public boolean getXmlNamespaceAware(){
        return xmlNamespaceAware;
    }


    /**
     * Set the namespace aware feature of the XML parser used when
     * parsing xml instances.
     * @param xmlNamespaceAware true to enable namespace awareness
     */
    public void setXmlNamespaceAware(boolean xmlNamespaceAware){
        this.xmlNamespaceAware=xmlNamespaceAware;
    }    
    
    
    /**
     * Set the validation feature of the XML parser used when
     * parsing xml instances.
     * @param xmlValidation true to enable xml instance validation
     */
    public void setXmlValidation(boolean xmlValidation){
        
        this.xmlValidation = xmlValidation;

    }

    /**
     * Get the server.xml &lt;host&gt; attribute's xmlValidation.
     * @return true if validation is enabled.
     *
     */
    public boolean getXmlValidation(){
        return xmlValidation;
    }
    
    
    
    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Context.
     *
     * @param child Child container to be added
     */
    public void addChild(Container child) {

        if (child instanceof Lifecycle) {
            ((Lifecycle) child).addLifecycleListener(
                    new MemoryLeakTrackingListener());
        }

        if (!(child instanceof Context))
            throw new IllegalArgumentException
                ("standardHost.notContext");
        
        super.addChild(child);

    }
    
    
    /**
     * Used to ensure the regardless of {@link Context} implementation, a record
     * is kept of the class loader used every time a context starts.
     */
    private class MemoryLeakTrackingListener implements LifecycleListener {
    	
    	public void lifecycleEvent(LifecycleEvent event) {
    		
    		if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
    			
    			// implements latter.
    		}
    	}
    }
    


	@Override
	public Context map(String uri) {
		return null;
	}

	
	
	// -------------------- Public Methods -------------------- 
	
	public void init() {
		
		if( initialized ) 
			return;
        initialized=true;
        
        if( getParent() == null ) {
        	//;
        }
        
        if( oname==null ) {
        	
        	try {
        		StandardEngine engine=(StandardEngine)parent;
        		domain=engine.getName();
        		
        		oname = new ObjectName(domain + ":type=Host,host=" +
                        this.getName());
        		
        		controller = oname;
        		
        		Registry.getRegistry(null, null)
                	.registerComponent(this, oname, null);
        		
        	}catch( Throwable t ) {
        		
        	}
        }
        
	}
	
	
	 /**
     * Start this host.
     */
	 public synchronized void start() throws LifecycleException {
		 
		 if( started ) 
			 return;
		 
		 if( ! initialized )
			 init();
		 
		 
		 super.start();
	 }
    
	
}
