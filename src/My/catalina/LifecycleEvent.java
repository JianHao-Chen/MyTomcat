package My.catalina;

import java.util.EventObject;

/**
 * General event for notifying listeners of significant changes on a component
 * that implements the Lifecycle interface.
 */

public final class LifecycleEvent extends EventObject {

	// ----------------------------------------------------- Instance Variables
	 /**
     * The event data associated with this event.
     */
    private Object data = null;


    /**
     * The Lifecycle on which this event occurred.
     */
    private Lifecycle lifecycle = null;


    /**
     * The event type this instance represents.
     */
    private String type = null;
    
    
    /**
     * Return the event data of this event.
     */
    public Object getData() {

        return (this.data);

    }


    /**
     * Return the Lifecycle on which this event occurred.
     */
    public Lifecycle getLifecycle() {

        return (this.lifecycle);

    }


    /**
     * Return the event type of this event.
     */
    public String getType() {

        return (this.type);

    }
	
	
	// ----------------------------------------------------------- Constructors
	 /**
     * Construct a new LifecycleEvent with the specified parameters.
     *
     * @param lifecycle Component on which this event occurred
     * @param type Event type (required)
     */
    public LifecycleEvent(Lifecycle lifecycle, String type) {

        this(lifecycle, type, null);

    }
    
    
    /**
     * Construct a new LifecycleEvent with the specified parameters.
     *
     * @param lifecycle Component on which this event occurred
     * @param type Event type (required)
     * @param data Event data (if any)
     */
    public LifecycleEvent(Lifecycle lifecycle, String type, Object data) {

        super(lifecycle);
        this.lifecycle = lifecycle;
        this.type = type;
        this.data = data;

    }
    
    
}
