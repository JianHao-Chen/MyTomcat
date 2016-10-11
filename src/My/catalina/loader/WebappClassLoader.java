package My.catalina.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.jar.JarFile;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.naming.resources.ProxyDirContext;
import My.naming.resources.Resource;
import My.naming.resources.ResourceAttributes;

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
     * The cache of ResourceEntry for classes and resources we have loaded,
     * keyed by resource name.
     */
    protected HashMap resourceEntries = new HashMap();
    
    
    
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
     * The list of local repositories, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected String[] repositories = new String[0];
    
    
    
    
    /**
     * Repositories translated as path in the work directory (for Jasper
     * originally), but which is used to generate fake URLs should getURLs be
     * called.
     */
    protected File[] files = new File[0];
    
    
    
    /**
     * The path which will be monitored for added Jar files.
     */
    protected String jarPath = null;
    
    
    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected String[] jarNames = new String[0];
    
    
    /**
     * The list of resources which should be checked when checking for
     * modifications.
     */
    protected String[] paths = new String[0];
    
    
    /**
     * The list of JARs last modified dates, in the order they should be
     * searched for locally loaded classes or resources.
     */
    protected long[] lastModifiedDates = new long[0];
    
    
    
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
    
    
    
    /**
     * Add a new repository to the set of places this ClassLoader can look for
     * classes to be loaded.
     *
     * @param repository Name of a source of classes to be loaded, such as a
     *  directory pathname, a JAR file pathname, or a ZIP file pathname
     *
     * @exception IllegalArgumentException if the specified repository is
     *  invalid or does not exist
     */
    synchronized void addRepository(String repository, File file) {
    	
    	// Note : There should be only one (of course), but I think we should
        // keep this a bit generic

        if (repository == null)
            return;
        
        int i;
        
        
        // Add this repository to our internal list
        String[] result = new String[repositories.length + 1];
        for (i = 0; i < repositories.length; i++) {
            result[i] = repositories[i];
        }
        
        result[repositories.length] = repository;
        
        repositories = result;
        
        // Add the file to the list
        File[] result2 = new File[files.length + 1];
        
        for (i = 0; i < files.length; i++) {
            result2[i] = files[i];
        }
        result2[files.length] = file;
        
        files = result2;
        
    }
    
    
    /**
     * Change the Jar path.
     */
    public void setJarPath(String jarPath) {

        this.jarPath = jarPath;

    }
    
    
    synchronized void addJar(String jar, JarFile jarFile, File file)
    throws IOException {
    	
    	if (jar == null)
            return;
        if (jarFile == null)
            return;
        if (file == null)
            return;
        
        int i;
        
        if ((jarPath != null) && (jar.startsWith(jarPath))) {
        	
        	String jarName = jar.substring(jarPath.length());
        	while (jarName.startsWith("/"))
                jarName = jarName.substring(1);
        	
        	String[] result = new String[jarNames.length + 1];
        	
        	for (i = 0; i < jarNames.length; i++) {
                result[i] = jarNames[i];
            }
        	result[jarNames.length] = jarName;
            jarNames = result;
        }
        
        
        try {
        	// Register the JAR for tracking
        	
        	long lastModified =
                ((ResourceAttributes) resources.getAttributes(jar))
                .getLastModified();
        	
        	String[] result = new String[paths.length + 1];
            for (i = 0; i < paths.length; i++) {
                result[i] = paths[i];
            }
            result[paths.length] = jar;
            paths = result;

            long[] result3 = new long[lastModifiedDates.length + 1];
            for (i = 0; i < lastModifiedDates.length; i++) {
                result3[i] = lastModifiedDates[i];
            }
            result3[lastModifiedDates.length] = lastModified;
            lastModifiedDates = result3;
            
        }catch (NamingException e) {
        	
        }
    	
    }
    
    
    
    /**
     * Find the specified class in our local repositories, if possible.  If
     * not found, throw <code>ClassNotFoundException</code>.
     *
     * @param name Name of the class to be loaded
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class findClass(String name) throws ClassNotFoundException {
    	
    	if (log.isDebugEnabled())
            log.debug("    findClass(" + name + ")");
    	
    	// Cannot load anything from local repositories if class loader is stopped
        if (!started) {
            throw new ClassNotFoundException(name);
        }
        
        // Ask our superclass to locate this class, if possible
        // (throws ClassNotFoundException if it is not found)
        Class clazz = null;
        try {
        	
        	try {
        		clazz = findClassInternal(name);
        	}
        	catch(ClassNotFoundException cnfe) {
        		
        	}
        	
        	
        	if (clazz == null) {
        		if (log.isDebugEnabled())
                    log.debug("    --> Returning ClassNotFoundException");
                throw new ClassNotFoundException(name);
        	}
        }
        catch (ClassNotFoundException e) {
        	if (log.isTraceEnabled())
                log.trace("    --> Passing on ClassNotFoundException");
            throw e;
        }
        
        
        
    	
    }
    
    
    /**
     * Load the class with the specified name.  This method searches for
     * classes in the same manner as <code>loadClass(String, boolean)</code>
     * with <code>false</code> as the second argument.
     *
     * @param name Name of the class to be loaded
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class loadClass(String name) throws ClassNotFoundException {

        return (loadClass(name, false));

    }
    
    
    /**
     * Load the class with the specified name, searching using the following
     * algorithm until it finds and returns the class.  If the class cannot
     * be found, returns <code>ClassNotFoundException</code>.
     * <ul>
     * <li>Call <code>findLoadedClass(String)</code> to check if the
     *     class has already been loaded.  If it has, the same
     *     <code>Class</code> object is returned.</li>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>loadClass()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findClass()</code> to find this class in our locally
     *     defined repositories.</li>
     * <li>Call the <code>loadClass()</code> method of our parent
     *     class loader, if any.</li>
     * </ul>
     * If the class was found using the above steps, and the
     * <code>resolve</code> flag is <code>true</code>, this method will then
     * call <code>resolveClass(Class)</code> on the resulting Class object.
     *
     * @param name Name of the class to be loaded
     * @param resolve If <code>true</code> then resolve the class
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
    	
    	Class clazz = null;
    	
    	if (!started) {
    		try {
                throw new IllegalStateException();
            } catch (IllegalStateException e) {
                log.info("webappClassLoader.stopped");
            }
    	}
    	
    	
    	// (0) Check our previously loaded local class cache
    	clazz = findLoadedClass0(name);
    	if (clazz != null) {
    		 if (log.isDebugEnabled())
                 log.debug("  Returning class from cache");
    		 
    		 if (resolve)
                 resolveClass(clazz);
    		 
    		 return (clazz);
    	}
    	
    	
    	// (0.1) Check our previously loaded class cache
    	clazz = findLoadedClass(name);
    	if (clazz != null) {
    		if (log.isDebugEnabled())
                log.debug("  Returning class from cache");
    		 if (resolve)
                 resolveClass(clazz);
             return (clazz);
    	}
    	
    	
    	// (0.2) Try loading the class with the system class loader, to prevent
        //       the webapp from overriding J2SE classes
    	try {
    		
    		clazz = system.loadClass(name);
    		if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
    	}catch (ClassNotFoundException e) {
            // Ignore
        }
    	
    	
    	// (1) Delegate to our parent if requested
    	 if (delegate) {
    		 if (log.isDebugEnabled())
                 log.debug("  Delegating to parent classloader1 " + parent);
    		 
    		 //...
    	 }
    	 
    	// (2) Search local repositories
    	 if (log.isDebugEnabled())
             log.debug("  Searching local repositories");
         try {
        	 
        	 clazz = findClass(name);
         }
    	
    }
    
    
    
    
    
    
    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("WebappClassLoader\r\n");
        sb.append("  context: ");
        sb.append(contextName);
        sb.append("\r\n");
        sb.append("  delegate: ");
        sb.append(delegate);
        sb.append("\r\n");
        sb.append("  repositories:\r\n");
        if (repositories != null) {
            for (int i = 0; i < repositories.length; i++) {
                sb.append("    ");
                sb.append(repositories[i]);
                sb.append("\r\n");
            }
        }
        if (this.parent != null) {
            sb.append("----------> Parent Classloader:\r\n");
            sb.append(this.parent.toString());
            sb.append("\r\n");
        }
        return (sb.toString());

    }
    
    
    
    
    /**
     * Finds the class with the given name if it has previously been
     * loaded and cached by this class loader, and return the Class object.
     * If this class has not been cached, return <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    protected Class findLoadedClass0(String name) {
    	
    	ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
    	
    	if (entry != null) {
            return entry.loadedClass;
        }
    	 return (null);
    }
    
    
    /**
     * Validate a classname. As per SRV.9.7.2, we must restict loading of 
     * classes from J2SE (java.*) and classes of the servlet API 
     * (javax.servlet.*). That should enhance robustness and prevent a number
     * of user error (where an older version of servlet.jar would be present
     * in /WEB-INF/lib).
     * 
     * @param name class name
     * @return true if the name is valid
     */
    protected boolean validate(String name) {

        if (name == null)
            return false;
        if (name.startsWith("java."))
            return false;

        return true;

    }
    
    /**
     * Find specified class in local repositories.
     *
     * @return the loaded class, or null if the class isn't found
     */
    protected Class findClassInternal(String name)
        throws ClassNotFoundException {
    	
    	 if (!validate(name))
             throw new ClassNotFoundException(name);
    	 
    	 String tempPath = name.replace('.', '/');
         String classPath = tempPath + ".class";
         
         ResourceEntry entry = null;
         
         entry = findResourceInternal(name, classPath);
    	
         if (entry == null)
             throw new ClassNotFoundException(name);
         
         
         Class clazz = entry.loadedClass;
         
         if (clazz != null)
             return clazz;
         
         synchronized (this) {
        	 clazz = entry.loadedClass;
             if (clazz != null)
                 return clazz;
             
             if (entry.binaryContent == null)
                 throw new ClassNotFoundException(name);
             
             
             // Looking up the package
             // implements latter...
             
             try {
            	 clazz = defineClass(name, entry.binaryContent, 0,
                         entry.binaryContent.length, 
                         new CodeSource(entry.codeBase, entry.certificates));
            	 
             }catch (UnsupportedClassVersionError ucve) {
            	 
             }
             
             entry.loadedClass = clazz;
             entry.binaryContent = null;
             entry.source = null;
             entry.codeBase = null;
             entry.manifest = null;
             entry.certificates = null;
             
         }
         return clazz;
    }
    
    
    
    /**
     * Find specified resource in local repositories.
     *
     * @return the loaded resource, or null if the resource isn't found
     */
    protected ResourceEntry findResourceInternal(String name, String path) {
    	
    	 if (!started) {
             log.info("webappClassLoader.stopped");
             return null;
         }
    	 
    	 if ((name == null) || (path == null))
             return null;
    	 
    	 ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
         if (entry != null)
             return entry;
         
         int contentLength = -1;
         InputStream binaryStream = null;

         int repositoriesLength = repositories.length;
         
         int i;

         Resource resource = null;
         
         for (i = 0; (entry == null) && (i < repositoriesLength); i++) {
        	 
        	 try {
        		 
        		 String fullPath = repositories[i] + path;
        		 
        		 Object lookupResult = resources.lookup(fullPath);
        		 
        		 if (lookupResult instanceof Resource) {
                     resource = (Resource) lookupResult;
                 }
        		 
        		// Note : Not getting an exception here means the 
        		// resource was found
        		 entry = findResourceInternal(files[i], path);
        		 
        		 ResourceAttributes attributes =
                     (ResourceAttributes) resources.getAttributes(fullPath);
        		 
        		 contentLength = (int) attributes.getContentLength();
        		 entry.lastModified = attributes.getLastModified();
        		 
        		 if (resource != null) {
        			 
        		 
        			 try {
                         binaryStream = resource.streamContent();
        			 }catch (IOException e) {
                         return null;
                     }
        		 }
        		 
        	 }catch (NamingException e) {
             }
         }
         
         
         if ((entry == null) )
             return null;
         
         
         
         // Add the entry in the local resource repository
         synchronized (resourceEntries) {
        	 // Ensures that all the threads which may be in a race to load
             // a particular class all end up with the same ResourceEntry
             // instance
        	 
        	 ResourceEntry entry2 = (ResourceEntry) resourceEntries.get(name);
             if (entry2 == null) {
                 resourceEntries.put(name, entry);
             } else {
                 entry = entry2;
             }
         }
         
         return entry;
    }
    
    
    
    /**
     * Find specified resource in local repositories.
     *
     * @return the loaded resource, or null if the resource isn't found
     */
    protected ResourceEntry findResourceInternal(File file, String path){
    	
    	ResourceEntry entry = new ResourceEntry();
    	 try {
    		 entry.source = getURI(new File(file, path));
    		 entry.codeBase = getURL(new File(file, path), false);
    	 }catch (MalformedURLException e) {
             return null;
         }   
         return entry;
    	
    }
    
    
    /**
     * Get URL.
     */
    protected URL getURI(File file)
        throws MalformedURLException {


        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }
        return realFile.toURI().toURL();

    }
    
    /**
     * Get URL.
     */
    protected URL getURL(File file, boolean encoded)
        throws MalformedURLException {

        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }
        if(encoded) {
            return getURI(realFile);
        } else {
            return realFile.toURL();
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
