package My.catalina.startup;

import My.catalina.Engine;
import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleListener;

/**
 * Startup event listener for a <b>Engine</b> that configures the properties
 * of that Engine, and the associated defined contexts.
 */

public class EngineConfig implements LifecycleListener {

	protected static My.juli.logging.Log log=
        My.juli.logging.LogFactory.getLog( EngineConfig.class );
	
    // ------------------- Instance Variables -------------------
	
	 /**
     * The Engine we are associated with.
     */
    protected Engine engine = null;
    
    
 // ------------------------ Public Methods ------------------------ 
    
    /**
     * Process the START event for an associated Engine.
     */
    public void lifecycleEvent(LifecycleEvent event) {
    	 // Identify the engine we are associated with
        try {
            engine = (Engine) event.getLifecycle();
        }catch (ClassCastException e) {
            log.error("engineConfig get Lifecycle ClassCastException");
            return;
        }
        
        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT))
            start();
        else if (event.getType().equals(Lifecycle.STOP_EVENT))
            stop();
        
    }
    
    
    // ------------------ Protected Methods ------------------ 
    
    /**
     * Process a "start" event for this Engine.
     */
    protected void start() {

        System.out.println("engineConfig.start");

    }


    /**
     * Process a "stop" event for this Engine.
     */
    protected void stop() {

    	System.out.println("engineConfig.stop");

    }
    
    
	
}
