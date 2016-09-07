package My.catalina.core;

import My.catalina.Contained;
import My.catalina.Container;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Pipeline;
import My.catalina.Valve;
import My.catalina.util.LifecycleSupport;
import My.catalina.valves.ValveBase;

public class StandardPipeline 
	implements Pipeline, Contained, Lifecycle {
	
	
	// ----------------------------------------------------------- Constructors


    /**
     * Construct a new StandardPipeline instance with no associated Container.
     */
    public StandardPipeline() {

        this(null);

    }


    /**
     * Construct a new StandardPipeline instance that is associated with the
     * specified Container.
     *
     * @param container The container we should be associated with
     */
    public StandardPipeline(Container container) {

        super();
        setContainer(container);

    }
    

	// ------------------ Instance Variables ------------------
    
    /**
     * The basic Valve (if any) associated with this Pipeline.
     */
    protected Valve basic = null;
    
    
    /**
     * The Container with which this Pipeline is associated.
     */
    protected Container container = null;

    
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    
    /**
     * Has this component been started yet?
     */
    protected boolean started = false;


    /**
     * The first valve associated with this Pipeline.
     */
    protected Valve first = null;
    
    
    
    // ------------- Public Methods -------------
    
    
    
	// --------------------- Contained Methods ---------------------
    
    /**
     * Return the Container with which this Pipeline is associated.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the Container with which this Pipeline is associated.
     *
     * @param container The new associated container
     */
    public void setContainer(Container container) {

        this.container = container;

    }
    
    
    
	// ---------------- Lifecycle Methods ---------------- 
    
    
    
    /**
     * Add a lifecycle event listener to this component.
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
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                ("standardPipeline.alreadyStarted");

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        started = true;

        // Start the Valves in our pipeline (including the basic), if any
        Valve current = first;
        if (current == null) {
        	current = basic;
        }
        while (current != null) {
            if (current instanceof Lifecycle)
                ((Lifecycle) current).start();
            registerValve(current);
        	current = current.getNext();
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(START_EVENT, null);

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                ("standardPipeline.notStarted");

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop the Valves in our pipeline (including the basic), if any
        Valve current = first;
        if (current == null) {
        	current = basic;
        }
        while (current != null) {
            if (current instanceof Lifecycle)
                ((Lifecycle) current).stop();
            unregisterValve(current);
        	current = current.getNext();
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);
    }
    
    
    
    private void registerValve(Valve valve) {

      
    }
    
    private void unregisterValve(Valve valve) {
        
        
    }    
    
    
    
	// ------------------- Pipeline Methods -------------------

	

	@Override
	public Valve getBasic() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBasic(Valve valve) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addValve(Valve valve) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Valve[] getValves() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeValve(Valve valve) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Valve getFirst() {
		// TODO Auto-generated method stub
		return null;
	}

}
