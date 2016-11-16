package My.catalina;

import javax.servlet.ServletContext;

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

public interface Context extends Container{

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
     * Return the "correctly configured" flag for this Context.
     */
    public boolean getConfigured();


    /**
     * Set the "correctly configured" flag for this Context.  This can be
     * set to false by startup listeners that detect a fatal configuration
     * error to avoid the application from being made available.
     *
     * @param configured The new correctly configured flag
     */
    public void setConfigured(boolean configured);


    /**
     * Set the path to a file to save this Context information.
     *
     * @param configFile The path to a file to save this Context information.
     */
    public void setConfigFile(String configFile);
    
    
    /**
     * Gets the value of the use HttpOnly cookies for session cookies flag.
     * 
     * @return <code>true</code> if the HttpOnly flag should be set on session
     *         cookies
     */
    public boolean getUseHttpOnly();


    /**
     * Sets the use HttpOnly cookies for session cookies flag.
     * 
     * @param useHttpOnly   Set to <code>true</code> to use HttpOnly cookies
     *                          for session cookies
     */
    public void setUseHttpOnly(boolean useHttpOnly);
    
    
    /**
     * Return the context path for this web application.
     */
    public String getPath();


    /**
     * Set the context path for this web application.
     *
     * @param path The new context path
     */
    public void setPath(String path);
    
    
    
    /**
     * Return the override flag for this web application.
     */
    public boolean getOverride();


    /**
     * Set the override flag for this web application.
     *
     * @param override The new override flag
     */
    public void setOverride(boolean override);
    
    
    
    /**
     * Return the servlet context for which this Context is a facade.
     */
    public ServletContext getServletContext();
    
    
    /**
     * Return the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getDocBase();


    /**
     * Set the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param docBase The new document root
     */
    public void setDocBase(String docBase);
    
    
    
    /**
     * Is URL rewriting disabled?
     * URL rewriting is an optional component of the servlet 2.5 specification.
     * However if set to true this will be non-compliant with the specification
     * as the specification requires that there <b>must</b> be a way to retain
     * sessions if the client doesn't allow session cookies.
     * 
     * @return true If URL rewriting is disabled.
     * 
     * @see <a href="http://jcp.org/aboutJava/communityprocess/mrel/jsr154/index2.html">Servlet
     *      2.5 Specification. Sections SRV.7.1.3 and SRV.7.1.4</a>
     * @see javax.servlet.http.HttpServletResponse#encodeURL(String) encodeURL
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(String)
     *      encodeRedirectURL
     */
    public boolean isDisableURLRewriting();
    
    /**
     * Is URL rewriting disabled?
     * URL rewriting is an optional component of the servlet 2.5 specification.
     * However if set to true this will be non-compliant with the specification
     * as the specification requires that there <b>must</b> be a way to retain
     * sessions if the client doesn't allow session cookies.
     *
     * @param disable True to disable URL Rewriting. Default <b>false</b>.
     */
    public void setDisableURLRewriting(boolean disable);
    
    
    /**
     * Factory method to create and return a new Wrapper instance, of
     * the Java implementation class appropriate for this Context
     * implementation.  The constructor of the instantiated Wrapper
     * will have been called, but no properties will have been set.
     */
    public Wrapper createWrapper();
    
    /**
     * Add a resource which will be watched for reloading by the host auto
     * deployer. Note: this will not be used in embedded mode.
     * 
     * @param name Path to the resource, relative to docBase
     */
    public void addWatchedResource(String name);
    
    /**
     * Return the set of watched resources for this Context. If none are 
     * defined, a zero length array will be returned.
     */
    public String[] findWatchedResources();
    
    
    /**
     * Return the reloadable flag for this web application.
     */
    public boolean getReloadable();


    /**
     * Set the reloadable flag for this web application.
     *
     * @param reloadable The new reloadable flag
     */
    public void setReloadable(boolean reloadable);
    
    
    /**
     * Return the privileged flag for this web application.
     */
    public boolean getPrivileged();


    /**
     * Set the privileged flag for this web application.
     *
     * @param privileged The new privileged flag
     */
    public void setPrivileged(boolean privileged);
    
    
    
    /**
     * Return the "use cookies for session ids" flag.
     */
    public boolean getCookies();


    /**
     * Set the "use cookies for session ids" flag.
     *
     * @param cookies The new flag
     */
    public void setCookies(boolean cookies);
    
    
    /**
     * Gets the name to use for session cookies. Overrides any setting that
     * may be specified by the application.
     * 
     * @return  The value of the default session cookie name or null if not
     *          specified
     */
    public String getSessionCookieName();
    
    
    /**
     * Sets the name to use for session cookies. Overrides any setting that
     * may be specified by the application.
     * 
     * @param sessionCookieName   The name to use
     */
    public void setSessionCookieName(String sessionCookieName);
    
    
    /**
     * Return the default session timeout (in minutes) for this
     * web application.
     */
    public int getSessionTimeout();


    /**
     * Set the default session timeout (in minutes) for this
     * web application.
     *
     * @param timeout The new default session timeout
     */
    public void setSessionTimeout(int timeout);
    
    
    
    /**
     * Gets the path to use for session cookies. Overrides any setting that
     * may be specified by the application.
     * 
     * @return  The value of the default session cookie path or null if not
     *          specified
     */
    public String getSessionCookiePath();
    
    
    /**
     * Sets the path to use for session cookies. Overrides any setting that
     * may be specified by the application.
     * 
     * @param sessionCookiePath   The path to use
     */
    public void setSessionCookiePath(String sessionCookiePath);
    
    
    /**
     * Return the URL encoded context path, using UTF-8.
     */
    public String getEncodedPath();
    
    
    /**
     * Return the distributable flag for this web application.
     */
    public boolean getDistributable();


    /**
     * Set the distributable flag for this web application.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable);
    
}
