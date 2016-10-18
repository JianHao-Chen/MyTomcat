package My.catalina.session;

import java.io.IOException;

import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Manager;
import My.catalina.Store;
import My.catalina.util.LifecycleSupport;

public abstract class StoreBase implements Lifecycle, Store {

	// ------------------ Instance Variables ----------------------
	
	/**
     * Has this component been started yet?
     */
    protected boolean started = false;

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    
    /**
     * The Manager with which this Store instance is associated.
     */
    protected Manager manager;
	
    
	// --------------------- Properties --------------------- 
    
    /**
     * Set the Manager with which this Store is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager) {
        Manager oldManager = this.manager;
        this.manager = manager;
    }

    /**
     * Return the Manager with which the Store is associated.
     */
    public Manager getManager() {
        return(this.manager);
    }
    
    
	// ---------------------- Public Methods -------------------------
    
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
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }
    
    
    
	// -------------------- Protected Methods --------------------
    
    /**
     * Called by our background reaper thread to check if Sessions
     * saved in our store are subject of being expired. If so expire
     * the Session and remove it from the Store.
     *
     */
    public void processExpires() {
    	
    	long timeNow = System.currentTimeMillis();
        String[] keys = null;
        
        if(!started) {
            return;
        }
        
        try {
            keys = keys();
        } catch (IOException e) {
        	
        }
        
        for (int i = 0; i < keys.length; i++) {
        	try {
        		StandardSession session = (StandardSession) load(keys[i]);
                if (session == null) {
                    continue;
                }
                
                int timeIdle = (int) ((timeNow - session.thisAccessedTime) / 1000L);
                
                if (timeIdle < session.getMaxInactiveInterval()) {
                    continue;
                }
                
                if ( ( (PersistentManagerBase) manager).isLoaded( keys[i] )) {
                	// recycle old backup session
                    session.recycle();
                }
                else {
                    // expire swapped out session
                    session.expire();
                }
                remove(keys[i]);
        	}
        	catch (Exception e) {
        		
        	}
        }
        
    }

    
    
	// --------------------- Thread Methods ----------------------
    
    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {
        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                ("StoreBase already start!!");
        
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {
        // Validate and update our current component state
        if (!started)
            throw new LifecycleException("StoreBase not start!!");
        
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

    }
    
}
