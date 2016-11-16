package My.catalina.ha.session;

import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.ha.CatalinaCluster;
import My.catalina.ha.ClusterManager;
import My.catalina.util.LifecycleSupport;

/**
 * The DeltaManager manages replicated sessions by only replicating the deltas
 * in data. For applications written to handle this, the DeltaManager is the
 * optimal way of replicating data.
 * 
 * This code is almost identical to StandardManager with a difference in how it
 * persists sessions and some modifications to it.
 * 
 * <b>IMPLEMENTATION NOTE </b>: Correct behavior of session storing and
 * reloading depends upon external calls to the <code>start()</code> and
 * <code>stop()</code> methods of this class at the correct times.
 */

public class DeltaManager extends ClusterManagerBase{
	
	public static My.juli.logging.Log log = 
		My.juli.logging.LogFactory.getLog(DeltaManager.class);
	
	// ---------------------- Instance Variables ----------------------
	
	/**
     * Has this component been started yet?
     */
    private boolean started = false;
    
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
	
    
    protected String name = null;
    
    private CatalinaCluster cluster = null;
    
    
	// ----------------------- Constructor -----------------------------
    public DeltaManager() {
        super();
    }
    
    
    
	// -------------------------- Properties -------------------------
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {
        return name;
    }
    
    
    public CatalinaCluster getCluster() {
        return cluster;
    }

    public void setCluster(CatalinaCluster cluster) {
        this.cluster = cluster;
    }
    
    
	
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
     * component. This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     * 
     * @exception LifecycleException
     *                if this component detects a fatal error that prevents this
     *                component from being used
     */
	public void start() throws LifecycleException {
		if (!initialized) init();
		
	}

	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	public ClusterManager cloneFromTemplate() {
		
		DeltaManager result = new DeltaManager();
		result.name = "Clone-from-"+name;
        return result;
	}

}
