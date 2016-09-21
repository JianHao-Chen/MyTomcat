package My.catalina;


/**
 * A <b>Host</b> is a Container that represents a virtual host in the
 * Catalina servlet engine.  It is useful in the following types of scenarios:
 * <ul>
 * <li>You wish to use Interceptors that see every single request processed
 *     by this particular virtual host.
 * <li>You wish to run Catalina in with a standalone HTTP connector, but still
 *     want support for multiple virtual hosts.
 * </ul>
 * In general, you would not use a Host when deploying Catalina connected
 * to a web server (such as Apache), because the Connector will have
 * utilized the web server's facilities to determine which Context (or
 * perhaps even which Wrapper) should be utilized to process this request.
 * <p>
 * The parent Container attached to a Host is generally an Engine, but may
 * be some other implementation, or may be omitted if it is not necessary.
 * <p>
 * The child containers attached to a Host are generally implementations
 * of Context (representing an individual servlet context).
 */

public interface Host extends Container{

	// ---------------- Manifest Constants ----------------
	
	
	
	// ----------------------- Properties -----------------------
	 /**
     * Return the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getAppBase();


    /**
     * Set the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param appBase The new application root
     */
    public void setAppBase(String appBase);
    
    
    /**
     * Return the value of the auto deploy flag.  If true, it indicates that 
     * this host's child webapps should be discovred and automatically 
     * deployed dynamically.
     */
    public boolean getAutoDeploy();
    
    /**
     * Set the auto deploy flag value for this host.
     * 
     * @param autoDeploy The new auto deploy flag
     */
    public void setAutoDeploy(boolean autoDeploy);
    
    
    /**
     * Return the Java class name of the context configuration class
     * for new web applications.
     */
    public String getConfigClass();

    
    /**
     * Set the Java class name of the context configuration class
     * for new web applications.
     *
     * @param configClass The new context configuration class
     */
    public void setConfigClass(String configClass);

    
    /**
     * Return the value of the deploy on startup flag.  If true, it indicates 
     * that this host's child webapps should be discovred and automatically 
     * deployed.
     */
    public boolean getDeployOnStartup();


    /**
     * Set the deploy on startup flag value for this host.
     * 
     * @param deployOnStartup The new deploy on startup flag
     */
    public void setDeployOnStartup(boolean deployOnStartup);
    
    
    
    /**
     * Return the canonical, fully qualified, name of the virtual host
     * this Container represents.
     */
    public String getName();


    /**
     * Set the canonical, fully qualified, name of the virtual host
     * this Container represents.
     *
     * @param name Virtual host name
     *
     * @exception IllegalArgumentException if name is null
     */
    public void setName(String name);
    
    
    
    
    
    
    
    
    
	// ---------------------- Public Methods ----------------------
    
    /**
     * Return the Context that would be used to process the specified
     * host-relative request URI, if any; otherwise return <code>null</code>.
     *
     * @param uri Request URI to be mapped
     */
    public Context map(String uri);
    
	
	
	
}
