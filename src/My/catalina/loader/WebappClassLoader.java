package My.catalina.loader;

import java.net.URL;
import java.net.URLClassLoader;

import javax.naming.directory.DirContext;

import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;

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
     * The parent class loader.
     */
    protected ClassLoader parent = null;
    
    
    /**
     * The system class loader.
     */
    protected ClassLoader system = null;
    
    
    /**
     * Has this component been started?
     */
    protected boolean started = false;
    
    
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
     * Set associated resources.
     */
    public void setResources(DirContext resources) {

        this.resources = resources;

        if (resources instanceof ProxyDirContext) {
            contextName = ((ProxyDirContext) resources).getContextName();
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
