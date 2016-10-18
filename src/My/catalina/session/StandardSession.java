package My.catalina.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import My.catalina.Context;
import My.catalina.Manager;
import My.catalina.Session;
import My.catalina.session.ManagerBase.SessionTiming;
import My.catalina.util.Enumerator;

/**
 * Standard implementation of the <b>Session</b> interface.  This object is
 * serializable, so that it can be stored in persistent storage or transferred
 * to a different JVM for distributable session support.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  An instance of this class represents both the
 * internal (Session) and application level (HttpSession) view of the session.
 * However, because the class itself is not declared public, Java logic outside
 * of the <code>org.apache.catalina.session</code> package cannot cast an
 * HttpSession view of this instance back to a Session view.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  If you add fields to this class, you must
 * make sure that you carry them over in the read/writeObject methods so
 * that this class is properly serialized.
 */

public class StandardSession 
	implements HttpSession, Session, Serializable {

	
	// --------------------- Constructors ------------------------------
	
	/**
     * Construct a new Session associated with the specified Manager.
     *
     * @param manager The manager with which this Session is associated
     */
    public StandardSession(Manager manager) {
    	super();
    	this.manager = manager;
    }
	
	// ------------------- Instance Variables ----------------------
    
    
    /**
     * The collection of user data attributes associated with this Session.
     */
    protected Map attributes = new ConcurrentHashMap();
    
    
    /**
     * The time this session was created, in milliseconds since midnight,
     * January 1, 1970 GMT.
     */
    protected long creationTime = 0L;
    
    
    
    /**
     * We are currently processing a session expiration, so bypass
     * certain IllegalStateException tests.  NOTE:  This value is not
     * included in the serialized version of this object.
     */
    protected transient volatile boolean expiring = false;
    
    
    /**
     * The facade associated with this session.  NOTE:  This value is not
     * included in the serialized version of this object.
     */
    protected transient StandardSessionFacade facade = null;
    
    
    /**
     * The session identifier of this Session.
     */
    protected String id = null;
    
    
    /**
     * The last accessed time for this Session.
     */
    protected volatile long lastAccessedTime = creationTime;
    
    
    /**
     * The session event listeners for this Session.
     */
    protected transient ArrayList listeners = new ArrayList();
    
    
    /**
     * The Manager with which this Session is associated.
     */
    protected transient Manager manager = null;
    
    
    /**
     * The maximum time interval, in seconds, between client requests before
     * the servlet container may invalidate this session.  A negative time
     * indicates that the session should never time out.
     */
    protected int maxInactiveInterval = -1;
    
    
    /**
     * Flag indicating whether this session is new or not.
     */
    protected boolean isNew = false;


    /**
     * Flag indicating whether this session is valid or not.
     */
    protected volatile boolean isValid = false;
	
    /**
     * The current accessed time for this session.
     */
    protected volatile long thisAccessedTime = creationTime;


    /**
     * The access count for this session.
     */
    protected transient AtomicInteger accessCount = null;
    
    
    
	// ------------------ Session Properties ---------------------
    
    /**
     * Update the accessed time information for this session.  This method
     * should be called by the context when a request comes in for a particular
     * session, even if the application does not reference it.
     */
    public void access() {
    	this.lastAccessedTime = this.thisAccessedTime;
        this.thisAccessedTime = System.currentTimeMillis();
    }
    
    
    /**
     * Set the creation time for this session.  This method is called by the
     * Manager when an existing Session instance is reused.
     *
     * @param time The new creation time
     */
    public void setCreationTime(long time) {

        this.creationTime = time;
        this.lastAccessedTime = time;
        this.thisAccessedTime = time;

    }

    

    /**
     * Return the session identifier for this session.
     */
    public String getId() {

        return (this.id);

    }
    
    /**
     * Set the session identifier for this session.
     *
     * @param id The new session identifier
     */
    public void setId(String id) {
        setId(id, true);
    }
    
    
    public void setId(String id, boolean notify) {

    	if ((this.id != null) && (manager != null))
            manager.remove(this);

        this.id = id;

        if (manager != null)
            manager.add(this);
        
        if (notify) {
            tellNew();
        }
    }
    
    
    
    /**
     * Inform the listeners about the new session.
     *
     */
    public void tellNew() {
    	
    	 // Notify interested session event listeners
        fireSessionEvent(Session.SESSION_CREATED_EVENT, null);
    	
    }
    
    


    /**
     * Return the session identifier for this session.
     */
    public String getIdInternal() {

        return (this.id);

    }


    /**
     * Return the Manager within which this Session is valid.
     */
    public Manager getManager() {

        return (this.manager);

    }
   
    /**
     * Set the Manager within which this Session is valid.
     *
     * @param manager The new Manager
     */
    public void setManager(Manager manager) {

        this.manager = manager;

    }
    
    
    /**
     * Set the <code>isNew</code> flag for this session.
     *
     * @param isNew The new value for the <code>isNew</code> flag
     */
    public void setNew(boolean isNew) {

        this.isNew = isNew;

    }


	@Override
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}


	@Override
	public boolean isValid() {
		if (this.expiring) {
            return true;
        }

        if (!this.isValid) {
            return false;
        }
        
        if (maxInactiveInterval >= 0) { 
        	long timeNow = System.currentTimeMillis();
            int timeIdle = (int) ((timeNow - thisAccessedTime) / 1000L);
            if (timeIdle >= maxInactiveInterval) {
                expire(true);
            }
        }
        return (this.isValid);
	}
	
	
	/**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     */
    public void expire() {

        expire(true);

    }
    
    
    
    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     *
     * @param notify Should we notify listeners about the demise of
     *  this session?
     */
    public void expire(boolean notify) {
    	
    	// Check to see if expire is in progress or has previously been called
        if (expiring || !isValid)
            return;
        
        synchronized (this) {
        	// Check again, now we are inside the sync so this code only runs once
            // Double check locking - expiring and isValid need to be volatile
            if (expiring || !isValid)
                return;

            if (manager == null)
                return;
            
            // Mark this session as "being expired"
            expiring = true;
            
            // Notify interested application event listeners
            Context context = (Context) manager.getContainer();
            
            
            setValid(false);
            
            
            /*
             * Compute how long this session has been alive, and update
             * session manager's related properties accordingly
             */
            long timeNow = System.currentTimeMillis();

            int timeAlive = (int) ((timeNow - creationTime)/1000);
            
            synchronized (manager) {
            	if (timeAlive > manager.getSessionMaxAliveTime()) {
            		manager.setSessionMaxAliveTime(timeAlive);
            	}
            	int numExpired = manager.getExpiredSessions();
            	
            	if (numExpired < Integer.MAX_VALUE) {
            		numExpired++;
                    manager.setExpiredSessions(numExpired);
            	}
            	
            	 int average = manager.getSessionAverageAliveTime();
                 // Using long, as otherwise (average * numExpired) might overflow 
                 average = (int) (((((long) average) * (numExpired - 1)) + timeAlive)
                         / numExpired);
                 manager.setSessionAverageAliveTime(average);
            }
            
            if (manager instanceof ManagerBase) {
            	
            	ManagerBase mb = (ManagerBase) manager;
                SessionTiming timing = new SessionTiming(timeNow, timeAlive);
                synchronized (mb.sessionExpirationTiming) {
                    mb.sessionExpirationTiming.add(timing);
                    mb.sessionExpirationTiming.poll();
                }
            }
            
            // Remove this session from our manager's active sessions
            manager.remove(this);
            
            // Notify interested session event listeners
            if (notify) {
                fireSessionEvent(Session.SESSION_DESTROYED_EVENT, null);
            }
            
            
            // We have completed expire of this session
            expiring = false;
            
        }
    }
    
    
    
    /**
     * Perform the internal processing required to passivate
     * this session.
     */
    public void passivate() {
    	// listeners
    }
    
    
    /**
     * Write a serialized version of the contents of this session object to
     * the specified object output stream, without requiring that the
     * StandardSession itself have been serialized.
     *
     * @param stream The object output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    public void writeObjectData(ObjectOutputStream stream)
        throws IOException {
    	
    	writeObject(stream);
    }
	
    
    /**
     * Write a serialized version of this session object to the specified
     * object output stream.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The owning Manager will not be stored
     * in the serialized representation of this Session.  After calling
     * <code>readObject()</code>, you must set the associated Manager
     * explicitly.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  Any attribute that is not Serializable
     * will be unbound from the session, with appropriate actions if it
     * implements HttpSessionBindingListener.  If you do not want any such
     * attributes, be sure the <code>distributable</code> property of the
     * associated Manager is set to <code>true</code>.
     *
     * @param stream The output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void writeObject(ObjectOutputStream stream) throws IOException {
    	
    	// Write the scalar instance variables (except Manager)
    	stream.writeObject(new Long(creationTime));
    	stream.writeObject(new Long(lastAccessedTime));
        stream.writeObject(new Integer(maxInactiveInterval));
        stream.writeObject(new Boolean(isNew));
        stream.writeObject(new Boolean(isValid));
        stream.writeObject(new Long(thisAccessedTime));
        stream.writeObject(id);
        
        
        // handle attributes ...
        
        
    }
    
    
    
    /**
     * Read a serialized version of the contents of this session object from
     * the specified object input stream, without requiring that the
     * StandardSession itself have been serialized.
     *
     * @param stream The object input stream to read from
     *
     * @exception ClassNotFoundException if an unknown class is specified
     * @exception IOException if an input/output error occurs
     */
    public void readObjectData(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {

        readObject(stream);

    }
    
    
    /**
     * Read a serialized version of this session object from the specified
     * object input stream.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The reference to the owning Manager
     * is not restored by this method, and must be set explicitly.
     *
     * @param stream The input stream to read from
     *
     * @exception ClassNotFoundException if an unknown class is specified
     * @exception IOException if an input/output error occurs
     */
    protected void readObject(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {
    	
    	creationTime = ((Long) stream.readObject()).longValue();
        lastAccessedTime = ((Long) stream.readObject()).longValue();
        maxInactiveInterval = ((Integer) stream.readObject()).intValue();
        isNew = ((Boolean) stream.readObject()).booleanValue();
        isValid = ((Boolean) stream.readObject()).booleanValue();
        thisAccessedTime = ((Long) stream.readObject()).longValue();
        
        id = (String) stream.readObject();
        
        
        // handle attributes ...
    }
	
	


	@Override
	public long getCreationTime() {
		if (!isValidInternal())
            throw new IllegalStateException
                ("standardSession.getCreationTime.ise");

        return (this.creationTime);
	}

	 /**
     * Return the last time the client sent a request associated with this
     * session, as the number of milliseconds since midnight, January 1, 1970
     * GMT.  Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access time.
     */
	public long getLastAccessedTime() {
		if (!isValidInternal()) {
            throw new IllegalStateException
                ("standardSession.getLastAccessedTime.ise");
        }

        return (this.lastAccessedTime);
	}


	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}


	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
        if (isValid && interval == 0) {
           // expire();
        }
	}

	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setAttribute(String name, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Enumeration getAttributeNames() {
		if (!isValidInternal())
            throw new IllegalStateException
                ("standardSession.getAttributeNames.ise");

        return (new Enumerator(attributes.keySet(), true));
	}


	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/**
     * Return the <code>HttpSession</code> for which this object
     * is the facade.
     */
    public HttpSession getSession() {
    	
    	if (facade == null){
    		facade = new StandardSessionFacade(this);
    	}
    	return (facade);
    }
    
	
    
    /**
     * End the access.
     */
    public void endAccess() {

        isNew = false;

    }
    
    
    
    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
    	
    	
    	attributes.clear();
    	
    	creationTime = 0L;
        expiring = false;
        id = null;
        lastAccessedTime = 0L;
        maxInactiveInterval = -1;
        
        isNew = false;
        isValid = false;
        manager = null;
    }
	
	
	// ---------------------- Protected Methods ----------------------------
	
	/**
     * Fire container events if the Context implementation is the
     * <code>org.apache.catalina.core.StandardContext</code>.
     *
     * @param context Context for which to fire events
     * @param type Event type
     * @param data Event data
     *
     * @exception Exception occurred during event firing
     */
    protected void fireContainerEvent(Context context,
                                    String type, Object data)
        throws Exception {
    	
    }
    
    
    /**
     * Notify all session event listeners that a particular event has
     * occurred for this Session.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireSessionEvent(String type, Object data) {
    	
    }
    
    
    
    /**
     * Return a string representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("StandardSession[");
        sb.append(id);
        sb.append("]");
        return (sb.toString());

    }
    
    
    
	// ------------------ HttpSession Protected Methods-------------------
    /**
     * Return the <code>isValid</code> flag for this session without any expiration
     * check.
     */
    protected boolean isValidInternal() {
        return (this.isValid || this.expiring);
    }
    
}
