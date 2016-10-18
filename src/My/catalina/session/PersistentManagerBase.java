package My.catalina.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Session;
import My.catalina.Store;
import My.catalina.util.LifecycleSupport;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public abstract class PersistentManagerBase 
	extends ManagerBase
	implements Lifecycle{

	private static Log log = LogFactory.getLog(PersistentManagerBase.class);
	
	
	
	// ------------------ Instance Variables ------------------
	
	/**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    
    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    protected int maxActiveSessions = -1;
    
    
    /**
     * Has this component been started yet?
     */
    protected boolean started = false;
    
    
    /**
     * Whether to save and reload sessions when the Manager <code>unload</code>
     * and <code>load</code> methods are called.
     */
    protected boolean saveOnRestart = true;
    
    /**
     * How long a session must be idle before it should be backed up.
     * -1 means sessions won't be backed up.
     */
    protected int maxIdleBackup = -1;
    
    
    /**
     * Minimum time a session must be idle before it is swapped to disk.
     * This overrides maxActiveSessions, to prevent thrashing if there are lots
     * of active sessions. Setting to -1 means it's ignored.
     */
    protected int minIdleSwap = -1;
    
    /**
     * The maximum time a session may be idle before it should be swapped
     * to file just on general principle. Setting this to -1 means sessions
     * should not be forced out.
     */
    protected int maxIdleSwap = -1;
    
    
    /**
     * Number of session creations that failed due to maxActiveSessions.
     */
    protected int rejectedSessions = 0;


    /**
     * Processing time during session expiration and passivation.
     */
    protected long processingTime = 0;
    
    
    /**
     * Sessions currently being swapped in and the associated locks
     */
    private final Map<String,Object> sessionSwapInLocks =
    	new HashMap<String,Object>();
	
    
    /**
     * Store object which will manage the Session store.
     */
    protected Store store = null;
    
    
	// ------------------------ Properties ---------------------------
    
    /**
	 * Indicates how many seconds old a session can get, after its last use in a
	 * request, before it should be backed up to the store. -1 means sessions
	 * are not backed up.
	 */
    public int getMaxIdleBackup() {

        return maxIdleBackup;

    }


    /**
     * Sets the option to back sessions up to the Store after they
     * are used in a request. Sessions remain available in memory
     * after being backed up, so they are not passivated as they are
     * when swapped out. The value set indicates how old a session
     * may get (since its last use) before it must be backed up: -1
     * means sessions are not backed up.
     * <p>
     * Note that this is not a hard limit: sessions are checked
     * against this age limit periodically according to <b>processExpiresFrequency</b>.
     * This value should be considered to indicate when a session is
     * ripe for backing up.
     * <p>
     * So it is possible that a session may be idle for maxIdleBackup +
     * processExpiresFrequency * engine.backgroundProcessorDelay seconds, plus the time it takes to handle other
     * session expiration, swapping, etc. tasks.
     *
     * @param backup The number of seconds after their last accessed
     * time when they should be written to the Store.
     */
    public void setMaxIdleBackup (int backup) {

        if (backup == this.maxIdleBackup)
            return;
        int oldBackup = this.maxIdleBackup;
        this.maxIdleBackup = backup;    
    }
    
    /**
     * The time in seconds after which a session should be swapped out of
     * memory to disk.
     */
    public int getMaxIdleSwap() {

        return maxIdleSwap;

    }


    /**
     * Sets the time in seconds after which a session should be swapped out of
     * memory to disk.
     */
    public void setMaxIdleSwap(int max) {

        if (max == this.maxIdleSwap)
            return;
        int oldMaxIdleSwap = this.maxIdleSwap;
        this.maxIdleSwap = max;
      
    }
    
    /**
     * The minimum time in seconds that a session must be idle before
     * it can be swapped out of memory, or -1 if it can be swapped out
     * at any time.
     */
    public int getMinIdleSwap() {

        return minIdleSwap;

    }


    /**
     * Sets the minimum time in seconds that a session must be idle before
     * it can be swapped out of memory due to maxActiveSession. Set it to -1
     * if it can be swapped out at any time.
     */
    public void setMinIdleSwap(int min) {

        if (this.minIdleSwap == min)
            return;
        int oldMinIdleSwap = this.minIdleSwap;
        this.minIdleSwap = min;
        
    }
    
    
    /**
     * Return the maximum number of active Sessions allowed, or -1 for
     * no limit.
     */
    public int getMaxActiveSessions() {

        return (this.maxActiveSessions);

    }


    /**
     * Set the maximum number of actives Sessions allowed, or -1 for
     * no limit.
     *
     * @param max The new maximum number of sessions
     */
    public void setMaxActiveSessions(int max) {

        int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = max;
   }
    
    
    /** 
     * Number of session creations that failed due to maxActiveSessions.
     *
     * @return The count
     */
    public int getRejectedSessions() {
        return rejectedSessions;
    }

    
    public void setRejectedSessions(int rejectedSessions) {
        this.rejectedSessions = rejectedSessions;
    }
    
    
    /**
     * Get the started status.
     */
    protected boolean isStarted() {

        return started;

    }


    /**
     * Set the started flag
     */
    protected void setStarted(boolean started) {

        this.started = started;

    }
    
    
    /**
     * Indicates whether sessions are saved when the Manager is shut down
     * properly. This requires the unload() method to be called.
     */
    public boolean getSaveOnRestart() {

        return saveOnRestart;

    }


    /**
     * Set the option to save sessions to the Store when the Manager is
     * shut down, then loaded when the Manager starts again. If set to
     * false, any sessions found in the Store may still be picked up when
     * the Manager is started again.
     *
     * @param saveOnRestart true if sessions should be saved on restart, false if
     *     they should be ignored.
     */
    public void setSaveOnRestart(boolean saveOnRestart) {

        if (saveOnRestart == this.saveOnRestart)
            return;

        boolean oldSaveOnRestart = this.saveOnRestart;
        this.saveOnRestart = saveOnRestart;
    }
    
    
    /**
	 * Set the Container with which this Manager has been associated. If it is a
	 * Context (the usual case), listen for changes to the session timeout
	 * property.
	 * 
	 * @param container
	 *            The associated Container
	 */
    public void setContainer(Container container) {
    
    	// Default processing provided by our superclass
    	super.setContainer(container);
    	
    	// Register with the new Container (if any)
    	if ((this.container != null) && (this.container instanceof Context)) {
    		setMaxInactiveInterval
            ( ((Context) this.container).getSessionTimeout()*60 );

    	}
    }
    
    
    /**
     * Set the Store object which will manage persistent Session
     * storage for this Manager.
     *
     * @param store the associated Store
     */
    public void setStore(Store store) {
        this.store = store;
        store.setManager(this);

    }


    /**
     * Return the Store object which manages persistent Session
     * storage for this Manager.
     */
    public Store getStore() {

        return (this.store);

    }
    
    
    
    
	// ------------------------ Public Methods --------------------------
    
    
    /**
     * Implements the Manager interface, direct call to processExpires and processPersistenceChecks
     */
	public void processExpires() {
		
		long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();
        int expireHere = 0 ;
        
        for (int i = 0; i < sessions.length; i++) {
            if (!sessions[i].isValid()) {
                expiredSessions++;
                expireHere++;
            }
        }
        
        processPersistenceChecks();
		
        if ((getStore() != null) && (getStore() instanceof StoreBase)) {
            ((StoreBase) getStore()).processExpires();
        }
        
        long timeEnd = System.currentTimeMillis();
        
        processingTime += (timeEnd - timeNow);
	}
    
    
    
	/**
     * Called by the background thread after active sessions have been checked
     * for expiration, to allow sessions to be swapped out, backed up, etc.
     */
    public void processPersistenceChecks() {
    	
    	processMaxIdleSwaps();
    	processMaxActiveSwaps();
        processMaxIdleBackups();
    }

    
    /**
     * Return true, if the session id is loaded in memory
     * otherwise false is returned
     *
     * @param id The session id for the session to be searched for
     */
    public boolean isLoaded( String id ){
    	try {
            if ( super.findSession(id) != null )
                return true;
        } catch (IOException e) {
            log.error("checking isLoaded for id, " + id + ", "+e.getMessage(), e);
        }
        return false;
    }
    
    
    
    
	// ------------------------Protected Methods ---------------------------
    
    /**
     * Swap idle sessions out to Store if they are idle too long.
     */
    protected void processMaxIdleSwaps() {
    	if (!isStarted() || maxIdleSwap < 0)
            return;

        Session sessions[] = findSessions();
        long timeNow = System.currentTimeMillis();
        
        // Swap out all sessions idle longer than maxIdleSwap
        if (maxIdleSwap >= 0) {
        	
        	for (int i = 0; i < sessions.length; i++) {
        		StandardSession session = (StandardSession) sessions[i];
                synchronized (session) {
                	if (!session.isValid())
                        continue;
                	
                	int timeIdle = // Truncate, do not round up
                        (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
                	
                	if (timeIdle > maxIdleSwap && timeIdle > minIdleSwap) {
                		if (session.accessCount != null &&
                                session.accessCount.get() > 0) {
                                // Session is currently being accessed - skip it
                                continue; 
                		} 
                		
                		try {
                			swapOut(session);
                		}
                		catch (IOException e) {
                            ;   // This is logged in writeSession()
                        }
                	}
                }
        	}
        }
        
    }
    
    
    
    /**
     * Remove the session from the Manager's list of active
     * sessions and write it out to the Store. If the session
     * is past its expiration or invalid, this method does
     * nothing.
     *
     * @param session The Session to write out.
     */
    protected void swapOut(Session session) throws IOException {
    	
    	if (store == null || !session.isValid()) {
            return;
        }
    	
    	((StandardSession)session).passivate();
    	
    	writeSession(session);
    	super.remove(session);
    	session.recycle();
    	
    }
    
    
    /**
     * Write the provided session to the Store without modifying
     * the copy in memory or triggering passivation events. Does
     * nothing if the session is invalid or past its expiration.
     */
    protected void writeSession(Session session) throws IOException {
    	if (store == null || !session.isValid()) {
            return;
        }
    	
    	try {
    		store.save(session);
    	}
    	catch (IOException e) {
    		log.error("writeSession error occur");
    	}
    }
    
    
    /**
     * Swap idle sessions out to Store if too many are active
     */
    protected void processMaxActiveSwaps() {
    	
    	if (!isStarted() || getMaxActiveSessions() < 0)
            return;
    	
    	
    }

    	
    /**
     * Back up idle sessions.
     */
    protected void processMaxIdleBackups() {
    	
    }
    
    
	// -------------------- Lifecycle Methods -------------------------

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		lifecycle.addLifecycleListener(listener);
	}


	@Override
	public LifecycleListener[] findLifecycleListeners() {
		return lifecycle.findLifecycleListeners();
	}


	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		lifecycle.removeLifecycleListener(listener);
	}


	@Override
	public void start() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}
    
    
    
    
    
}
