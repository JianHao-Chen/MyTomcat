package My.catalina.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Globals;
import My.catalina.Host;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.Loader;
import My.catalina.Wrapper;
import My.catalina.loader.WebappLoader;
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
     * The application available flag for this Context.
     */
    private boolean available = false;
    
    /**
     * The ServletContext implementation associated with this Context.
     */
    protected transient ApplicationContext context = null;
    
    
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
