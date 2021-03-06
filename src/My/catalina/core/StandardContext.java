package My.catalina.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Globals;
import My.catalina.Host;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.Loader;
import My.catalina.Manager;
import My.catalina.Wrapper;
import My.catalina.loader.WebappLoader;
import My.catalina.session.StandardManager;
import My.catalina.util.RequestUtil;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.naming.resources.BaseDirContext;
import My.naming.resources.FileDirContext;
import My.naming.resources.ProxyDirContext;
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
     * The antiJARLocking flag for this Context.
     */
    private boolean antiJARLocking = false;
    
    
    /**
     * The document root for this web application.
     */
    private String docBase = null;
    
    
    /**
     * Has URL rewriting been disabled. 
     */
    private boolean disableURLRewriting = false;
    
    /**
     * The path to a file to save this Context information.
     */
    private String configFile = null;


    /**
     * The "correctly configured" flag for this Context.
     */
    private boolean configured = false;

    
    /**
     * The DefaultContext override flag for this web application.
     */
    private boolean override = false;
    
    
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
     * The public identifier of the DTD for the web application deployment
     * descriptor version we are currently parsing.  This is used to support
     * relaxed validation rules when processing version 2.2 web.xml files.
     */
    private String publicId = null;
    
    
    /**
     * The application available flag for this Context.
     */
    private boolean available = false;
    
    /**
     * The ServletContext implementation associated with this Context.
     */
    protected transient ApplicationContext context = null;
    
    
    /**
     * The mapper associated with this context.
     */
    private My.tomcat.util.http.mapper.Mapper mapper = 
        new My.tomcat.util.http.mapper.Mapper();
    
    /**
     * The servlet mappings for this web application, keyed by
     * matching pattern.
     */
    private HashMap servletMappings = new HashMap();
    
    private final Object servletMappingsLock = new Object();
    
    
    /**
     * Java class name of the Wrapper class implementation we use.
     */
    private String wrapperClassName = StandardWrapper.class.getName();
    private Class wrapperClass = null;
    
    
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
     * The MIME mappings for this web application, keyed by extension.
     */
    private HashMap mimeMappings = new HashMap();
    
    
    /**
     * The reloadable flag for this web application.
     */
    private boolean reloadable = false;
    
    
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
     * The privileged flag for this web application.
     */
    private boolean privileged = false;
    
    
    /**
     * The set of classnames of InstanceListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String instanceListeners[] = new String[0];

    private final Object instanceListenersLock = new Object();
    
    
    /** 
     * Override the default context xml location.
     */
    private String defaultContextXml;


    /** 
     * Override the default web xml location.
     */
    private String defaultWebXml;
    
    /**
     * The distributable flag for this web application.
     */
    private boolean distributable = false;
    
    private transient DirContext webappResources = null;
    
    
    /**
     * The pathname to the work directory for this context (relative to
     * the server's home if not absolute).
     */
    private String workDir = null;
    
    /**
     * Filesystem based flag.
     */
    private boolean filesystemBased = false;
    
    /**
     * Case sensitivity.
     */
    protected boolean caseSensitive = true;
    
    
    /**
     * Allow linking.
     */
    protected boolean allowLinking = false;

    
    
    /**
     * Caching allowed flag.
     */
    private boolean cachingAllowed = true;
    
    /**
     * Cache TTL in ms.
     */
    protected int cacheTTL = 5000;
    
    
    /**
     * Cache max size in KB.
     */
    protected int cacheMaxSize = 10240; // 10 MB
    
    
    /**
     * Cache object max size in KB.
     */
    protected int cacheObjectMaxSize = 512; // 512K
    
    
    /**
     * The welcome files for this application.
     */
    private String welcomeFiles[] = new String[0];
    
    /**
     * If an HttpClient keep-alive timer thread has been started by this web
     * application and is still running, should Tomcat change the context class
     * loader from the current {@link WebappClassLoader} to
     * {@link WebappClassLoader#parent} to prevent a memory leak? Note that the
     * keep-alive timer thread will stop on its own once the keep-alives all
     * expire however, on a busy system that might not happen for some time.
     */
    private boolean clearReferencesHttpClientKeepAliveThread = true;
    
    
    /**
     * Should Tomcat attempt to terminate any {@link java.util.TimerThread}s
     * that have been started by the web application? If not specified, the
     * default value of <code>false</code> will be used.
     */
    private boolean clearReferencesStopTimerThreads = false;
    
    
    /**
     * Should Tomcat attempt to clear any ThreadLocal objects that are instances
     * of classes loaded by this class loader. Failure to remove any such
     * objects will result in a memory leak on web application stop, undeploy or
     * reload. It is disabled by default since the clearing of the ThreadLocal
     * objects is not performed in a thread-safe manner.
     */
    private boolean clearReferencesThreadLocals = false;
    
    
    /**
     * Should Tomcat attempt to terminate threads that have been started by the
     * web application? Stopping threads is performed via the deprecated (for
     * good reason) <code>Thread.stop()</code> method and is likely to result in
     * instability. As such, enabling this should be viewed as an option of last
     * resort in a development environment and is not recommended in a
     * production environment. If not specified, the default value of
     * <code>false</code> will be used.
     */
    private boolean clearReferencesStopThreads = false;
    
    
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
     * Return the public identifier of the deployment descriptor DTD that is
     * currently being parsed.
     */
    public String getPublicId() {

        return (this.publicId);

    }


    /**
     * Set the public identifier of the deployment descriptor DTD that is
     * currently being parsed.
     *
     * @param publicId The public identifier
     */
    public void setPublicId(String publicId) {

        if (log.isDebugEnabled())
            log.debug("Setting deployment descriptor public ID to '" +
                publicId + "'");

        String oldPublicId = this.publicId;
        this.publicId = publicId;
        
    }
    
    
    /**
     * Return the DefaultContext override flag for this web application.
     */
    public boolean getOverride() {

        return (this.override);

    }
    
    
    /**
     * Set the DefaultContext override flag for this web application.
     *
     * @param override The new override flag
     */
    public void setOverride(boolean override) {

        boolean oldOverride = this.override;
        this.override = override;

    }
    
    
    /**
     * Set case sensitivity.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }


    /**
     * Is case sensitive ?
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    
    /**
     * Set allow linking.
     */
    public void setAllowLinking(boolean allowLinking) {
        this.allowLinking = allowLinking;
    }


    /**
     * Is linking allowed.
     */
    public boolean isAllowLinking() {
        return allowLinking;
    }
    
    
    /**
     * Is caching allowed ?
     */
    public boolean isCachingAllowed() {
        return cachingAllowed;
    }


    /**
     * Set caching allowed flag.
     */
    public void setCachingAllowed(boolean cachingAllowed) {
        this.cachingAllowed = cachingAllowed;
    }
    
    
    /**
     * Set cache TTL.
     */
    public void setCacheTTL(int cacheTTL) {
        this.cacheTTL = cacheTTL;
    }


    /**
     * Get cache TTL.
     */
    public int getCacheTTL() {
        return cacheTTL;
    }
    
    
    /**
     * Return the maximum size of the cache in KB.
     */
    public int getCacheMaxSize() {
        return cacheMaxSize;
    }


    /**
     * Set the maximum size of the cache in KB.
     */
    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }
    
    
    /**
     * Return the maximum size of objects to be cached in KB.
     */
    public int getCacheObjectMaxSize() {
        return cacheObjectMaxSize;
    }


    /**
     * Set the maximum size of objects to be placed the cache in KB.
     */
    public void setCacheObjectMaxSize(int cacheObjectMaxSize) {
        this.cacheObjectMaxSize = cacheObjectMaxSize;
    }
    
    
    /**
     * Set the Loader with which this Context is associated.
     *
     * @param loader The newly associated loader
     */
    public synchronized void setLoader(Loader loader) {

        super.setLoader(loader);

    }
    
    
    
    /**
     * Returns true if the resources associated with this context are
     * filesystem based.
     */
    public boolean isFilesystemBased() {

        return (filesystemBased);

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
     * The flag that indicates that session cookies should use HttpOnly
     */
    private boolean useHttpOnly = false;
    
    /**
     * Gets the value of the use HttpOnly cookies for session cookies flag.
     * 
     * @return <code>true</code> if the HttpOnly flag should be set on session
     *         cookies
     */
    public boolean getUseHttpOnly() {
        return useHttpOnly;
    }


    /**
     * Sets the use HttpOnly cookies for session cookies flag.
     * 
     * @param useHttpOnly   Set to <code>true</code> to use HttpOnly cookies
     *                          for session cookies
     */
    public void setUseHttpOnly(boolean useHttpOnly) {
        boolean oldUseHttpOnly = this.useHttpOnly;
        this.useHttpOnly = useHttpOnly;
    }

    
    /**
     * Return a File object representing the base directory for the
     * entire servlet container (i.e. the Engine container if present).
     */
    protected File engineBase() {
    	String base=System.getProperty("catalina.base");
    	return (new File(base));
    }
    
    
    /**
     * Get base path.
     */
    protected String getBasePath() {
    	
    	String docBase = null;
        Container container = this;
        
        while (container != null) {
        	if (container instanceof Host)
                break;
            container = container.getParent();
        }
        
        File file = new File(getDocBase());
        
        if (!file.isAbsolute()) {
        	String appBase = ((Host) container).getAppBase();
        	file = new File(appBase);
        	if (!file.isAbsolute())
        		file = new File(engineBase(), appBase);
        	
        	docBase = (new File(file, getDocBase())).getPath();
        }
        else
        	docBase = file.getPath();
       
        return docBase;
    }
    
    
    public String getHostname() {
        Container parentHost = getParent();
        if (parentHost != null) {
            hostName = parentHost.getName();
        }
        if ((hostName == null) || (hostName.length() < 1))
            hostName = "_";
        return hostName;
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
    
    
    
    public String getDefaultContextXml() {
        return defaultContextXml;
    }

    /** 
     * Set the location of the default context xml that will be used.
     * If not absolute, it'll be made relative to the engine's base dir
     * ( which defaults to catalina.base system property ).
     *
     * @param defaultContextXml The default web xml 
     */
    public void setDefaultContextXml(String defaultContextXml) {
        this.defaultContextXml = defaultContextXml;
    }
    
    
    
    /**
     * Return the reloadable flag for this web application.
     */
    public boolean getReloadable() {

        return (this.reloadable);

    }
    
    
    /**
     * Set the reloadable flag for this web application.
     */
    public void setReloadable(boolean reloadable) {
    	this.reloadable = reloadable;
    }
    
    
    
    /**
     * Return the distributable flag for this web application.
     */
    public boolean getDistributable() {

        return (this.distributable);

    }
    
    /**
     * Set the distributable flag for this web application.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable) {
    	
    	this.distributable = distributable;
    	
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
     * Factory method to create and return a new Wrapper instance, of
     * the Java implementation class appropriate for this Context
     * implementation.  The constructor of the instantiated Wrapper
     * will have been called, but no properties will have been set.
     */
    public Wrapper createWrapper() {
    	
    	Wrapper wrapper = null;
    	if (wrapperClass != null) {
    		try {
                wrapper = (Wrapper) wrapperClass.newInstance();
            } catch (Throwable t) {
                log.error("createWrapper", t);
                return (null);
            }
    	}
    	else{
    		wrapper = new StandardWrapper();
    	}

    	
    	return (wrapper);
    	
    }
    
    
    /**
     * Return the servlet context for which this Context is a facade.
     */
    public ServletContext getServletContext() {
    	
    	if (context == null) {
    		context = new ApplicationContext(getBasePath(), this);
    	}
    	return (context.getFacade());
    }
    
    
    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Wrapper.
     *
     * @param child Child container to be added
     *
     * @exception IllegalArgumentException if the proposed container is
     *  not an implementation of Wrapper
     */
    public void addChild(Container child) {
    	// Global JspServlet
        Wrapper oldJspServlet = null;

        if (!(child instanceof Wrapper)) {
        	throw new IllegalArgumentException
            ("standardContext.notWrapper");
        }
        
        Wrapper wrapper = (Wrapper) child;
        boolean isJspServlet = "jsp".equals(child.getName());
        
        
        
        super.addChild(child);
        
    }
    
    
    
    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     *
     * @exception IllegalArgumentException if the specified servlet name
     *  is not known to this Context
     */
    public void addServletMapping(String pattern, String name) {
        addServletMapping(pattern, name, false);
    }
    
    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     * @param jspWildCard true if name identifies the JspServlet
     * and pattern contains a wildcard; false otherwise
     *
     * @exception IllegalArgumentException if the specified servlet name
     *  is not known to this Context
     */
    public void addServletMapping(String pattern, String name,
                                  boolean jspWildCard) {
    	
    	// Validate the proposed mapping
        if (findChild(name) == null)
        	throw new IllegalArgumentException
            	("standardContext.servletMap.name");
        
        pattern = adjustURLPattern(RequestUtil.URLDecode(pattern));
        
        if (!validateURLPattern(pattern))
        	throw new IllegalArgumentException
            ("standardContext.servletMap.pattern");
        
        // Add this mapping to our registered set
        synchronized (servletMappingsLock) {
        	String name2 = (String) servletMappings.get(pattern);
        	if (name2 != null) {
        		// Don't allow more than one servlet on the same pattern
                Wrapper wrapper = (Wrapper) findChild(name2);
                wrapper.removeMapping(pattern);
                mapper.removeWrapper(pattern);
        	}
        	servletMappings.put(pattern, name);
        }
        Wrapper wrapper = (Wrapper) findChild(name);
        wrapper.addMapping(pattern);
        
        // Update context mapper
        mapper.addWrapper(pattern, wrapper, jspWildCard);
    }
    
    
    /**
     * Adjust the URL pattern to begin with a leading slash, if appropriate
     * (i.e. we are running a servlet 2.2 application).  Otherwise, return
     * the specified URL pattern unchanged.
     *
     * @param urlPattern The URL pattern to be adjusted (if needed)
     *  and returned
     */
    protected String adjustURLPattern(String urlPattern) {
    	
    	if (urlPattern == null)
            return (urlPattern);
    	
    	if (urlPattern.startsWith("/") || urlPattern.startsWith("*."))
            return (urlPattern);
    	
    	
    	return ("/" + urlPattern);
    }
    
    /**
     * Validate the syntax of a proposed <code>&lt;url-pattern&gt;</code>
     * for conformance with specification requirements.
     *
     * @param urlPattern URL pattern to be validated
     */
    private boolean validateURLPattern(String urlPattern) {
    	
    	if (urlPattern == null)
            return (false);
    	
    	if (urlPattern.indexOf('\n') >= 0 || urlPattern.indexOf('\r') >= 0) {
            return (false);
        }
    	
    	if (urlPattern.startsWith("*.")) {
    		if (urlPattern.indexOf('/') < 0) {
    			return (true);
    		}
    		else
                return (false);
    	}
    	
    	if ( (urlPattern.startsWith("/")) &&
                (urlPattern.indexOf("*.") < 0)) {
    		return (true);
    	}
    	else
            return (false);
    }
    
    
    /**
     * Check for unusual but valid <code>&lt;url-pattern&gt;</code>s.
     */
    private void checkUnusualURLPattern(String urlPattern) {
    	
    }
    
    
    
    /**
     * Should we attempt to use cookies for session id communication?
     */
    private boolean cookies = true;

    /**
     * Return the "use cookies for session ids" flag.
     */
    public boolean getCookies() {

        return (this.cookies);
    }

    /**
     * Set the "use cookies for session ids" flag.
     */
    public void setCookies(boolean cookies) {

        boolean oldCookies = this.cookies;
        this.cookies = cookies;
    }
    
    
    
    
    /**
     * The name to use for session cookies. <code>null</code> indicates that
     * the name is controlled by the application.
     */
    private String sessionCookieName;
    
    /**
     * Gets the name to use for session cookies.
     * 
     * @return  The value of the default session cookie name or null if not
     *          specified
     */
    public String getSessionCookieName() {
        return sessionCookieName;
    }
    
    /**
     * Sets the name to use for session cookies. Overrides any setting that
     * may be specified by the application.
     * 
     * @param sessionCookieName   The name to use
     */
    public void setSessionCookieName(String sessionCookieName) {
        String oldSessionCookieName = this.sessionCookieName;
        this.sessionCookieName = sessionCookieName;
    }
    
    
    
    
    /**
     * The session timeout (in minutes) for this web application.
     */
    private int sessionTimeout = 30;
    
    /**
     * Return the default session timeout (in minutes) for this
     * web application.
     */
    public int getSessionTimeout() {

        return (this.sessionTimeout);

    }
    
    /**
     * Set the default session timeout (in minutes) for this
     * web application.
     *
     * @param timeout The new default session timeout
     */
    public void setSessionTimeout(int timeout) {
    	int oldSessionTimeout = this.sessionTimeout;
        
        this.sessionTimeout = (timeout == 0) ? -1 : timeout;
    }
    
    
    
    /**
     * The path to use for session cookies. <code>null</code> indicates that
     * the path is controlled by the application.
     */
    private String sessionCookiePath;
    
    /**
     * Gets the path to use for session cookies.
     * 
     * @return  The value of the default session cookie path or null if not
     *          specified
     */
    public String getSessionCookiePath() {
        return sessionCookiePath;
    }
    
    /**
     * Sets the path to use for session cookies.
     * 
     * @param sessionCookiePath   The path to use
     */
    public void setSessionCookiePath(String sessionCookiePath) {
        String oldSessionCookiePath = this.sessionCookiePath;
        this.sessionCookiePath = sessionCookiePath;
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
    
    public void setName( String name ) {
        super.setName( name );
        encodedPath = (name);
    }
    
    
    
    /**
     * Should we allow the <code>ServletContext.getContext()</code> method
     * to access the context of other web applications in this server?
     */
    private boolean crossContext = false;
    
    /**
     * Return the "allow crossing servlet contexts" flag.
     */
    public boolean getCrossContext() {

        return (this.crossContext);
    }

    /**
     * Set the "allow crossing servlet contexts" flag.
     *
     * @param crossContext The new cross contexts flag
     */
    public void setCrossContext(boolean crossContext) {

        boolean oldCrossContext = this.crossContext;
        this.crossContext = crossContext;
    }
    
    
    
    /**
     * Encoded path.
     */
    private String encodedPath = null;
    
    public String getEncodedPath() {
        return encodedPath;
    }
    
    
    /**
     * Set the resources DirContext object with which this Container is
     * associated.
     */
    public synchronized void setResources(DirContext resources) {
    	
    	if (started) {
    		throw new IllegalStateException
            ("standardContext.resources.started");
    	}
    	
    	DirContext oldResources = this.webappResources;
    	if (oldResources == resources)
            return;
    	
    	if (resources instanceof BaseDirContext) {
    		
    		((BaseDirContext) resources).setCached(isCachingAllowed());
    		((BaseDirContext) resources).setCacheTTL(getCacheTTL());
    		((BaseDirContext) resources).setCacheMaxSize(getCacheMaxSize());
            ((BaseDirContext) resources).setCacheObjectMaxSize(
                    getCacheObjectMaxSize());
    	}
    	
    	if (resources instanceof FileDirContext) {
    		filesystemBased = true;
    		((FileDirContext) resources).setCaseSensitive(isCaseSensitive());
            ((FileDirContext) resources).setAllowLinking(isAllowLinking());
    	}
    	
    	this.webappResources = resources;
    	
    	// The proxied resources will be refreshed on start
        this.resources = null;
    }
    
    
    /**
     * Return the privileged flag for this web application.
     */
    public boolean getPrivileged() {

        return (this.privileged);

    }


    /**
     * Set the privileged flag for this web application.
     *
     * @param privileged The new privileged flag
     */
    public void setPrivileged(boolean privileged) {

        boolean oldPrivileged = this.privileged;
        this.privileged = privileged;   
    }
    
    
    /**
     * Return the antiJARLocking flag for this Context.
     */
    public boolean getAntiJARLocking() {

        return (this.antiJARLocking);

    }
    
    
    /**
     * Return the clearReferencesStopThreads flag for this Context.
     */
    public boolean getClearReferencesStopThreads() {

        return (this.clearReferencesStopThreads);

    }


    /**
     * Set the clearReferencesStopThreads feature for this Context.
     *
     * @param clearReferencesStopThreads The new flag value
     */
    public void setClearReferencesStopThreads(
            boolean clearReferencesStopThreads) {

        boolean oldClearReferencesStopThreads = this.clearReferencesStopThreads;
        this.clearReferencesStopThreads = clearReferencesStopThreads;
        
    }
    
    
    
    /**
     * Return the clearReferencesStopTimerThreads flag for this Context.
     */
    public boolean getClearReferencesStopTimerThreads() {
        return (this.clearReferencesStopTimerThreads);
    }


    /**
     * Set the clearReferencesStopTimerThreads feature for this Context.
     *
     * @param clearReferencesStopTimerThreads The new flag value
     */
    public void setClearReferencesStopTimerThreads(
            boolean clearReferencesStopTimerThreads) {

        boolean oldClearReferencesStopTimerThreads =
            this.clearReferencesStopTimerThreads;
        this.clearReferencesStopTimerThreads = clearReferencesStopTimerThreads;
    }
    
    
    /**
     * Return the clearReferencesHttpClientKeepAliveThread flag for this
     * Context.
     */
    public boolean getClearReferencesHttpClientKeepAliveThread() {
        return (this.clearReferencesHttpClientKeepAliveThread);
    }


    /**
     * Set the clearReferencesHttpClientKeepAliveThread feature for this
     * Context.
     *
     * @param clearReferencesHttpClientKeepAliveThread The new flag value
     */
    public void setClearReferencesHttpClientKeepAliveThread(
            boolean clearReferencesHttpClientKeepAliveThread) {
        this.clearReferencesHttpClientKeepAliveThread =
            clearReferencesHttpClientKeepAliveThread;
    }
    
    
    
    /**
     * Return the clearReferencesThreadLocals flag for this Context.
     */
    public boolean getClearReferencesThreadLocals() {

        return (this.clearReferencesThreadLocals);
    }
    
    
    /**
     * Set the clearReferencesStopThreads feature for this Context.
     *
     * @param clearReferencesStopThreads The new flag value
     */
    public void setClearReferencesThreadLocals(
            boolean clearReferencesThreadLocals) {

        boolean oldClearReferencesThreadLocals =
            this.clearReferencesThreadLocals;
        this.clearReferencesThreadLocals = clearReferencesThreadLocals;
       
    }
    
    
    
    /**
     * Add a new MIME mapping, replacing any existing mapping for
     * the specified extension.
     *
     * @param extension Filename extension being mapped
     * @param mimeType Corresponding MIME type
     */
    public void addMimeMapping(String extension, String mimeType) {

        synchronized (mimeMappings) {
            mimeMappings.put(extension, mimeType);
        }
        fireContainerEvent("addMimeMapping", extension);

    }
    
    
    /**
     * Return the MIME type to which the specified extension is mapped,
     * if any; otherwise return <code>null</code>.
     *
     * @param extension Extension to map to a MIME type
     */
    public String findMimeMapping(String extension) {

        return ((String) mimeMappings.get(extension));

    }
    
    
    /**
     * Return the extensions for which MIME mappings are defined.  If there
     * are none, a zero-length array is returned.
     */
    public String[] findMimeMappings() {

        synchronized (mimeMappings) {
            String results[] = new String[mimeMappings.size()];
            return
                ((String[]) mimeMappings.keySet().toArray(results));
        }

    }
    
    
    
    
    /**
     * The set of application listener class names configured for this
     * application, in the order they were encountered in the web.xml file.
     */
    private String applicationListeners[] = new String[0];
    
    private final Object applicationListenersLock = new Object();
    
    /**
     * Return the set of application listener class names configured
     * for this application.
     */
    public String[] findApplicationListeners() {

        return (applicationListeners);

    }
    
    
    /**
     * Add a new Listener class name to the set of Listeners
     * configured for this application.
     *
     * @param listener Java class name of a listener class
     */
    public void addApplicationListener(String listener) {
    	synchronized (applicationListenersLock) {
    		String results[] =new String[applicationListeners.length + 1];
            for (int i = 0; i < applicationListeners.length; i++) {
                if (listener.equals(applicationListeners[i])) {
                    log.info("standardContext.duplicateListener");
                    return;
                }
                results[i] = applicationListeners[i];
            }
            results[applicationListeners.length] = listener;
            applicationListeners = results;
        }
        fireContainerEvent("addApplicationListener", listener);
    	
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
    public boolean isDisableURLRewriting() {
        return (this.disableURLRewriting);
    }
    
    /**
     * Sets the disabling of URL Rewriting.
     * @param disable True to disable URL Rewriting. Default <b>false</b>.
     */
    public void setDisableURLRewriting(boolean disable){
        boolean oldDisableURLRewriting = this.isDisableURLRewriting();
        this.disableURLRewriting = disable;
        
    }
    
    
    
    /**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        if (getParent() != null) {
            sb.append(getParent().toString());
            sb.append(".");
        }
        sb.append("StandardContext[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

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
    
    
    /**
     * Return the work directory for this Context.
     */
    public String getWorkDir() {

        return (this.workDir);

    }
    
    /**
     * Set the work directory for this Context.
     *
     * @param workDir The new work directory
     */
    public void setWorkDir(String workDir) {

        this.workDir = workDir;

        if (started) {
            postWorkDirectory();
        }
    }

    
    
    /**
     * Set the appropriate context attribute for our work directory.
     */
    private void postWorkDirectory() {
    	// Acquire (or calculate) the work directory path
        String workDir = getWorkDir();
        if (workDir == null || workDir.length() == 0) {

            // Retrieve our parent (normally a host) name
            String hostName = null;
            String engineName = null;
            String hostWorkDir = null;
            Container parentHost = getParent();
            if (parentHost != null) {
                hostName = parentHost.getName();
                if (parentHost instanceof StandardHost) {
                    hostWorkDir = ((StandardHost)parentHost).getWorkDir();
                }
                Container parentEngine = parentHost.getParent();
                if (parentEngine != null) {
                   engineName = parentEngine.getName();
                }
            }
            if ((hostName == null) || (hostName.length() < 1))
                hostName = "_";
            if ((engineName == null) || (engineName.length() < 1))
                engineName = "_";

            String temp = getPath();
            if (temp.startsWith("/"))
                temp = temp.substring(1);
            temp = temp.replace('/', '_');
            temp = temp.replace('\\', '_');
            if (temp.length() < 1)
                temp = "_";
            if (hostWorkDir != null ) {
                workDir = hostWorkDir + File.separator + temp;
            } else {
                workDir = "work" + File.separator + engineName +
                    File.separator + hostName + File.separator + temp;
            }
            setWorkDir(workDir);
        }
        
        
        // Create this directory if necessary
        File dir = new File(workDir);
        if (!dir.isAbsolute()) {
            File catalinaHome = engineBase();
            String catalinaHomePath = null;
            try {
                catalinaHomePath = catalinaHome.getCanonicalPath();
                dir = new File(catalinaHomePath, workDir);
            } catch (IOException e) {
            }
        }
        dir.mkdirs();
        
        // Set the appropriate servlet context attribute
        if (context == null) {
            getServletContext();
        }
        context.setAttribute(Globals.WORK_DIR_ATTR, dir);
        context.setAttributeReadOnly(Globals.WORK_DIR_ATTR);
    }
    
    
    /**
     * Reload this web application, if reloading is supported.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  This method is designed to deal with
     * reloads required by changes to classes in the underlying repositories
     * of our class loader.  It does not handle changes to the web application
     * deployment descriptor.  If that has occurred, you should stop this
     * Context and create (and start) a new Context instance instead.
     *
     * @exception IllegalStateException if the <code>reloadable</code>
     *  property is set to <code>false</code>.
     */
    public synchronized void reload() {
    	
    	// Validate our current component state
    	if (!started)
    		throw new IllegalStateException("containerBase.notStarted");
    	
    	// Make sure reloading is enabled
    	if(log.isInfoEnabled())
            log.info("standardContext.reloadingStarted");
    	
    	// Stop accepting requests temporarily
        setPaused(true);
        
        try {
        	stop();
        }catch (LifecycleException e) {
            log.error("standardContext.stoppingContext");
        }
        
        try {
            start();
        } catch (LifecycleException e) {
            log.error("standardContext.startingContext");
        }
    	
        setPaused(false);
    }
    
    /**
     * Set the request processing paused flag for this Context.
     *
     * @param paused The new request processing paused flag
     */
    private void setPaused(boolean paused) {

        this.paused = paused;

    }
    
    /**
     * Allocate resources, including proxy.
     * Return <code>true</code> if initialization was successfull,
     * or <code>false</code> otherwise.
     */
    public boolean resourcesStart() {
    	
    	boolean ok = true;
    	
    	 Hashtable env = new Hashtable();
    	 
    	 if (getParent() != null)
    		 env.put(ProxyDirContext.HOST, getParent().getName());
    	 
    	 env.put(ProxyDirContext.CONTEXT, getName());
    	 
    	 try {
    		 ProxyDirContext proxyDirContext =
                 new ProxyDirContext(env, webappResources);
    		 
    		 if (webappResources instanceof FileDirContext) {
    			 filesystemBased = true;
                 ((FileDirContext) webappResources).setCaseSensitive
                     (isCaseSensitive());
                 ((FileDirContext) webappResources).setAllowLinking
                     (isAllowLinking());
    		 }
    		 
    		 if (webappResources instanceof BaseDirContext) {
    			 ((BaseDirContext) webappResources).setDocBase(getBasePath());
    			 
    			 ((BaseDirContext) webappResources).setCached(isCachingAllowed());
    			 
    			 ((BaseDirContext) webappResources).setCacheTTL(getCacheTTL());
    			 
    			 ((BaseDirContext) webappResources).setCacheMaxSize(getCacheMaxSize());
    			 
    			 ((BaseDirContext) webappResources).allocate();
    		 }
    		 
    		// Register the cache in JMX
             if (isCachingAllowed()) {
                 ObjectName resourcesName = 
                     new ObjectName(this.getDomain() + ":type=Cache,host=" 
                                    + getHostname() + ",path=" 
                                    + (("".equals(getPath()))?"/":getPath()));
                 Registry.getRegistry(null, null).registerComponent
                     (proxyDirContext.getCache(), resourcesName, null);
             }
    		 
             this.resources = proxyDirContext;
    	 }
    	 catch (Throwable t) {
    		 log.error("standardContext.resourcesStart");
             ok = false;
    	 }
    	 
    	 return (ok);
    }
    
    
    
    /**
     * Load and initialize all servlets marked "load on startup" in the
     * web application deployment descriptor.
     *
     * @param children Array of wrappers for all currently defined
     *  servlets (including those not declared load on startup)
     */
    public void loadOnStartup(Container children[]) {
    	
    	// Collect "load on startup" servlets that need to be initialized
    	TreeMap map = new TreeMap();
    	
    	for (int i = 0; i < children.length; i++) {
    		Wrapper wrapper = (Wrapper) children[i];
    		int loadOnStartup = wrapper.getLoadOnStartup();
    		if (loadOnStartup < 0)
    			continue;
    		
    		Integer key = Integer.valueOf(loadOnStartup);
    		ArrayList list = (ArrayList) map.get(key);
    		if (list == null) {
                list = new ArrayList();
                map.put(key, list);
            }
            list.add(wrapper);
    	}
    	
    	// Load the collected "load on startup" servlets
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
        	Integer key = (Integer) keys.next();
            ArrayList list = (ArrayList) map.get(key);
            Iterator wrappers = list.iterator();
            while (wrappers.hasNext()) {
            	 Wrapper wrapper = (Wrapper) wrappers.next();
            	 try {
            		 wrapper.load();
            	 }catch (ServletException e) {
            		 
            	 }
            }
        }
    	
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
        
        
        
        // add listener manually
        addApplicationListener("listeners.SessionListener");
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
         
         
         if (webappResources == null) {   // (1) Required by Loader
        	 
        	 try {
        		 if((docBase != null) && (docBase.endsWith(".war")) &&
        				 (!(new File(getBasePath())).isDirectory()));
        		 else
        			 setResources(new FileDirContext());
        		 
        	 }catch (IllegalArgumentException e) {
        		 ok = false;
        	 }
         }
         
         if (ok) {
        	 if (!resourcesStart()) {
        		 log.error( "Error in resourceStart()");
                 ok = false;
        	 }
         }
         
         
         
         /*
          *  realm part!
          *  
          *  implements latter
          */
         
         
         
         if (getLoader() == null) {
        	 
        	 WebappLoader webappLoader = new WebappLoader(getParentClassLoader());
        	 setLoader(webappLoader);
         }
         
         
         
      	// Post work directory
         postWorkDirectory();
         
         
         try {
        	 if (ok) {
        		 started = true;
        		 
        		 // Start our subordinate components
        		// Start our subordinate components, if any
                 if ((loader != null) && (loader instanceof Lifecycle))
                     ((Lifecycle) loader).start();
        		 
        		 
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
        		
                
                
                // Acquire clustered manager
                Manager contextManager = null;
                if (manager == null) {
                	
                	// forclustered manager
                	if ( manager == null ) {
                		if ( (getCluster() != null) && distributable) {
                			try {
                				contextManager = getCluster().createManager(getName());
                			}
                			catch (Exception ex) {
                				
                			}
                		}
                		else
                            contextManager = new StandardManager();
                	}
                	
                }
                
                // Configure default manager if none was specified
                if (contextManager != null) {
                	setManager(contextManager);
                }
                
        		 
        	 }
         }
         finally {
        	 ;
         }
         
         // We put the resources into the servlet context
         if (ok)
             getServletContext().setAttribute
                 (Globals.RESOURCES_ATTR, getResources());
         
         
         
         
         // Configure and call application event listeners
         if (ok) {
        	 if (!listenerStart()) {
                 log.error( "Error listenerStart");
                 ok = false;
             }
         }
         
         
         try {
             // Start manager
        	 if ((manager != null) && (manager instanceof Lifecycle)) {
        		 ((Lifecycle) getManager()).start();
        	 }
        	// Start ContainerBackgroundProcessor thread
             super.threadStart();
         }
         catch(Exception e) {
             log.error("Error manager.start()", e);
             ok = false;
         }
         
         
         
         
         
         // Load and initialize all "load on startup" servlets
         if (ok) {
        	 loadOnStartup(findChildren());
         }
         
         
         
         // Set available status depending upon startup success
         if (ok) {
        	 setAvailable(true);
         }
         else {
        	 log.error("standardContext.startFailed");
             try {
                 stop();
             } catch (Throwable t) {
                 log.error("standardContext.startCleanup");
             }
             setAvailable(false);
         }
         
         
         // JMX registration
         registerJMX();
  
    }
    
    
    /**
     * Stop this Context component.
     *
     * @exception LifecycleException if a shutdown error occurs
     */
    public synchronized void stop() throws LifecycleException {
    	// Validate and update our current component state
        if (!started) {
            if(log.isInfoEnabled())
                log.info("containerBase.notStarted");
            return;
        }
        
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);
        
        // Mark this application as unavailable while we shut down
        setAvailable(false);
        
        // Binding thread
        //ClassLoader oldCCL = bindThread();
        
        try {
        	// Stop our child containers, if any
            Container[] children = findChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof Lifecycle)
                    ((Lifecycle) children[i]).stop();
            }
            
            // Stop our filters
            //filterStop();

            // Stop ContainerBackgroundProcessor thread
            super.threadStop();
            
            if ((manager != null) && (manager instanceof Lifecycle)) {
                ((Lifecycle) manager).stop();
            }
            
            // Stop our application listeners
            listenerStop();
            
            // Finalize our character set mapper
            //setCharsetMapper(null);
            
            lifecycle.fireLifecycleEvent(STOP_EVENT, null);
            started = false;
            
            // Stop the Valves in our pipeline (including the basic), if any
            if (pipeline instanceof Lifecycle) {
                ((Lifecycle) pipeline).stop();
            }
            
            // Clear all application-originated servlet context attributes
            if (context != null)
                context.clearAttributes();
            
            // Stop resources
            resourcesStop();
            
            if ((cluster != null) && (cluster instanceof Lifecycle)) {
                ((Lifecycle) cluster).stop();
            }
            
            if ((loader != null) && (loader instanceof Lifecycle)) {
                ((Lifecycle) loader).stop();
            }
        }
        finally {
        	
        }
        
        
        // Reset application context
        context = null;

        // This object will no longer be visible or used. 
        try {
            resetContext();
        } catch( Exception ex ) {
            log.error( "Error reseting context " + this + " " + ex, ex );
        }
    }
    
    private void resetContext(){
    	children=new HashMap();

        // Bugzilla 32867
        distributable = false;

        applicationListeners = new String[0];
        applicationEventListenersObjects = new Object[0];
        applicationLifecycleListenersObjects = new Object[0];

        if(log.isDebugEnabled())
            log.debug("resetContext " + oname);
    }
    
    
    
    /**
     * Deallocate resources and destroy proxy.
     */
    public boolean resourcesStop() {
    	 boolean ok = true;
    	 
    	 try {
    		 if (resources != null) {
    			 if (resources instanceof Lifecycle) {
                     ((Lifecycle) resources).stop();
                 }
    			 if (webappResources instanceof BaseDirContext) {
                     ((BaseDirContext) webappResources).release();
                 }
    			 
    			 // Unregister the cache in JMX
    			 if (isCachingAllowed()) {
                     ObjectName resourcesName = 
                         new ObjectName(this.getDomain()
                                        + ":type=Cache,host=" 
                                        + getHostname() + ",path=" 
                                        + (("".equals(getPath()))?"/"
                                           :getPath()));
                     Registry.getRegistry(null, null)
                         .unregisterComponent(resourcesName);
    			 }
    		 }
    	 }
    	 catch (Throwable t) {
    		 log.error("standardContext.resourcesStop");
             ok = false;
    	 }
    	 
    	 this.resources = null;

         return (ok);
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
    
    
    
    /**
     * Configure the set of instantiated application event listeners
     * for this Context.  Return <code>true</code> if all listeners wre
     * initialized successfully, or <code>false</code> otherwise.
     */
    public boolean listenerStart() {
    	
    	return true;
        
    }
    
    /**
     * Send an application stop event to all interested listeners.
     * Return <code>true</code> if all events were sent successfully,
     * or <code>false</code> otherwise.
     */
    public boolean listenerStop() {
    	return true;
    }
    
    
}
