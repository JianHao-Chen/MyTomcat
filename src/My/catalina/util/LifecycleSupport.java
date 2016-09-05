package My.catalina.util;

import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleListener;

/**
 * Support class to assist in firing LifecycleEvent notifications to
 * registered LifecycleListeners.
 */

public final class LifecycleSupport {

	// ----------------------- Instance Variables -----------------------
	/**
     * The source component for lifecycle events that we will fire.
     */
    private Lifecycle lifecycle = null;
    
    
    /**
     * The set of registered LifecycleListeners for event notifications.
     */
    private LifecycleListener listeners[] = new LifecycleListener[0];
    
    
    /**
     * Lock object for changes to listeners
     */
    private final Object listenersLock = new Object(); 
	
    
    /**
     * Tracks the current state of lifecycle object based on the events that
     * are fired.
     */
    private String state = "NEW";
    
    
	
	// ----------------------- Constructors -----------------------
	/**
     * Construct a new LifecycleSupport object associated with the specified
     * Lifecycle component.
     */
	public LifecycleSupport(Lifecycle lifecycle) {
		super();
		this.lifecycle = lifecycle;
	}
	
	
	
	// ----------------------- Public Methods -----------------------
	 /**
     * Add a lifecycle event listener to this component.
     */
	 public void addLifecycleListener(LifecycleListener listener) {
		 
		 synchronized(listenersLock){
			 LifecycleListener results[] =
		            new LifecycleListener[listeners.length + 1];
			 for (int i = 0; i < listeners.length; i++)
	              results[i] = listeners[i];
			 results[listeners.length] = listener;
	          listeners = results;
		 }
	 }
	 
	 /**
	 * Get the lifecycle listeners associated with this lifecycle. If this 
	 * Lifecycle has no listeners registered, a zero-length array is returned.
	 */
	public LifecycleListener[] findLifecycleListeners() {
		return listeners;
	}
	
	
	/**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     */
	public void fireLifecycleEvent(String type, Object data) {

	        if (Lifecycle.INIT_EVENT.equals(type)) {
	            state = "INITIALIZED";
	        } else if (Lifecycle.BEFORE_START_EVENT.equals(type)) {
	            state = "STARTING_PREP";
	        } else if (Lifecycle.START_EVENT.equals(type)) {
	            state = "STARTING";
	        } else if (Lifecycle.AFTER_START_EVENT.equals(type)) {
	            state = "STARTED";
	        } else if (Lifecycle.BEFORE_STOP_EVENT.equals(type)) {
	            state = "STOPPING_PREP";
	        } else if (Lifecycle.STOP_EVENT.equals(type)) {
	            state = "STOPPING";
	        } else if (Lifecycle.AFTER_STOP_EVENT.equals(type)) {
	            state = "STOPPED";
	        } else if (Lifecycle.DESTROY_EVENT.equals(type)) {
	            state = "DESTROYED";
	        }
	        LifecycleEvent event = new LifecycleEvent(lifecycle, type, data);
	        LifecycleListener interested[] = listeners;
	        for (int i = 0; i < interested.length; i++)
	            interested[i].lifecycleEvent(event);

	}
	
	
	
	/**
     * Remove a lifecycle event listener from this component.
     */
	public void removeLifecycleListener(LifecycleListener listener) {

	        synchronized (listenersLock) {
	            int n = -1;
	            for (int i = 0; i < listeners.length; i++) {
	                if (listeners[i] == listener) {
	                    n = i;
	                    break;
	                }
	            }
	            if (n < 0)
	                return;
	            LifecycleListener results[] =
	              new LifecycleListener[listeners.length - 1];
	            int j = 0;
	            for (int i = 0; i < listeners.length; i++) {
	                if (i != n)
	                    results[j++] = listeners[i];
	            }
	            listeners = results;
	        }
	}
	
	
	public String getState() {
		return state;
	}

}
