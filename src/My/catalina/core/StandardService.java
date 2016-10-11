package My.catalina.core;

import java.util.ArrayList;

import My.catalina.Container;
import My.catalina.Engine;
import My.catalina.Executor;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Server;
import My.catalina.Service;
import My.catalina.connector.Connector;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.catalina.util.LifecycleSupport;

/**
 * Standard implementation of the <code>Service</code> interface.  The
 * associated Container is generally an instance of Engine, but this is
 * not required.
 */

public class StandardService implements Lifecycle , Service{

	private static Log log = LogFactory.getLog(StandardService.class);
	
	// ------------------------- Instance Variables -------------------------
	
	/**
     * The name of this service.
     */
    private String name = null;
    
    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    
    /**
     * The <code>Server</code> that owns this Service, if any.
     */
    private Server server = null;
    
    /**
     * Has this component been started?
     */
    private boolean started = false;
    
    
    /**
     * The set of Connectors associated with this Service.
     */
    protected Connector connectors[] = new Connector[0];
    

    protected ArrayList<Executor> executors = new ArrayList<Executor>();
    
    
    /**
     * The Container associated with this Service. (In the case of the
     * org.apache.catalina.startup.Embedded subclass, this holds the most
     * recently added Engine.)
     */
    protected Container container = null;
    
    
    /**
     * Has this component been initialized?
     */
    protected boolean initialized = false;
    
    
    
    // ----------------------------- Properties -----------------------------
    
    /**
     * Return the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     */
    public Container getContainer() {
        return (this.container);
    }
    
    
    /**
     * Set the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     *
     * @param container The new Container
     */
    public void setContainer(Container container) {
    	
    	 Container oldContainer = this.container;
    	 
    	 if ((oldContainer != null) && (oldContainer instanceof Engine))
             ((Engine) oldContainer).setService(null);
    	 
    	 this.container = container;
         if ((this.container != null) && (this.container instanceof Engine))
             ((Engine) this.container).setService(this);
    	 
         
         if (started && (this.container != null) &&
                 (this.container instanceof Lifecycle)) {
                 try {
                     ((Lifecycle) this.container).start();
                 } catch (LifecycleException e) {
                     ;
                 }
          }
         
         
         synchronized (connectors) {
             for (int i = 0; i < connectors.length; i++)
                 connectors[i].setContainer(this.container);
         }
         
         
         
         if (started && (oldContainer != null) &&
                 (oldContainer instanceof Lifecycle)) {
                 try {
                     ((Lifecycle) oldContainer).stop();
                 } catch (LifecycleException e) {
                     ;
                 }
        }
         
         
         
    }
    

    /**
     * the name of this Service.
     */
    public String getName() {
        return (this.name);
    }
    public void setName(String name) {
        this.name = name;
    }
    
    
    /**
     * <code>Server</code> with which we are associated (if any).
     */
    public Server getServer() {
        return (this.server);
    }
    
    public void setServer(Server server) {
        this.server = server;
    }
    
    
    
    // ------------------------- Public Methods -------------------------
    
    /**
     * Add a new Connector to the set of defined Connectors, and associate it
     * with this Service's Container.
     */
    public void addConnector(Connector connector) {
    	
    	 synchronized (connectors) {
    		 connector.setContainer(this.container);
    		 connector.setService(this);
    		 Connector results[] = new Connector[connectors.length + 1];
             System.arraycopy(connectors, 0, results, 0, connectors.length);
             results[connectors.length] = connector;
             connectors = results;
             
             // if this service already init, then the new added connector
             // should be init now.
             if (initialized) {
            	 try {
                     connector.initialize();
                 }catch (LifecycleException e) {
                	 log.error("standardService.connector.initFailed");
                 }
             }
             
             
             if (started && (connector instanceof Lifecycle)) {
            	 try {
                     ((Lifecycle) connector).start();
                 } catch (LifecycleException e) {
                	 log.error( "standardService.connector.startFailed");
                 }
             }

    	 }
    }
    
    
    
    
    
