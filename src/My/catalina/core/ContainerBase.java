package My.catalina.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import My.catalina.Container;
import My.catalina.ContainerListener;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.Loader;
import My.catalina.Manager;
import My.catalina.Pipeline;
import My.catalina.util.LifecycleSupport;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public abstract class ContainerBase 
	implements Container,Lifecycle,Pipeline,Serializable{

    private static My.juli.logging.Log log=
        My.juli.logging.LogFactory.getLog( ContainerBase.class );
	
	// ----------------------------------------------------- Instance Variables
    /**
     * The child Containers belonging to this Container, keyed by name.
     */
    protected HashMap children = new HashMap();
    
    /**
     * The processor delay for this component.
     */
    protected int backgroundProcessorDelay = -1;
    
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    /**
     * The container event listeners for this Container.
     */
    protected ArrayList listeners = new ArrayList();
    
    
    /**
     * The Loader implementation with which this Container is associated.
     */
    protected Loader loader = null;


    /**
     * The Logger implementation with which this Container is associated.
     */
    protected Log logger = null;
    
    
    /**
     * The Manager implementation with which this Container is associated.
     */
    protected Manager manager = null;
    
    
    /**
     * The parent Container to which this Container is a child.
     */
    protected Container parent = null;
    
    
    /**
     * The parent class loader to be configured when we install a Loader.
     */
    protected ClassLoader parentClassLoader = null;


    /**
     * The Pipeline object with which this Container is associated.
     */
    // implements this latter.
    //  protected Pipeline pipeline = new StandardPipeline(this);
    
    
    
    
    /**
     * Has this component been started?
     */
    protected boolean started = false;

    protected boolean initialized=false;
    
    
    /**
     * Will children be started automatically when they are added.
     */
    protected boolean startChildren = true;
    
    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false;
    
    
    
	// ------------------------------- Properties
    
    /**
     * Get the delay between the invocation of the backgroundProcess method on
     * this container and its children. Child containers will not be invoked
     * if their delay value is not negative (which would mean they are using 
     * their own thread). Setting this to a positive value will cause 
     * a thread to be spawn. After waiting the specified amount of time, 
     * the thread will invoke the executePeriodic method on this container 
     * and all its children.
     */
    public int getBackgroundProcessorDelay() {
        return backgroundProcessorDelay;
    }
    
    public void setBackgroundProcessorDelay(int delay) {
        backgroundProcessorDelay = delay;
    }
    
    
    /**
     * Return the Loader with which this Container is associated.  If there is
     * no associated Loader, return the Loader associated with our parent
     * Container (if any); otherwise, return <code>null</code>.
     */
    public Loader getLoader() {

        if (loader != null)
            return (loader);
        if (parent != null)
            return (parent.getLoader());
        return (null);

    }
    
    /**
     * Set the Loader with which this Container is associated.
     */
    public synchronized void setLoader(Loader loader) {

        // Change components if necessary
        Loader oldLoader = this.loader;
        if (oldLoader == loader)
            return;
        this.loader = loader;
        
        
        // Stop the old component if necessary
        if (started && (oldLoader != null) &&
                (oldLoader instanceof Lifecycle)) {
        	try {
                ((Lifecycle) oldLoader).stop();
        	}catch (LifecycleException e) {
        		log.error("ContainerBase.setLoader: stop: ", e);
        	}
        }
        
        // Start the new component if necessary
        if (loader != null)
            loader.setContainer(this);
        if (started && (loader != null) &&
            (loader instanceof Lifecycle)) {
            try {
                ((Lifecycle) loader).start();
            } catch (LifecycleException e) {
                log.error("ContainerBase.setLoader: start: ", e);
            }
        }
        
    }
    
    
    /**
     * Return the Logger with which this Container is associated.  If there is
     * no associated Logger, return the Logger associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Log getLogger() {

        if (logger != null)
            return (logger);
        logger = LogFactory.getLog("");
        return (logger);

    }
    
    
    /**
     * Return the Manager with which this Container is associated.  If there is
     * no associated Manager, return the Manager associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Manager getManager() {

        if (manager != null)
            return (manager);
        if (parent != null)
            return (parent.getManager());
        return (null);

    }
    
    /**
     * Set the Manager with which this Container is associated.
     *
     * @param manager The newly associated Manager
     */
    public synchronized void setManager(Manager manager) {
    	// Change components if necessary
        Manager oldManager = this.manager;
        if (oldManager == manager)
            return;
        this.manager = manager;

        // Stop the old component if necessary
        if (started && (oldManager != null) &&
            (oldManager instanceof Lifecycle)) {
            try {
                ((Lifecycle) oldManager).stop();
            } catch (LifecycleException e) {
                log.error("ContainerBase.setManager: stop: ", e);
            }
        }

        // Start the new component if necessary
        if (manager != null)
            manager.setContainer(this);
        if (started && (manager != null) &&
            (manager instanceof Lifecycle)) {
            try {
                ((Lifecycle) manager).start();
            } catch (LifecycleException e) {
                log.error("ContainerBase.setManager: start: ", e);
            }
        }
    }
    
    
    /**
     * Return if children of this container will be started automatically when
     * they are added to this container.
     */
    public boolean getStartChildren() {

        return (startChildren);

    }


    /**
     * Set if children of this container will be started automatically when
     * they are added to this container.
     *
     * @param startChildren New value of the startChildren flag
     */
    public void setStartChildren(boolean startChildren) {

        boolean oldStartChildren = this.startChildren;
        this.startChildren = startChildren;
    }

    
    /**
     * Return the Container for which this Container is a child, if there is
     * one.  If there is no defined parent, return <code>null</code>.
     */
    public Container getParent() {

        return (parent);

    }
    
    
    /**
     * Set the parent Container to which this Container is being added as a
     * child.  This Container may refuse to become attached to the specified
     * Container by throwing an exception.
     */
    public void setParent(Container container) {

        Container oldParent = this.parent;
        this.parent = container;
    }
    
    
    
    /**
     * Return the parent class loader (if any) for this web application.
     * This call is meaningful only <strong>after</strong> a Loader has
     * been configured.
     */
    public ClassLoader getParentClassLoader() {
        if (parentClassLoader != null)
            return (parentClassLoader);
        if (parent != null) {
            return (parent.getParentClassLoader());
        }
        return (ClassLoader.getSystemClassLoader());

    }


    /**
     * Set the parent class loader (if any) for this web application.
     * This call is meaningful only <strong>before</strong> a Loader has
     * been configured, and the specified value (if non-null) should be
     * passed as an argument to the class loader constructor.
     *
     *
     * @param parent The new parent class loader
     */
    public void setParentClassLoader(ClassLoader parent) {
        ClassLoader oldParentClassLoader = this.parentClassLoader;
        this.parentClassLoader = parent;
    }
    
    
    
    
    
    // --------------------- Container Methods ---------------------
    
    
    
    /**
     * Add a new child Container to those associated with this Container
     */
    public void addChild(Container child) {
    	
    	synchronized(children) {
    		 if (children.get(child.getName()) != null)
                 throw new IllegalArgumentException("addChild:  Child name '" +
                                                    child.getName() +
                                                    "' is not unique");
    		 child.setParent(this);  // May throw IAE
             children.put(child.getName(), child);
             
          // Start child
             if (started && startChildren && (child instanceof Lifecycle)) {
            	 
            	 boolean success = false;
            	 
            	 try {
            		 ((Lifecycle) child).start();
            		 success = true;
            	 }
            	 catch (LifecycleException e) {
                     log.error("ContainerBase.addChild: start: ", e);
                     throw new IllegalStateException
                         ("ContainerBase.addChild: start: " + e);
            	 }
            	 finally {
                     if (!success) {
                         children.remove(child.getName());
                     }
            	 }
             }
             
             fireContainerEvent(ADD_CHILD_EVENT, child);
    	}
    }
    
    
    /**
     * Add a container event listener to this component.
     */
    public void addContainerListener(ContainerListener listener) {

        synchronized (listeners) {
            listeners.add(listener);
        }

    }
    
    
    /**
     * Return the child Container, associated with this Container, with
     * the specified name (if any); otherwise, return <code>null</code>
     */
    public Container findChild(String name) {

        if (name == null)
            return (null);
        synchronized (children) {       // Required by post-start changes
            return ((Container) children.get(name));
        }

    }
    
    
    /**
     * Return the set of children Containers associated with this Container.
     * If this Container has no children, a zero-length array is returned.
     */
    public Container[] findChildren() {

        synchronized (children) {
            Container results[] = new Container[children.size()];
            return ((Container[]) children.values().toArray(results));
        }

    }
    
    
    /**
     * Return the set of container listeners associated with this Container.
     * If this Container has no registered container listeners, a zero-length
     * array is returned.
     */
    public ContainerListener[] findContainerListeners() {

        synchronized (listeners) {
            ContainerListener[] results = 
                new ContainerListener[listeners.size()];
            return ((ContainerListener[]) listeners.toArray(results));
        }

    }
    
    
    
    
    
    
    
	// ------------------------- Protected Methods -------------------------
    
    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     */
    public void fireContainerEvent(String type, Object data) {
    	
    	 // implements latter!
    }
    
    
    
    
}
