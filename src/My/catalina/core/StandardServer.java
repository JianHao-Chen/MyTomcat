package My.catalina.core;


import java.net.ServerSocket;

import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Server;
import My.catalina.Service;
import My.catalina.deploy.NamingResources;
import My.catalina.util.LifecycleSupport;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public final class StandardServer 
	implements Lifecycle, Server{

	
	private static Log log = LogFactory.getLog(StandardServer.class);
	
	
	// ------------------- Constructor -------------------

	
	 /**
     * Construct a default instance of this class.
     */
    public StandardServer() {
        
        globalNamingResources = new NamingResources();
        globalNamingResources.setContainer(this);
        
        
        //UseNaming
      //  if (namingContextListener == null) {
        	
       // }
    }
	
	// ------------------------ Instance Variables ------------------------
	
	 /**
     * Global naming resources.
     */
    private NamingResources globalNamingResources = null;
    
	
	 /**
     * Global naming resources context.
     */
    private javax.naming.Context globalNamingContext = null;
    
    
    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    
    /**
     * The port number on which we wait for shutdown commands.
     */
    private int port = 8005;
    
    
    /**
     * The set of Services associated with this Server.
     */
    private Service services[] = new Service[0];
    
    
    /**
     * The shutdown command string we are looking for.
     */
    private String shutdown = "SHUTDOWN";

    
    /**
     * Has this component been started?
     */
    private boolean started = false;


    /**
     * Has this component been initialized?
     */
    private boolean initialized = false;
    
    
    private volatile boolean stopAwait = false;

    /**
     * Thread that currently is inside our await() method.
     */
    private volatile Thread awaitThread = null;

    /**
     * Server socket that is used to wait for the shutdown command.
     */
    private volatile ServerSocket awaitSocket = null;
    
    
	// ----------------------- Properties -----------------------
    
    
    
    
    // -------------------------- Server Methods -------------------
    
    /**
     * Add a new Service to the set of defined Services.
     */
    public void addService(Service service) {
    	
    	 service.setServer(this);

         synchronized (services) {
        	 Service results[] = new Service[services.length + 1];
             System.arraycopy(services, 0, results, 0, services.length);
             results[services.length] = service;
             services = results;

             if (initialized) {
                 try {
                     service.initialize();
                 } catch (LifecycleException e) {
                     log.error(e);
                 }
             }

             if (started && (service instanceof Lifecycle)) {
                 try {
                     ((Lifecycle) service).start();
                 } catch (LifecycleException e) {
                     ;
                 }
             }
         }
    }
    
    
    
    /**
     * Return the specified Service (if it exists); otherwise return
     * <code>null</code>.
     *
     * @param name Name of the Service to be returned
     */
    public Service findService(String name) {

        if (name == null) {
            return (null);
        }
        synchronized (services) {
            for (int i = 0; i < services.length; i++) {
                if (name.equals(services[i].getName())) {
                    return (services[i]);
                }
            }
        }
        return (null);

    }
    
    /**
     * Return the set of Services defined within this Server.
     */
    public Service[] findServices() {

        return (services);

    }
    
    
    
    
	// --------------------- Public Methods ---------------------
    
	
    
    
	// -------------------- Lifecycle Methods --------------------
    /**
     * Add a LifecycleEvent listener to this component.
     *
     * @param listener The listener to add
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
     * Remove a LifecycleEvent listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called before any of the public
     * methods of this component are utilized.  It should also send a
     * LifecycleEvent of type START_EVENT to any registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // Validate and update our current component state
        if (started) {
            log.debug("standardServer.start.started");
            return;
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Start our defined Services
        synchronized (services) {
            for (int i = 0; i < services.length; i++) {
                if (services[i] instanceof Lifecycle)
                    ((Lifecycle) services[i]).start();
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
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            return;

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop our defined Services
        for (int i = 0; i < services.length; i++) {
            if (services[i] instanceof Lifecycle)
                ((Lifecycle) services[i]).stop();
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);

       // stopAwait();

    }

    public void init() throws Exception {
        initialize();
    }
    
    
    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     */
    public void initialize()
        throws LifecycleException 
    {
    	
    }



	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public int getPort() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public void setPort(int port) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public String getShutdown() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setShutdown(String shutdown) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void await() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void removeService(Service service) {
		 synchronized (services) {
	            int j = -1;
	            for (int i = 0; i < services.length; i++) {
	                if (service == services[i]) {
	                    j = i;
	                    break;
	                }
	            }
	            if (j < 0)
	                return;
	            if (services[j] instanceof Lifecycle) {
	                try {
	                    ((Lifecycle) services[j]).stop();
	                } catch (LifecycleException e) {
	                    ;
	                }
	            }
	            int k = 0;
	            Service results[] = new Service[services.length - 1];
	            for (int i = 0; i < services.length; i++) {
	                if (i != j)
	                    results[k++] = services[i];
	            }
	            services = results;

	        }
		
	}
    
    
}
