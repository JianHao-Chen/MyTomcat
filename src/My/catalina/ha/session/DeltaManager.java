package My.catalina.ha.session;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import My.catalina.core.StandardContext;
import My.catalina.ha.session.SessionMessage;
import My.catalina.ha.session.SessionMessageImpl;
import My.catalina.ha.tcp.ReplicationValve;
import My.catalina.Cluster;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Session;
import My.catalina.ha.CatalinaCluster;
import My.catalina.ha.ClusterManager;
import My.catalina.ha.ClusterMessage;
import My.catalina.tribes.Member;
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
     * cached replication valve cluster container!
     */
    private ReplicationValve replicationValve = null ;
    
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
    
    
    private boolean notifySessionListenersOnReplication = true;
    private volatile boolean stateTransfered = false ;
    private int stateTransferTimeout = 60;
    private boolean sendClusterDomainOnly = true ;
    private boolean sendAllSessions = true;
    
    
    private ArrayList receivedMessageQueue = new ArrayList();
    private boolean receiverQueue = false ;
    private long stateTransferCreateSendTime; 
    
    
    
	// --------------------------- stats attributes ------------------------
    int rejectedSessions = 0;
    private long sessionReplaceCounter = 0 ;
    long processingTime = 0;
    private long counterReceive_EVT_GET_ALL_SESSIONS = 0 ;
    private long counterReceive_EVT_ALL_SESSION_DATA = 0 ;
    private long counterReceive_EVT_SESSION_CREATED = 0 ;
    private long counterReceive_EVT_SESSION_EXPIRED = 0;
    private long counterReceive_EVT_SESSION_ACCESSED = 0 ;
    private long counterReceive_EVT_SESSION_DELTA = 0;
    private int counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0 ;
    private long counterReceive_EVT_CHANGE_SESSION_ID = 0 ;
    private long counterSend_EVT_GET_ALL_SESSIONS = 0 ;
    private long counterSend_EVT_ALL_SESSION_DATA = 0 ;
    private long counterSend_EVT_SESSION_CREATED = 0;
    private long counterSend_EVT_SESSION_DELTA = 0 ;
    private long counterSend_EVT_SESSION_ACCESSED = 0;
    private long counterSend_EVT_SESSION_EXPIRED = 0;
    private int counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0 ;
    private long counterSend_EVT_CHANGE_SESSION_ID = 0;
    private int counterNoStateTransfered = 0 ;
    
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
    
    
    /**
     * @return Returns the sendClusterDomainOnly.
     */
    public boolean doDomainReplication() {
        return sendClusterDomainOnly;
    }
    
    /**
     * @param sendClusterDomainOnly The sendClusterDomainOnly to set.
     */
    public void setDomainReplication(boolean sendClusterDomainOnly) {
        this.sendClusterDomainOnly = sendClusterDomainOnly;
    }
    
    
    /**
     * @return Returns the stateTransferTimeout.
     */
    public int getStateTransferTimeout() {
        return stateTransferTimeout;
    }
    /**
     * @param timeoutAllSession The timeout
     */
    public void setStateTransferTimeout(int timeoutAllSession) {
        this.stateTransferTimeout = timeoutAllSession;
    }
    
    /**
     * is session state transfered complete?
     * 
     */
    public boolean getStateTransfered() {
        return stateTransfered;
    }

    /**
     * set that state ist complete transfered  
     * @param stateTransfered
     */
    public void setStateTransfered(boolean stateTransfered) {
        this.stateTransfered = stateTransfered;
    }
    
    
    /**
     * 
     * @return Returns the sendAllSessions.
     */
    public boolean isSendAllSessions() {
        return sendAllSessions;
    }
    
    /**
     * @param sendAllSessions The sendAllSessions to set.
     */
    public void setSendAllSessions(boolean sendAllSessions) {
        this.sendAllSessions = sendAllSessions;
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
    		
    		msg.setTimestamp(session.getCreationTime());
    		counterSend_EVT_SESSION_CREATED++;
    		send(msg);
    	}
    }
    
    /**
     * Send messages to other backup member (domain or all)
     * @param msg Session message
     */
    protected void send(SessionMessage msg) {
    	if(cluster != null) {
    		if(doDomainReplication())
    			cluster.sendClusterDomain(msg);
    		else
    			cluster.send(msg);
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
    		long beforeSendTime = System.currentTimeMillis();
    		Member mbr = findSessionMasterMember();
    		if(mbr == null) { // No domain member found
                return;
           }
    		
    		
    		SessionMessage msg = 
    			new SessionMessageImpl(this.getName(),SessionMessage.EVT_GET_ALL_SESSIONS, null, "GET-ALL","GET-ALL-" + getName());
    		
    		// set reference time
            stateTransferCreateSendTime = beforeSendTime ;
            // request session state
            counterSend_EVT_GET_ALL_SESSIONS++;
            stateTransfered = false ;
            
            
            try {
            	synchronized(receivedMessageQueue) {
            		receiverQueue = true ;
            	}
            	cluster.send(msg, mbr);
            	
            	if (log.isWarnEnabled()) 
            		log.warn("deltaManager.waitForSessionState");
            	
            	waitForSendAllSessions(beforeSendTime);
            }
            finally {
            	synchronized(receivedMessageQueue) {
            		for (Iterator iter = receivedMessageQueue.iterator(); iter.hasNext();) {
            			//...
            		}
            		receivedMessageQueue.clear();
                    receiverQueue = false ;
            	}
            	
            }
    	}
    	else
    		if (log.isInfoEnabled()) 
    			log.info("deltaManager.noMembers");
    }

	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	/**
     * Register cross context session at replication valve thread local
     * @param session cross context session
     */
    protected void registerSessionAtReplicationValve(DeltaSession session) {
    	if(replicationValve == null) {
    		 if(container instanceof StandardContext && ((StandardContext)container).getCrossContext()) {
    			 //... default is not go into here.
    		 }
    		 if(replicationValve != null) {
    			 //...
    		 }
    	}
    }
    
    /**
     * Find the master of the session state
     * @return master member of sessions 
     */
    protected Member findSessionMasterMember() {
    	Member mbr = null;
        Member mbrs[] = cluster.getMembers();
        if(mbrs.length != 0 ) 
        	mbr = mbrs[0];
        return mbr;
    }
    
    
    /**
     * Wait that cluster session state is transfer or timeout after 60 Sec
     * With stateTransferTimeout == -1 wait that backup is transfered (forever mode)
     */
    protected void waitForSendAllSessions(long beforeSendTime) {
    	long reqStart = System.currentTimeMillis();
    	long reqNow = reqStart ;
    	
    	boolean isTimeout = false;
    	if(getStateTransferTimeout() > 0) {
    		// wait that state is transfered with timeout check
    		do {
    			try {
    				Thread.sleep(100);
    			}
    			catch (Exception sleep) {
                    //
                }
    			
    			reqNow = System.currentTimeMillis();
    			isTimeout = ((reqNow - reqStart) > (1000 * getStateTransferTimeout()));
    		}while ((!getStateTransfered()) && (!isTimeout));
    	}
    	else{
    		if(getStateTransferTimeout() == -1) {
    			// wait that state is transfered
    			do {
                    try {
                        Thread.sleep(100);
                    } catch (Exception sleep) {
                    }
                } while ((!getStateTransfered()));
    			reqNow = System.currentTimeMillis();
    		}
    	}
    	
    	if (isTimeout || (!getStateTransfered())) {
    		counterNoStateTransfered++ ;
    		log.error("deltaManager.noSessionState");
    	}
    	else {
            if (log.isInfoEnabled())
                log.info("deltaManager.sessionReceived");
        }
    }
    
    
    
    /**
     * send a block of session to sender
     * @param sender
     * @param currentSessions
     * @param sendTimestamp
     * @throws IOException
     */
    protected void sendSessions(Member sender, Session[] currentSessions,long sendTimestamp) throws IOException {
    	byte[] data = serializeSessions(currentSessions);
    	SessionMessage newmsg = new SessionMessageImpl(name,SessionMessage.EVT_ALL_SESSION_DATA, data,"SESSION-STATE", "SESSION-STATE-" + getName());
    	newmsg.setTimestamp(sendTimestamp);
    	counterSend_EVT_ALL_SESSION_DATA++;
    	cluster.send(newmsg, sender);
    }
    
    
    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any. If persistence is not supported, this method returns
     * without doing anything.
     * 
     * @exception IOException
     *                if an input/output error occurs
     */
    protected byte[] serializeSessions(Session[] currentSessions) throws IOException {
    	// Open an output stream to the specified pathname, if any
        ByteArrayOutputStream fos = null;
        ObjectOutputStream oos = null;
        
        try {
        	fos = new ByteArrayOutputStream();
        	oos = new ObjectOutputStream(new BufferedOutputStream(fos));
        	oos.writeObject(new Integer(currentSessions.length));
        	for(int i=0 ; i < currentSessions.length;i++) {
                ((DeltaSession)currentSessions[i]).writeObjectData(oos);                
            }
        	// Flush and close the output stream
            oos.flush();
        }
        catch (IOException e) {
        	log.error("deltaManager.unloading.ioe");
            throw e;
        }
        finally {
        	if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
                oos = null;
            }
        }
        
        // send object data as byte[]
        return fos.toByteArray();
    }
    
    
    /**
     * Load sessions from other cluster node.
     * FIXME replace currently sessions with same id without notifcation.
     * FIXME SSO handling is not really correct with the session replacement!
     * @exception ClassNotFoundException
     *                if a serialized class cannot be found during the reload
     * @exception IOException
     *                if an input/output error occurs
     */
    protected void deserializeSessions(byte[] data) throws ClassNotFoundException,IOException {
    	
    	ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    	
    	ObjectInputStream ois = null;
    	// Load the previously unloaded active sessions
    	try {
    		ois = getReplicationStream(data);
    		Integer count = (Integer) ois.readObject();
    		
    		int n = count.intValue();
    		for (int i = 0; i < n; i++) {
    			DeltaSession session = (DeltaSession) createEmptySession();
    			session.readObjectData(ois);
    			session.setManager(this);
    			session.setValid(true);
    			session.setPrimarySession(false);
    			
    			session.access();
    			
    			if (findSession(session.getIdInternal()) == null ) {
    				sessionCounter++;
    			}
    			else {
    				//...
    			}
    			add(session);
    		}
    	}
    	catch (ClassNotFoundException e) {
    		log.error("deltaManager.loading.cnfe");
            throw e;
    	}
    	catch (IOException e) {
    		log.error("deltaManager.loading.ioe");
            throw e;
    	}
    	finally {
    		// Close the input stream
            try {
                if (ois != null) ois.close();
            } catch (IOException f) {
                // ignored
            }
            ois = null;
            if (originalLoader != null) 
            	Thread.currentThread().setContextClassLoader(originalLoader);
    	}
    }
	
	
	
	public ClusterManager cloneFromTemplate() {
		
		DeltaManager result = new DeltaManager();
		result.name = "Clone-from-"+name;
        return result;
	}
	
	
	
	// ------------------- Replication Methods -----------------
	
	/**
     * A message was received from another node, this is the callback method to
     * implement if you are interested in receiving replication messages.
     * 
     * @param cmsg -
     *            the message received.
     */
    public void messageDataReceived(ClusterMessage cmsg) {
    	if (cmsg != null && cmsg instanceof SessionMessage) {
    		SessionMessage msg = (SessionMessage) cmsg;
    		switch (msg.getEventType()) {
	            case SessionMessage.EVT_GET_ALL_SESSIONS:
	            case SessionMessage.EVT_SESSION_CREATED: 
	            case SessionMessage.EVT_SESSION_EXPIRED: 
	            case SessionMessage.EVT_SESSION_ACCESSED:
	            case SessionMessage.EVT_SESSION_DELTA:
	            case SessionMessage.EVT_CHANGE_SESSION_ID: {
	            	synchronized(receivedMessageQueue) {
	                    if(receiverQueue) {
	                        receivedMessageQueue.add(msg);
	                        return ;
	                    }
	                }
	            	break;
	            }
	            default: {
	                //we didn't queue, do nothing
	                break;
	            }
    		}
    		
    		messageReceived(msg, 
    				msg.getAddress() != null ? (Member) msg.getAddress() : null);
    	}

    }
    
    
    // ---------------------- message receive ----------------------

    
    /**
     * This method is called by the received thread when a SessionMessage has
     * been received from one of the other nodes in the cluster.
     * 
     * @param msg -
     *            the message received
     * @param sender -
     *            the sender of the message, this is used if we receive a
     *            EVT_GET_ALL_SESSION message, so that we only reply to the
     *            requesting node
     */
    protected void messageReceived(SessionMessage msg, Member sender) {
    	
    	ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
    	
    	try {
    		ClassLoader[] loaders = getClassLoaders();
    		if ( loaders != null && loaders.length > 0) 
    			Thread.currentThread().setContextClassLoader(loaders[0]);
    		
    		switch (msg.getEventType()) {
    			case SessionMessage.EVT_SESSION_CREATED: {
    				handleSESSION_CREATED(msg,sender);
    				break;
    			}
    			case SessionMessage.EVT_SESSION_EXPIRED: {
    				//...
                    break;
                }
    			
    			case SessionMessage.EVT_GET_ALL_SESSIONS: {
                    handleGET_ALL_SESSIONS(msg,sender);
                    break;
                }
    			
    			case SessionMessage.EVT_ALL_SESSION_DATA: {
                    handleALL_SESSION_DATA(msg,sender);
                    break;
                }
    			
    			default: {
                    //we didn't recognize the message type, do nothing
                    break;
                }
    		}
    	}
    	catch (Exception x) {
    		
    	}
    	finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    	
    	
    }
    
    
	// ----------------------- message receiver handler -----------------------
    
    /**
     * handle receive new session is created at other node (create backup - primary false)
     * @param msg
     * @param sender
     */
    protected void handleSESSION_CREATED(SessionMessage msg,Member sender) {
    	counterReceive_EVT_SESSION_CREATED++;
    	
    	DeltaSession session = (DeltaSession) createEmptySession();
    	session.setManager(this);
        session.setValid(true);
        session.setPrimarySession(false);
        session.setCreationTime(msg.getTimestamp());
        
        session.setMaxInactiveInterval(getMaxInactiveInterval(), false);
        session.access();
        session.setId(msg.getSessionID(), notifySessionListenersOnReplication);
        session.resetDeltaRequest();
        session.endAccess();
    	
    }
    
    
    /**
     * handle receive that other node want all sessions ( restart )
     * a) send all sessions with one message
     * b) send session at blocks
     * After sending send state is complete transfered
     * @param msg
     * @param sender
     * @throws IOException
     */
    protected void handleGET_ALL_SESSIONS(SessionMessage msg, Member sender) throws IOException {
    	counterReceive_EVT_GET_ALL_SESSIONS++;
    	
    	// Write the number of active sessions, followed by the details
    	// get all sessions and serialize without sync
    	Session[] currentSessions = findSessions();
    	long findSessionTimestamp = System.currentTimeMillis() ;
    	
    	if (isSendAllSessions()) {
    		sendSessions(sender, currentSessions, findSessionTimestamp);
    	}
    	
    }
    
    
    /**
     * handle receive sessions from other not ( restart )
     * @param msg
     * @param sender
     * @throws ClassNotFoundException
     * @throws IOException
     */
    protected void handleALL_SESSION_DATA(SessionMessage msg,Member sender) throws ClassNotFoundException, IOException {
    	counterReceive_EVT_ALL_SESSION_DATA++;
    	byte[] data = msg.getSession();
    	deserializeSessions(data);
    	
    }

}
