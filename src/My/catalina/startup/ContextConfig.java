package My.catalina.startup;

import My.catalina.LifecycleEvent;
import My.catalina.LifecycleListener;

public class ContextConfig implements LifecycleListener {

	protected static My.juli.logging.Log log=
        My.juli.logging.LogFactory.getLog( ContextConfig.class );
	
	
	// --------------------------------------------------------- Public Methods


    /**
     * Process events for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {
    	
    }
}
