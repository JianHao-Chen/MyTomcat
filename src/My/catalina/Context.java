package My.catalina;

/**
 * A <b>Context</b> is a Container that represents a servlet context, and
 * therefore an individual web application, in the Catalina servlet engine.
 * It is therefore useful in almost every deployment of Catalina (even if a
 * Connector attached to a web server (such as Apache) uses the web server's
 * facilities to identify the appropriate Wrapper to handle this request.
 * It also provides a convenient mechanism to use Interceptors that see
 * every request processed by this particular web application.
 * <p>
 * The parent Container attached to a Context is generally a Host, but may
 * be some other implementation, or may be omitted if it is not necessary.
 * <p>
 * The child containers attached to a Context are generally implementations
 * of Wrapper (representing individual servlet definitions).
 * <p>
 */

public interface Context {

	 // ----------------- Manifest Constants -----------------
	
	/**
     * The LifecycleEvent type sent when a context is reloaded.
     */
    public static final String RELOAD_EVENT = "reload";


    /**
     * Container event for changing the ID of a session.
     */
    public static final String CHANGE_SESSION_ID_EVENT = "changeSessionId";
    
    
	// -------------------------- Properties --------------------------
    
    /**
     * Return the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationEventListeners();
    
    
    /**
     * Store the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationEventListeners(Object listeners[]);
    
    
    /**
     * Return the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationLifecycleListeners();


    /**
     * Store the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationLifecycleListeners(Object listeners[]);
    
    
    /**
     * Return the application available flag for this Context.
     */
    public boolean getAvailable();


    /**
     * Set the application available flag for this Context.
     *
     * @param available The new application available flag
     */
    public void setAvailable(boolean available);
    
    /**
     * Return the Locale to character set mapper for this Context.
     */
 //   public CharsetMapper getCharsetMapper();


    /**
     * Set the Locale to character set mapper for this Context.
     *
     * @param mapper The new mapper
     */
 //   public void setCharsetMapper(CharsetMapper mapper);


    /**
     * Return the path to a file to save this Context information.
     */
    public String getConfigFile();


    /**
     * Set the path to a file to save this Context information.
     *
     * @param configFile The path to a file to save this Context information.
     */
    public void setConfigFile(String configFile);
    
    
    
}
