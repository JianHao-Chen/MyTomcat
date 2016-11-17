package My.catalina.ha.session;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import My.catalina.Manager;
import My.catalina.ha.ClusterSession;
import My.catalina.session.StandardSession;

/**
*
* Similar to the StandardSession except that this session will keep
* track of deltas during a request.
*/

public class DeltaSession 
	extends StandardSession 
	implements Externalizable,ClusterSession{

	public static My.juli.logging.Log log = 
		My.juli.logging.LogFactory.getLog(DeltaSession.class);

	
	// -------------------- Instance Variables --------------------
	
	/**
     * only the primary session will expire, or be able to expire due to
     * inactivity. This is set to false as soon as I receive this session over
     * the wire in a session message. That means that someone else has made a
     * request on another server.
     */
    private transient boolean isPrimarySession = true;
    
    /**
     * The delta request contains all the action info
     *
     */
    private transient DeltaRequest deltaRequest = null;
    
    /**
     * Last time the session was replicatd, used for distributed expiring of
     * session
     */
    private transient long lastTimeReplicated = System.currentTimeMillis();
    
    protected final Lock diffLock = new ReentrantReadWriteLock().writeLock();
    
    private long version;
    
	// ---------------------- Constructors -----------------------------
    
    /**
     * Construct a new Session.
     */
    public DeltaSession() {
        this(null);
    }
    
	public DeltaSession(Manager manager) {
		super(manager);
		this.resetDeltaRequest();
	}
	
	
	
	/**
     * Lock during serialization
     */
    public void lock() {
        diffLock.lock();
    }
    
    /**
     * Unlock after serialization
     */
    public void unlock() {
        diffLock.unlock();
    }
	
	
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public boolean isPrimarySession() {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public void setPrimarySession(boolean primarySession) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void resetDeltaRequest() {
		try {
			lock();
			if (deltaRequest == null) {
				deltaRequest = new DeltaRequest(getIdInternal(), false);
			}
		}
		finally{
            unlock();
        }
	}
	
	public DeltaRequest getDeltaRequest() {
		if (deltaRequest == null) 
			resetDeltaRequest();
		return deltaRequest;
	}
	
	
}
