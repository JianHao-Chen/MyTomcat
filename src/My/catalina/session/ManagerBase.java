package My.catalina.session;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import My.catalina.Container;
import My.catalina.Manager;
import My.catalina.Session;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

/**
 * Minimal implementation of the <b>Manager</b> interface that supports
 * no session persistence or distributable capabilities.  This class may
 * be subclassed to create more sophisticated Manager implementations.
 */

public class ManagerBase implements Manager{

	protected Log log = LogFactory.getLog(ManagerBase.class);
	
	// ------------------- Instance Variables -------------------

	/**
     * The Container with which this Manager is associated.
     */
    protected Container container;
    
    
    
    protected boolean initialized = false;

    private int sessionIdCount = -1;
    
    
    
    
    /**
     * The set of currently active Sessions for this Manager, keyed by
     * session identifier.
     */
    protected Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();
    
    // Number of sessions created by this manager
    protected int sessionCounter = 0;
    
    protected volatile int maxActive = 0;
    private final Object maxActiveUpdateLock = new Object();
    
    
    
    /**
     * The default maximum inactive interval for Sessions created by
     * this Manager.
     */
    protected int maxInactiveInterval = 60;
    
    
    
    protected static final int TIMING_STATS_CACHE_SIZE = 100;
    
    protected LinkedList<SessionTiming> sessionCreationTiming =
        new LinkedList<SessionTiming>();

    protected LinkedList<SessionTiming> sessionExpirationTiming =
        new LinkedList<SessionTiming>();
    
    
    
    
    
	// ----------------------- Properties -----------------------
	
	/**
     * Return the Container with which this Manager is associated.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the Container with which this Manager is associated.
     *
     * @param container The newly associated Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
    }
    
    /**
     * Set the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     *
     * @param interval The new default value
     */
    public void setMaxInactiveInterval(int interval) {
    	int oldMaxInactiveInterval = this.maxInactiveInterval;
        this.maxInactiveInterval = interval;
    }
    
    
    
    
    
    
	// ------------------ Public Methods ------------------
    
    public void init() {
    	if( initialized ) 
    		return;
    	initialized=true;
    	
    	
    	
    	// Ensure caches for timing stats are the right size by filling with
        // nulls.
    	while (sessionCreationTiming.size() < TIMING_STATS_CACHE_SIZE) {
            sessionCreationTiming.add(null);
        }
        while (sessionExpirationTiming.size() < TIMING_STATS_CACHE_SIZE) {
            sessionExpirationTiming.add(null);
        }
    	
    }
    
    
    
    
    
    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     * 
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @deprecated
     */
    public Session createSession() {
        return createSession(null);
    }
    
    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id specified will be used as the session id.  
     * If a new session cannot be created for any reason, return 
     * <code>null</code>.
     * 
     * @param sessionId The session id which should be used to create the
     *  new session; if <code>null</code>, a new session id will be
     *  generated
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession(String sessionId) {
    	
    	// Recycle or create a Session instance
        Session session = createEmptySession();
        
        // Initialize the properties of the new session and return it
        session.setNew(true);
        session.setValid(true);
        
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(this.maxInactiveInterval);
        
        if (sessionId == null) {
        	sessionId = generateSessionId();
        }
        session.setId(sessionId);
        sessionCounter++;
        
        SessionTiming timing = new SessionTiming(session.getCreationTime(), 0);
        synchronized (sessionCreationTiming) {
            sessionCreationTiming.add(timing);
            sessionCreationTiming.poll();
        }
        return (session);
    }
    
    /**
     * Get a session from the recycled ones or create a new empty one.
     * The PersistentManager manager does not need to create session data
     * because it reads it from the Store.
     */
    public Session createEmptySession() {
        return (getNewSession());
    }
    
    
    /**
     * Add this Session to the set of active Sessions for this Manager.
     *
     * @param session Session to be added
     */
    public void add(Session session) {
    	
    	sessions.put(session.getIdInternal(), session);
    	int size = sessions.size();
    	if( size > maxActive ) {
    		synchronized(maxActiveUpdateLock) {
    			if( size > maxActive ) {
                    maxActive = size;
                }
    		}
    	}
    }
    
    
    
    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session Session to be removed
     */
    public void remove(Session session) {

        sessions.remove(session.getIdInternal());

    }
    
    
    
	// -----------------Protected Methods -------------------------
    
    /**
     * Get new session class to be used in the doLoad() method.
     */
    protected StandardSession getNewSession() {
        return new StandardSession(this);
    }
    
    /**
     * Generate and return a new session identifier.
     */
    protected synchronized String generateSessionId() {
    	sessionIdCount++;
    	return "SessionID--" +sessionIdCount;
    	
    }
   
    
    
    
    
    
    
    
	// ----------------------- Inner classes -----------------------
    
    protected static final class SessionTiming {
    	private long timestamp;
        private int duration;
        
        public SessionTiming(long timestamp, int duration) {
            this.timestamp = timestamp;
            this.duration = duration;
        }
        
        
        /**
         * Time stamp associated with this piece of timing information in
         * milliseconds.
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Duration associated with this piece of timing information in seconds.
         */
        public int getDuration() {
            return duration;
        }
    }


  
}
