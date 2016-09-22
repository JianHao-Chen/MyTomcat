package My.catalina.loader;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Loader;
import My.catalina.util.LifecycleSupport;

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
			
		}
		
	}


	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
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
    
}
