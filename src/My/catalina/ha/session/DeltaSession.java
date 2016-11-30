package My.catalina.ha.session;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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
    
    
    public void setMaxInactiveInterval(int interval) {
        this.setMaxInactiveInterval(interval,true);
    }
    public void setMaxInactiveInterval(int interval, boolean addDeltaRequest) {
    	super.maxInactiveInterval = interval;
        if (isValid && interval == 0) {
            expire();
        } else {
        	if (addDeltaRequest && (deltaRequest != null)) {
        		//...
        	}
        }
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
	
	
	public void setId(String id, boolean notify) {
        super.setId(id, notify);
        resetDeltaRequest();
    }
	
	
	public void resetDeltaRequest() {
		try {
			lock();
			if (deltaRequest == null) {
				deltaRequest = new DeltaRequest(getIdInternal(), false);
			}
			else {
				deltaRequest.reset();
				deltaRequest.setSessionId(getIdInternal());
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
	
	/**
     * End the access and register to ReplicationValve (crossContext support)
     */
    public void endAccess() {
    	super.endAccess() ;
    	if(manager instanceof DeltaManager) {
    		((DeltaManager)manager).registerSessionAtReplicationValve(this);   
    	}
    }
    
    
    
    /**
     * Write a serialized version of the contents of this session object to the
     * specified object output stream, without requiring that the
     * StandardSession itself have been serialized.
     *
     * @param stream
     *            The object output stream to write to
     *
     * @exception IOException
     *                if an input/output error occurs
     */
    @Override
    public void writeObjectData(ObjectOutputStream stream) throws IOException {
        writeObjectData((ObjectOutput)stream);
    }
    
    public void writeObjectData(ObjectOutput stream) throws IOException {
        writeObject(stream);
    }
    
    
    /**
     * Write a serialized version of this session object to the specified object
     * output stream.
     * <p>
     * <b>IMPLEMENTATION NOTE </b>: The owning Manager will not be stored in the
     * serialized representation of this Session. After calling
     * <code>readObject()</code>, you must set the associated Manager
     * explicitly.
     * <p>
     * <b>IMPLEMENTATION NOTE </b>: Any attribute that is not Serializable will
     * be unbound from the session, with appropriate actions if it implements
     * HttpSessionBindingListener. If you do not want any such attributes, be
     * sure the <code>distributable</code> property of the associated Manager
     * is set to <code>true</code>.
     *
     * @param stream
     *            The output stream to write to
     *
     * @exception IOException
     *                if an input/output error occurs
     */
    protected void writeObject(ObjectOutputStream stream) throws IOException {
        writeObject((ObjectOutput)stream);
    }
    
    private void writeObject(ObjectOutput stream) throws IOException {
    	
    	stream.writeObject(new Long(creationTime));
        stream.writeObject(new Long(lastAccessedTime));
        stream.writeObject(new Integer(maxInactiveInterval));
        stream.writeObject(new Boolean(isNew));
        stream.writeObject(new Boolean(isValid));
        stream.writeObject(new Long(thisAccessedTime));
        stream.writeObject(new Long(version));
        
        stream.writeObject(id);
        
        // handle attribute latter..
    }
	
	
}
