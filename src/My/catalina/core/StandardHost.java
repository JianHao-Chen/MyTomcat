package My.catalina.core;

import java.util.Map;
import java.util.WeakHashMap;

import javax.naming.Context;

import My.catalina.Host;

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
        "org.apache.catalina.startup.ContextConfig";
    
    
    /**
     * The deploy on startup flag for this Host.
     */
    private boolean deployOnStartup = true;
    
    
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


	@Override
	public Context map(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

    
	
}
