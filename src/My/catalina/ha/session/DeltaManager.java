package My.catalina.ha.session;

import org.apache.catalina.ha.session.SessionMessage;
import org.apache.catalina.ha.session.SessionMessageImpl;

import My.catalina.Cluster;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Session;
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
	
    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    private int maxActiveSessions = -1;
    
    
    
    protected String name = null;
    
    protected boolean defaultMode = false;
    
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
    
    public void setDefaultMode(boolean defaultMode) {
        this.defaultMode = defaultMode;
    }
    
    public boolean isDefaultMode() {
        return defaultMode;
    }
    
    
    
	// ---------------------- Public Methods ----------------------
    
    /**
     * Construct and return a new session object, based on the default settings
     * specified by this Manager's properties. The session id will be assigned
     * by this method, and available via the getId() method of the returned
     * session. If a new session cannot be created for any reason, return
     * <code>null</code>.
     */
    public Session createSession(String sessionId) {
    	return createSession(sessionId, true);
    }
    
    /**
     * create new session with check maxActiveSessions and send session creation
     * to other cluster nodes.
     */
    public Session createSession(String sessionId, boolean distribute) {
    	if ((maxActiveSessions >= 0) && (sessions.size() >= maxActiveSessions)) {
    		throw new IllegalStateException("deltaManager.createSession.ise");
    	}
    	
    	DeltaSession session = (DeltaSession) super.createSession(sessionId) ;
    	
    	if (distribute) {
    		sendCreateSession(session.getId(), session);
    	}
    	
    	return (session);
    }
    
    public Session createEmptySession() {
        return getNewDeltaSession() ;
    }
    
    protected DeltaSession getNewDeltaSession() {
        return new DeltaSession(this);
    }
    
    
    
    /**
     * Send create session evt to all backup node
     */
    protected void sendCreateSession(String sessionId, DeltaSession session) {
    	if(cluster.getMembers().length > 0 ) {
    		SessionMessage msg = 
                new SessionMessageImpl(getName(),
                                       SessionMessage.EVT_SESSION_CREATED, 
                                       null, 
                                       sessionId,
                                       sessionId + "-" + System.currentTimeMillis());
    		
    		
    	}
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
		if (!initialized) 
			init();
		
		// Validate and update our current component state
		if (started) {
            return;
        }
		started = true;
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        
        
        // Load unloaded sessions, if any
        try {
        	//the channel is already running
        	Cluster cluster = getCluster() ;
        	
        	cluster.registerManager(this);
        	
        	getAllClusterSessions();
        }
        catch (Throwable t) {
        	log.error("deltaManager.managerLoad");
        }
        
	}
	
	/**
     * get from first session master the backup from all clustered sessions
     * @see #findSessionMasterMember()
     */
    public synchronized void getAllClusterSessions() {
    	if (cluster != null && cluster.getMembers().length > 0) {
    		
    	}
    	else
    		if (log.isInfoEnabled()) 
    			log.info("deltaManager.noMembers");
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