    // -------------------------- Lifecycle Methods --------------------------
    
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }
    public LifecycleListener[] findLifecycleListeners() {
    	 return lifecycle.findLifecycleListeners();
    }
    
    
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }
    
    
    
    /**
     * Adds a named executor to the service
    */
    public void addExecutor(Executor ex) {
    	
        synchronized (executors) {
            if (!executors.contains(ex)) {
                executors.add(ex);
                if (started)
                    try {
                        ex.start();
                    } catch (LifecycleException x) {
                        log.error("Executor.start", x);
                    }
            }
        }
    }
    
    
    /**
     * Retrieves all executors
     * @return Executor[]
     */
    public Executor[] findExecutors() {
    	
        synchronized (executors) {
            Executor[] arr = new Executor[executors.size()];
            executors.toArray(arr);
            return arr;
        }
    }

    /**
     * Retrieves executor by name, null if not found
     * @param name String
     * @return Executor
     */
    public Executor getExecutor(String name) {
        synchronized (executors) {
            for (int i = 0; i < executors.size(); i++) {
                if (name.equals(executors.get(i).getName()))
                    return executors.get(i);
            }
        }
        return null;
    }
    
    /**
     * Removes an executor from the service
     * @param ex Executor
     */
    public void removeExecutor(Executor ex) {
        synchronized (executors) {
            if ( executors.remove(ex) && started ) {
                try {
                    ex.stop();
                } catch (LifecycleException e) {
                    log.error("Executor.stop", e);
                }
            }
        }
    }
    
    
    
    
    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     */
    public void initialize()throws LifecycleException {
    	
    	if (initialized) {
            if(log.isInfoEnabled())
                log.info("standardService.initialize.initialized");
            return;
        }
    	
    	initialized = true;
    	
    	
    	// Initialize our defined Connectors
    	synchronized (connectors) {
    		for (int i = 0; i < connectors.length; i++) {
    			try {
                    connectors[i].initialize();
    			}
    			catch (Exception e) {
    				String message = 
                            "standardService.connector.initFailed";
                    log.error(message, e);
    			}
    		}
    	}
    	
    	
    }
    
    
    public void init() {
        try {
            initialize();
        } catch( Throwable t ) {
            log.error("standardService.initialize.failed");
        }
    }
    
    
    
    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called before any of the public
     * methods of this component are utilized.  It should also send a
     * LifecycleEvent of type START_EVENT to any registered listeners.
     */
    public void start() throws LifecycleException {
    	// Validate and update our current component state
        if (started) {
            if (log.isInfoEnabled()) {
                log.info("standardService.start.started");
            }
            return;
        }
        
        
        if( ! initialized )
            init(); 
        
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);
        
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;
        
        // Start our defined Container first
        if (container != null) {
        	synchronized (container) {
                if (container instanceof Lifecycle) {
                    ((Lifecycle) container).start();
                }
            }
        }
        
        synchronized (executors) {
            for ( int i=0; i<executors.size(); i++ ) {
                executors.get(i).start();
            }
        }
        
        
        // Start our defined Connectors second
        synchronized (connectors) {
            for (int i = 0; i < connectors.length; i++) {
                try {
                    ((Lifecycle) connectors[i]).start();
                } catch (Exception e) {
                    log.error("standardService.connector.startFailed");
                }
            }
        }
        
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);

    }
    
    
    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.  It should also send a LifecycleEvent
     * of type STOP_EVENT to any registered listeners.
     */
    public void stop() throws LifecycleException {
    	
    }



	@Override
	public Connector[] findConnectors() {
		return (connectors);
	}


	 /**
     * Remove the specified Connector from the set associated from this
     * Service.  The removed Connector will also be disassociated from our
     * Container.
     */
	@Override
	public void removeConnector(Connector connector) {
		synchronized (connectors) {
            int j = -1;
            for (int i = 0; i < connectors.length; i++) {
                if (connector == connectors[i]) {
                    j = i;
                    break;
                }
            }
            if (j < 0)
                return;
            if (started && (connectors[j] instanceof Lifecycle)) {
                try {
                    ((Lifecycle) connectors[j]).stop();
                } catch (LifecycleException e) {
                    log.error("standardService.connector.stopFailed");
                }
            }
            connectors[j].setContainer(null);
            connector.setService(null);
            int k = 0;
            Connector results[] = new Connector[connectors.length - 1];
            for (int i = 0; i < connectors.length; i++) {
                if (i != j)
                    results[k++] = connectors[i];
            }
            connectors = results;
		
		}
	}
	
	
	
	/**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("StandardService[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }
    
}
