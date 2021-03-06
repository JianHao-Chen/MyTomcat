package My.catalina.loader;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.jar.JarFile;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Globals;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Loader;
import My.catalina.core.StandardContext;
import My.catalina.util.LifecycleSupport;
import My.naming.resources.DirContextURLStreamHandler;
import My.naming.resources.Resource;

/**
 * Classloader implementation which is specialized for handling web
 * applications in the most efficient way, while being Catalina aware (all
 * accesses to resources are made through the DirContext interface).
 * This class loader supports detection of modified
 * Java classes, which can be used to implement auto-reload support.
 * <p>
 * This class loader is configured by adding the pathnames of directories,
 * JAR files, and ZIP files with the <code>addRepository()</code> method,
 * prior to calling <code>start()</code>.  When a new class is required,
 * these repositories will be consulted first to locate the class.  If it
 * is not present, the system class loader will be used instead.
 */

public class WebappLoader implements Lifecycle, Loader{
	
	
	private static My.juli.logging.Log log=
        My.juli.logging.LogFactory.getLog( WebappLoader.class );

	// --------------------- Constructors ---------------------
	 /**
     * Construct a new WebappLoader with no defined parent class loader
     * (so that the actual parent will be the system class loader).
     */
    public WebappLoader() {

        this(null);

    }
    
    
    /**
     * Construct a new WebappLoader with the specified class loader
     * to be defined as the parent of the ClassLoader we ultimately create.
     *
     * @param parent The parent class loader
     */
    public WebappLoader(ClassLoader parent) {
        super();
        this.parentClassLoader = parent;
    }
	
    
	// ---------------------- Instance Variables ----------------------
    
    /**
     * The Container with which this Loader has been associated.
     */
    private Container container = null;
    
    /**
     * The "follow standard delegation model" flag that will be used to
     * configure our ClassLoader.
     */
    private boolean delegate = false;
    
    
    /**
     * The class loader being managed by this Loader component.
     */
    private WebappClassLoader classLoader = null;
    
    
    
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    
    /**
     * The parent class loader of the class loader we will create.
     */
    private ClassLoader parentClassLoader = null;
    
    
    /**
     * The reloadable flag for this Loader.
     */
    private boolean reloadable = false;
    
    
    /**
     * The set of repositories associated with this class loader.
     */
    private String repositories[] = new String[0];
    
    
    /**
     * Has this component been started?
     */
    private boolean started = false;
    
    
    /**
     * Classpath set in the loader.
     */
    private String classpath = null;
    
    
    /**
     * The Java class name of the ClassLoader implementation to be used.
     * This class should extend WebappClassLoader, otherwise, a different 
     * loader implementation must be used.
     */
    private String loaderClass =
        "My.catalina.loader.WebappClassLoader";
    
    
    /**
     * Repositories that are set in the loader, for JMX.
     */
    private ArrayList loaderRepositories = null;
    
    /**
     * Whether we should search the external repositories first
     */
    private boolean searchExternalFirst = false;
    
    
	// ---------------------- Properties ----------------------
    
    /**
     * Return the Container with which this Logger has been associated.
     */
    public Container getContainer() {

        return (container);

    }

    /**
     * Set the Container with which this Logger has been associated.
     */
    public void setContainer(Container container) {
    	
    	this.container = container;
    	
    	// Register with the new Container (if any)
    	if ((this.container != null) && (this.container instanceof Context)) {
    		setReloadable( ((Context) this.container).getReloadable() );
    	}
    }
    
    
    /**
     * Return the reloadable flag for this Loader.
     */
    public boolean getReloadable() {
    	
    	return (this.reloadable);
    }
    
    
    /**
     * Set the reloadable flag for this Loader.
     */
    public void setReloadable(boolean reloadable) {
    	
    	this.reloadable = reloadable;
    }
    
    	
    /**
     * Return the Java class loader to be used by this Container.
     */
    public ClassLoader getClassLoader() {

        return ((ClassLoader) classLoader);

    }
    
    
    
	// ------------------- Public Methods -------------------
    
