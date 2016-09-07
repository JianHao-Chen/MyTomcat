package My.catalina.core;

import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleListener;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class AprLifecycleListener implements LifecycleListener {


	private static Log log = LogFactory.getLog(AprLifecycleListener.class);

	private static boolean instanceCreated = false;
	
	
	
	// ---------------------------------------------- Constants
	protected static final int TCN_REQUIRED_MAJOR = 1;
    protected static final int TCN_REQUIRED_MINOR = 1;
    protected static final int TCN_REQUIRED_PATCH = 17;
    protected static final int TCN_RECOMMENDED_PV = 19;
    
    
    
    
    // ----------------------- LifecycleListener Methods
    /**
     * Primary entry point for startup and shutdown events.
     *
     * @param event The event that has occurred
     */
	
	@Override
	public void lifecycleEvent(LifecycleEvent event) {
	
		
	
	}
}
