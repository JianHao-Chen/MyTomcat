package My.catalina.session;

import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;

/**
 * Standard implementation of the <b>Manager</b> interface that provides
 * simple session persistence across restarts of this component (such as
 * when the entire server is shut down and restarted, or when a particular
 * web application is reloaded.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  Correct behavior of session storing and
 * reloading depends upon external calls to the <code>start()</code> and
 * <code>stop()</code> methods of this class at the correct times.
 */

public class StandardManager 
	extends ManagerBase
	implements Lifecycle{

	
	
	
	
	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LifecycleListener[] findLifecycleListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	
	

	 /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
	public void start() throws LifecycleException {
		if( ! initialized )
            init();
		
	}

	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}

}
