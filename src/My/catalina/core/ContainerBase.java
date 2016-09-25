package My.catalina.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.directory.DirContext;
import javax.servlet.ServletException;

import My.catalina.Container;
import My.catalina.ContainerEvent;
import My.catalina.ContainerListener;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Loader;
import My.catalina.Manager;
import My.catalina.Pipeline;
import My.catalina.Valve;
import My.catalina.connector.Request;
import My.catalina.connector.Response;
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
     * The human-readable name of this Container.
     */
    protected String name = null;
    
    
    /**
     * The resources DirContext object with which this Container is associated.
     */
    protected DirContext resources = null;
    
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
      protected Pipeline pipeline = new StandardPipeline(this);
    
    
    
    
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
     * Return a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     */
    public String getName() {

        return (name);

    }


    /**
     * Set a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     *
     * @param name New name of this container
     *
     * @exception IllegalStateException if this Container has already been
     *  added to the children of a parent Container (after which the name
     *  may not be changed)
     */
    public void setName(String name) {

        String oldName = this.name;
        this.name = name;
    }
    
    
    
    /**
     * Return the resources DirContext object with which this Container is
     * associated.  If there is no associated resources object, return the
     * resources associated with our parent Container (if any); otherwise
     * return <code>null</code>.
    */
   public DirContext getResources() {
       if (resources != null)
           return (resources);
       if (parent != null)
           return (parent.getResources());
       return (null);

   }
   
   
   /**
    * Set the resources DirContext object with which this Container is
    * associated.
    *
    * @param resources The newly associated DirContext
    */
   public synchronized void setResources(DirContext resources) {
	   
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
    
    
    /**
     * Return the Pipeline object that manages the Valves associated with
     * this Container.
     */
    public Pipeline getPipeline() {

        return (this.pipeline);

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
    
    
    
    
    /**
     * Process the specified Request, to produce the corresponding Response,
     * by invoking the first Valve in our pipeline (if any), or the basic
     * Valve otherwise.
     */
    public void invoke(Request request, Response response)
    throws IOException, ServletException {
    	
    }
    
    
    /**
     * Return an object which may be utilized for mapping to this component.
     */
    public Object getMappingObject() {
        return this;
    }
    
    
    
    /**
     * Remove an existing child Container from association with this parent
     * Container.
     */
    public void removeChild(Container child) {

        if (child == null) {
            return;
        }
        
        synchronized(children) {
            if (children.get(child.getName()) == null)
                return;
            children.remove(child.getName());
        }
        
        if (started && (child instanceof Lifecycle)) {
            try {
                if( child instanceof ContainerBase ) {
                    if( ((ContainerBase)child).started ) {
                        ((Lifecycle) child).stop();
                    }
                } else {
                    ((Lifecycle) child).stop();
                }
            } catch (LifecycleException e) {
                log.error("ContainerBase.removeChild: stop: ", e);
            }
        }
    
    }
    
    
    
    /**
     * Remove a container event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeContainerListener(ContainerListener listener) {

        synchronized (listeners) {
            listeners.remove(listener);
        }

    }
    
    
    
    
	// --------------------- Lifecycle Methods ---------------------
    
    /**
     * Add a lifecycle event listener to this component.
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }
    
    
    
    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {
    	return lifecycle.findLifecycleListeners();
    }
    
    
    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }
    
    
    
    public void init() throws Exception {
    	
    	initialized=true;
    }
    
    
    
    /**
     * Prepare for active use of the public methods of this Component.
     */
    public synchronized void start() throws LifecycleException {
    	
    	// Validate and update our current component state
    	if (started) {
    		// already started
    		return;
    	}
    	
    	// Notify our interested LifecycleListeners
    	lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);
    	
    	started = true;
    	
    	/* Start our subordinate components, if any
    		
    		they include:
    			loader
    			logger
    			manager
    			cluster
    			realm
    			resources
    	*/
    	
    	
    	// Start our child containers, if any
    	Container children[] = findChildren();
    	for (int i = 0; i < children.length; i++) {
    		if (children[i] instanceof Lifecycle)
                ((Lifecycle) children[i]).start();
    	}
    	
    	
    	// Start the Valves in our pipeline (including the basic), if any
        if (pipeline instanceof Lifecycle)
            ((Lifecycle) pipeline).start();
        
        
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        
        
        // Start our thread
        threadStart();

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
        
    }
     
    /**
     * Gracefully shut down active use of the public methods of this Component.
     */
    public synchronized void stop() throws LifecycleException {
    	
    }
    
 
    
    
    
    
    // ---------------------- Pipeline Methods ----------------------
    
    /**
     * <p>Return the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).
     */
    public Valve getBasic() {

        return (pipeline.getBasic());

    }
    
    
    /**
     * <p>Set the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).  Prioer to setting the basic Valve,
     * the Valve's <code>setContainer()</code> will be called, if it
     * implements <code>Contained</code>, with the owning Container as an
     * argument.  The method may throw an <code>IllegalArgumentException</code>
     * if this Valve chooses not to be associated with this Container, or
     * <code>IllegalStateException</code> if it is already associated with
     * a different Container.</p>
     *
     * @param valve Valve to be distinguished as the basic Valve
     */
    public void setBasic(Valve valve) {

        pipeline.setBasic(valve);

    }
    
    
    /**
     * Add a new Valve to the end of the pipeline associated with this
     * Container.  Prior to adding the Valve, the Valve's
     * <code>setContainer</code> method must be called, with this Container
     * as an argument.  The method may throw an
     * <code>IllegalArgumentException</code> if this Valve chooses not to
     * be associated with this Container, or <code>IllegalStateException</code>
     * if it is already associated with a different Container.
     *
     * @param valve Valve to be added
     *
     * @exception IllegalArgumentException if this Container refused to
     *  accept the specified Valve
     * @exception IllegalArgumentException if the specifie Valve refuses to be
     *  associated with this Container
     * @exception IllegalStateException if the specified Valve is already
     *  associated with a different Container
     */
    public synchronized void addValve(Valve valve) {

        pipeline.addValve(valve);
        fireContainerEvent(ADD_VALVE_EVENT, valve);
    }
    
    
    /**
     * Return the set of Valves in the pipeline associated with this
     * Container, including the basic Valve (if any).  If there are no
     * such Valves, a zero-length array is returned.
     */
    public Valve[] getValves() {

        return (pipeline.getValves());

    }
    
    
    /**
     * Remove the specified Valve from the pipeline associated with this
     * Container, if it is found; otherwise, do nothing.
     *
     * @param valve Valve to be removed
     */
    public synchronized void removeValve(Valve valve) {

        pipeline.removeValve(valve);
        fireContainerEvent(REMOVE_VALVE_EVENT, valve);
    }
    
    
    /**
     * Return the first valve in the pipeline.
     */
    public Valve getFirst() {

        return (pipeline.getFirst());

    }
    
    
    
    
    
    
	// ------------------------- Protected Methods -------------------------
    
    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     */
    public void fireContainerEvent(String type, Object data) {
    	
    	 if (listeners.size() < 1)
             return;
         ContainerEvent event = new ContainerEvent(this, type, data);
         ContainerListener list[] = new ContainerListener[0];
         synchronized (listeners) {
             list = (ContainerListener[]) listeners.toArray(list);
         }
         for (int i = 0; i < list.length; i++)
             ((ContainerListener) list[i]).containerEvent(event);

    }
    
    
    
    
 // -------------------- JMX and Registration  --------------------
    protected String type;
    protected String domain;
    protected String suffix;
    protected ObjectName oname;
    protected ObjectName controller;
    protected transient MBeanServer mserver;

    public ObjectName getJmxName() {
        return oname;
    }
    
    public String getObjectName() {
        if (oname != null) {
            return oname.toString();
        } else return null;
    }

    public String getDomain() {
        if( domain==null ) {
        	domain = "Catalina";
            /*Container parent=this;
            while( parent != null &&
                    !( parent instanceof StandardEngine) ) {
                parent=parent.getParent();
            }
            if( parent instanceof StandardEngine ) {
                domain=((StandardEngine)parent).getDomain();
            } */
        }
        return domain;
    }

    public void setDomain(String domain) {
        this.domain=domain;
    }
    
    public String getType() {
        return type;
    }

    protected String getJSR77Suffix() {
        return suffix;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        if (name == null ){
            return null;
        }

        domain=name.getDomain();

        type=name.getKeyProperty("type");
        if( type==null ) {
            type=name.getKeyProperty("j2eeType");
        }

        String j2eeApp=name.getKeyProperty("J2EEApplication");
        String j2eeServer=name.getKeyProperty("J2EEServer");
        if( j2eeApp==null ) {
            j2eeApp="none";
        }
        if( j2eeServer==null ) {
            j2eeServer="none";
        }
        suffix=",J2EEApplication=" + j2eeApp + ",J2EEServer=" + j2eeServer;
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public ObjectName[] getChildren() {
        ObjectName result[]=new ObjectName[children.size()];
        Iterator it=children.values().iterator();
        int i=0;
        while( it.hasNext() ) {
            Object next=it.next();
            if( next instanceof ContainerBase ) {
                result[i++]=((ContainerBase)next).getJmxName();
            }
        }
        return result;
    }

    public ObjectName createObjectName(String domain, ObjectName parent)
        throws Exception
    {
        if( log.isDebugEnabled())
            log.debug("Create ObjectName " + domain + " " + parent );
        return null;
    }

    public String getContainerSuffix() {
        Container container=this;
        Container context=null;
        Container host=null;
        Container servlet=null;
        
        StringBuffer suffix=new StringBuffer();
        
        if( container instanceof StandardHost ) {
            host=container;
        } else if( container instanceof StandardContext ) {
            host=container.getParent();
            context=container;
        } else if( container instanceof StandardWrapper ) {
            context=container.getParent();
            host=context.getParent();
            servlet=container;
        }
        if( context!=null ) {
            String path=((StandardContext)context).getPath();
            suffix.append(",path=").append((path.equals("")) ? "/" : path);
        } 
        if( host!=null ) suffix.append(",host=").append( host.getName() );
        if( servlet != null ) {
            String name=container.getName();
            suffix.append(",servlet=");
            suffix.append((name=="") ? "/" : name);
        }
        return suffix.toString();
    }


    /**
     * Start the background thread that will periodically check for
     * session timeouts.
     */
    protected void threadStart() {

        if (thread != null)
            return;
        if (backgroundProcessorDelay <= 0)
            return;

        threadDone = false;
        String threadName = "ContainerBackgroundProcessor[" + toString() + "]";
        thread = new Thread(new ContainerBackgroundProcessor(), threadName);
        thread.setDaemon(true);
        thread.start();

    }


    /**
     * Stop the background thread that is periodically checking for
     * session timeouts.
     */
    protected void threadStop() {

        if (thread == null)
            return;

        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ;
        }

        thread = null;

    }
    
    
    
    
    
 // -------------- ContainerExecuteDelay Inner Class --------------
    /**
     * Private thread class to invoke the backgroundProcess method 
     * of this container and its children after a fixed delay.
     */
    protected class ContainerBackgroundProcessor implements Runnable {
    	
    	public void run() {
    		while (!threadDone) {
    			try {
                    Thread.sleep(backgroundProcessorDelay * 1000L);
    			}catch (InterruptedException e) {;}
    			
    			if (!threadDone) {
    				//XXX 
    				//do this latter
    			}
    		}
    	}
    }
    
    
    
}