    /**
     * Add a new repository to the set of repositories for this class loader.
     *
     * @param repository Repository to be added
     */
    public void addRepository(String repository) {
    	
    	for (int i = 0; i < repositories.length; i++) {
    		if (repository.equals(repositories[i]))
    			return;
    	}
    	
    	
    	String results[] = new String[repositories.length + 1];
    	
    	for (int i = 0; i < repositories.length; i++)
            results[i] = repositories[i];
    	
    	results[repositories.length] = repository;
        repositories = results;
        
        
        
    }
    
    
    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess() {
    	if (reloadable && modified()) {
    		try{
    			Thread.currentThread().setContextClassLoader
    				(WebappLoader.class.getClassLoader());
    			
    			if (container instanceof StandardContext) {
    				((StandardContext) container).reload();
    			}
    		}
    		finally {
    			if (container.getLoader() != null) {
    				Thread.currentThread().setContextClassLoader
                    	(container.getLoader().getClassLoader());
    			}
    		}
    	}
    }
    
    
    /**
     * Has the internal repository associated with this Loader been modified,
     * such that the loaded classes should be reloaded?
     */
    public boolean modified() {
    	return (classLoader.modified());
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

	
	
	
	private boolean initialized = false;
	
	public void init() {
		
		initialized = true;
		
		
		
	}
	
	
	/**
     * Start this component, initializing our associated class loader.
     */

	@Override
	public void start() throws LifecycleException {
		if( ! initialized ) 
			init();
		
		if (started)
			throw new LifecycleException("webappLoader.alreadyStarted");
	
		lifecycle.fireLifecycleEvent(START_EVENT, null);
		
		started = true;
		
		
		// Construct a class loader based on our current repositories list
		
		try {
			
			classLoader = createClassLoader();
			classLoader.setResources(container.getResources());
			classLoader.setDelegate(this.delegate);
			classLoader.setSearchExternalFirst(searchExternalFirst);
			
			if (container instanceof StandardContext) {
				classLoader.setAntiJARLocking(
                        ((StandardContext) container).getAntiJARLocking());
                classLoader.setClearReferencesStopThreads(
                        ((StandardContext) container).getClearReferencesStopThreads());
                classLoader.setClearReferencesStopTimerThreads(
                        ((StandardContext) container).getClearReferencesStopTimerThreads());
                classLoader.setClearReferencesThreadLocals(
                        ((StandardContext) container).getClearReferencesThreadLocals());
                classLoader.setClearReferencesHttpClientKeepAliveThread(
                        ((StandardContext) container).getClearReferencesHttpClientKeepAliveThread());
			}
			
			
			for (int i = 0; i < repositories.length; i++) {
				classLoader.addRepository(repositories[i]);
			}
			
			
			// Configure our repositories
			setRepositories();
			//setClassPath();
			
			
			if (classLoader instanceof Lifecycle)
                ((Lifecycle) classLoader).start();
			
		}catch (Throwable t) {
			
		}
		
	}


	 /**
     * Stop this component, finalizing our associated class loader.
     */
	public void stop() throws LifecycleException {
		// Validate and update our current component state
        if (!started)
            throw new LifecycleException("webappLoader.notStarted");
        
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;
        
        // Remove context attributes as appropriate
        if (container instanceof Context) {
        	ServletContext servletContext =
                ((Context) container).getServletContext();
        	
        	servletContext.removeAttribute(Globals.CLASS_PATH_ATTR);
        }
        
        // Throw away our current class loader
        if (classLoader instanceof Lifecycle)
        	((Lifecycle) classLoader).stop();
        
        //DirContextURLStreamHandler.unbind((ClassLoader) classLoader);
	
        
        classLoader = null;
        
        initialized = false;
	}
    
    
    
    
	// ----------------------- Reloader Methods -----------------
    
    
	// ----------------------- Private Methods ------------------
	
	/**
     * Create associated classLoader.
     */
    private WebappClassLoader createClassLoader()
        throws Exception {
    	
    	Class clazz = Class.forName(loaderClass);
    	WebappClassLoader classLoader = null;
    	Class[] argTypes = { ClassLoader.class };
    	Object[] args = { parentClassLoader };
        Constructor constr = clazz.getConstructor(argTypes);
        classLoader = (WebappClassLoader) constr.newInstance(args);
        
        return classLoader;
    }
    
    
    
    /**
     * Configure the repositories for our class loader, based on the
     * associated Context.
     * @throws IOException 
     */
    private void setRepositories() throws IOException {
    	
    	if (!(container instanceof Context))
    		return;
    	
    	ServletContext servletContext =
            ((Context) container).getServletContext();
    	
    	if (servletContext == null)
            return;
    	
    	loaderRepositories=new ArrayList();
    	
    	// Loading the work directory
    	File workDir =
            (File) servletContext.getAttribute(Globals.WORK_DIR_ATTR);
    	
    	if (workDir == null) {
            log.info("No work dir for " + servletContext);
        }
    	
    	classLoader.setWorkDir(workDir);
    	
    	DirContext resources = container.getResources();
    	
    	// Setting up the class repository (/WEB-INF/classes), if it exists
        String classesPath = "/WEB-INF/classes";
        
        DirContext classes = null;
        
        
        
        try {
        	Object object = resources.lookup(classesPath);
        	if (object instanceof DirContext) {
        		classes = (DirContext) object;
        	}
        }catch(NamingException e) {
        	// Silent catch: it's valid that no /WEB-INF/classes collection
            // exists
        }
        
        if (classes != null) {
        	
        	 File classRepository = null;

             String absoluteClassesPath =
                 servletContext.getRealPath(classesPath);
             
             if (absoluteClassesPath != null) {
            	 classRepository = new File(absoluteClassesPath);
             }else {
            	 
             }
             
             // Adding the repository to the class loader
             classLoader.addRepository(classesPath + "/", classRepository);
             loaderRepositories.add(classesPath + "/" );
        }
        
        
        // Setting up the JAR repository (/WEB-INF/lib), if it exists
        
       /* String libPath = "/WEB-INF/lib";
        
        DirContext libDir = null;
        // Looking up directory /WEB-INF/lib in the context
        
        try {
        	Object object = resources.lookup(libPath);
        	 if (object instanceof DirContext)
                 libDir = (DirContext) object;	 
        }catch(NamingException e) {
        	 // Silent catch: it's valid that no /WEB-INF/lib collection
            // exists
        }
        
        
        if (libDir != null) {
        	
        	boolean copyJars = false;
        	String absoluteLibPath = servletContext.getRealPath(libPath);
        	
        	File destDir = null;
        	
        	if (absoluteLibPath != null) {
                destDir = new File(absoluteLibPath);
            } else {
                copyJars = true;
                destDir = new File(workDir, libPath);
                destDir.mkdirs();
            }
        	
        	
        	// Looking up directory /WEB-INF/lib in the context
        	NamingEnumeration<NameClassPair> enumeration = null;
        	
        	try {
        		 enumeration = libDir.list("");
        		 
        	}catch (NamingException e) {
        		throw new IOException("webappLoader.namingFailure");
        	}
        	
        	
        	while (enumeration.hasMoreElements()) {
        		 
        		NameClassPair ncPair = enumeration.nextElement();
        		String filename = libPath + "/" + ncPair.getName();
        		
        		if (!filename.endsWith(".jar"))
                    continue;
        		
        		// Copy JAR in the work directory, always (the JAR file
                // would get locked otherwise, which would make it
                // impossible to update it or remove it at runtime)
        		
        		File destFile = new File(destDir, ncPair.getName());
        		
        		
        		Object obj = null;
                try {
                    obj = libDir.lookup(ncPair.getName());
                } catch (NamingException e) {
                	
                }
                
                if (!(obj instanceof Resource))
                    continue;
                
                Resource jarResource = (Resource) obj;
                
                if (copyJars) {
                	//...
                }
                
                
                try {
                	// currently, don't add jar files.
                	
                	JarFile jarFile = new JarFile(destFile);
                	classLoader.addJar(filename, jarFile, destFile);
                }
                catch (Exception ex) {
                	
                }
        	}
        	
        }*/
        
        
        
    }
    
}
