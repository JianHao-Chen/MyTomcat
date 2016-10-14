package My.catalina;

/**
 * A <b>Session</b> is the Catalina-internal facade for an
 * <code>HttpSession</code> that is used to maintain state information
 * between requests for a particular user of a web application.
 */

public interface Session {

	// ------------------- Manifest Constants -----------------
	/**
     * The SessionEvent event type when a session is created.
     */
    public static final String SESSION_CREATED_EVENT = "createSession";


    /**
     * The SessionEvent event type when a session is destroyed.
     */
    public static final String SESSION_DESTROYED_EVENT = "destroySession";


    /**
     * The SessionEvent event type when a session is activated.
     */
    public static final String SESSION_ACTIVATED_EVENT = "activateSession";


    /**
     * The SessionEvent event type when a session is passivated.
     */
    public static final String SESSION_PASSIVATED_EVENT = "passivateSession";
    
    
	// --------------------- Properties ---------------------
    
    
    
    /**
     * Set the <code>isValid</code> flag for this session.
     *
     * @param isValid The new value for the <code>isValid</code> flag
     */
    public void setValid(boolean isValid);


    /**
     * Return the <code>isValid</code> flag for this session.
     */
    public boolean isValid();
    
    
}
