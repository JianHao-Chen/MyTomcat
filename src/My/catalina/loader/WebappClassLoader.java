package My.catalina.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.naming.directory.DirContext;

import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.naming.resources.ProxyDirContext;

/**
 * Specialized web application class loader.
 * <p>
 * This class loader is a full reimplementation of the 
 * <code>URLClassLoader</code> from the JDK. It is designed to be fully
 * compatible with a normal <code>URLClassLoader</code>, although its internal
 * behavior may be completely different.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - This class loader faithfully follows 
 * the delegation model recommended in the specification. The system class 
 * loader will be queried first, then the local repositories, and only then 
 * delegation to the parent class loader will occur. This allows the web 
 * application to override any shared class except the classes from J2SE.
 * Special handling is provided from the JAXP XML parser interfaces, the JNDI
 * interfaces, and the classes from the servlet API, which are never loaded 
 * from the webapp repository.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Due to limitations in Jasper 
 * compilation technology, any repository which contains classes from 
 * the servlet API will be ignored by the class loader.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - The class loader generates source
 * URLs which include the full JAR URL when a class is loaded from a JAR file,
 * which allows setting security permission at the class level, even when a
 * class is contained inside a JAR.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Local repositories are searched in
 * the order they are added via the initial constructor and/or any subsequent
 * calls to <code>addRepository()</code> or <code>addJar()</code>.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - No check for sealing violations or
 * security is made unless a security manager is present.
 */



public class WebappClassLoader 
	extends URLClassLoader
	implements Reloader, Lifecycle{

	protected static My.juli.logging.Log log=
        My.juli.logging.LogFactory.getLog( WebappClassLoader.class );
	
	
	
	
	// ------------------------ Constructors ------------------------------
	
	/**
     * Construct a new ClassLoader with no defined repositories and no
     * parent ClassLoader.
     */
    public WebappClassLoader() {
    	
    	 super(new URL[0]);
    	 this.parent = getParent();
    	 system = getSystemClassLoader();
    	 
    }
    
    /**
     * Construct a new ClassLoader with no defined repositories and the given
     * parent ClassLoader.
     */
    public WebappClassLoader(ClassLoader parent) {
    	
    	super(new URL[0], parent);
    	this.parent = getParent();
    	
    	system = getSystemClassLoader();
    }
    
    
	// --------------------- Instance Variables ---------------------
    
    /**
     * Associated directory context giving access to the resources in this
     * webapp.
     */
    protected DirContext resources = null;
    
    
    /**
     * Should this class loader delegate to the parent class loader
     * <strong>before</strong> searching its own repositories (i.e. the
     * usual Java2 delegation model)?  If set to <code>false</code>,
     * this class loader will search its own repositories first, and
     * delegate to the parent only if the class or resource is not
     * found locally.
     */
    protected boolean delegate = false;
    
    
    
    /**
     * The parent class loader.
     */
    protected ClassLoader parent = null;
    
    
    /**
     * The system class loader.
     */
    protected ClassLoader system = null;
    
    
    /**
     * Has external repositories.
     */
    protected boolean hasExternalRepositories = false;

    /**
     * Search external repositories first
     */
    protected boolean searchExternalFirst = false;
    
    
    /**
     * Has this component been started?
     */
    protected boolean started = false;
    
    
    /**
     * Use anti JAR locking code, which does URL rerouting when accessing
     * resources.
     */
    boolean antiJARLocking = false; 
    
    
    /**
     * Path where resources loaded from JARs will be extracted.
     */
    protected File loaderDir = null;
    protected String canonicalLoaderDir = null;
    
    
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
     * If an HttpClient keep-alive timer thread has been started by this web
     * application and is still running, should Tomcat change the context class
     * loader from the current {@link WebappClassLoader} to
     * {@link WebappClassLoader#parent} to prevent a memory leak? Note that the
     * keep-alive timer thread will stop on its own once the keep-alives all
     * expire however, on a busy system that might not happen for some time.
     */
    private boolean clearReferencesHttpClientKeepAliveThread = true;
    
    
    /**
     * Name of associated context used with logging and JMX to associate with
     * the right web application. Particularly useful for the clear references
     * messages. Defaults to unknown but if standard Tomcat components are used
     * it will be updated during initialisation from the resources.
     */
    private String contextName = "unknown";
    

    
	// ------------------------- Properties -------------------------
    
    public void addRepository(String repository) {
    	
    }
    
    
    /**
     * Get associated resources.
     */
    public DirContext getResources() {

        return this.resources;

    }
    
    
    /**
     * Return the "delegate first" flag for this class loader.
     */
    public boolean getDelegate() {

        return (this.delegate);

    }


    /**
     * Set the "delegate first" flag for this class loader.
     *
     * @param delegate The new "delegate first" flag
     */
    public void setDelegate(boolean delegate) {

        this.delegate = delegate;

    }
    


    /**
     * Set associated resources.
     */
    public void setResources(DirContext resources) {

        this.resources = resources;

        if (resources instanceof ProxyDirContext) {
            contextName = ((ProxyDirContext) resources).getContextName();
        }
    }
    
    
    /**
     * @return Returns the antiJARLocking.
     */
    public boolean getAntiJARLocking() {
        return antiJARLocking;
    }
    
    /**
     * @param antiJARLocking The antiJARLocking to set.
     */
    public void setAntiJARLocking(boolean antiJARLocking) {
        this.antiJARLocking = antiJARLocking;
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
        this.clearReferencesStopTimerThreads = clearReferencesStopTimerThreads;
    }
    
    
    /**
     * Return the clearReferencesThreadLocals flag for this Context.
     */
    public boolean getClearReferencesThreadLocals() {
        return (this.clearReferencesThreadLocals);
    }


    /**
     * Set the clearReferencesThreadLocals feature for this Context.
     *
     * @param clearReferencesThreadLocals The new flag value
     */
    public void setClearReferencesThreadLocals(
            boolean clearReferencesThreadLocals) {
        this.clearReferencesThreadLocals = clearReferencesThreadLocals;
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
     * @return Returns the searchExternalFirst.
     */
    public boolean getSearchExternalFirst() {
        return searchExternalFirst;
    }

    /**
     * @param searchExternalFirst Whether external repositories should be searched first
     */
    public void setSearchExternalFirst(boolean searchExternalFirst) {
        this.searchExternalFirst = searchExternalFirst;
    }
    
    
    /**
     * Change the work directory.
     */
    public void setWorkDir(File workDir) {
        this.loaderDir = new File(workDir, "loader");
        if (loaderDir == null) {
            canonicalLoaderDir = null;
        } else { 
            try {
                canonicalLoaderDir = loaderDir.getCanonicalPath();
                if (!canonicalLoaderDir.endsWith(File.separator)) {
                    canonicalLoaderDir += File.separator;
                }
            } catch (IOException ioe) {
                canonicalLoaderDir = null;
            }
        }
    }
    
    
    
    

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

	@Override
	public void start() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] findRepositories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean modified() {
		// TODO Auto-generated method stub
		return false;
	}
    
    
    
	
}
