package My.catalina;

/**
 * A <b>Manager</b> manages the pool of Sessions that are associated with a
 * particular Container.  Different Manager implementations may support
 * value-added features such as the persistent storage of session data,
 * as well as migrating sessions for distributable web applications.
 * <p>
 * In order for a <code>Manager</code> implementation to successfully operate
 * with a <code>Context</code> implementation that implements reloading, it
 * must obey the following constraints:
 * <ul>
 * <li>Must implement <code>Lifecycle</code> so that the Context can indicate
 *     that a restart is required.
 * <li>Must allow a call to <code>stop()</code> to be followed by a call to
 *     <code>start()</code> on the same <code>Manager</code> instance.
 * </ul>
 */

public interface Manager {

	// ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Manager is associated.
     */
    public Container getContainer();


    /**
     * Set the Container with which this Manager is associated.
     *
     * @param container The newly associated Container
     */
    public void setContainer(Container container);
    
    
    /**
     * Add this Session to the set of active Sessions for this Manager.
     *
     * @param session Session to be added
     */
    public void add(Session session);
    
    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session Session to be removed
     */
    public void remove(Session session);
    
    
    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id specified will be used as the session id.
     * If a new session cannot be created for any reason, return 
     * <code>null</code>.
     * 
     * @param sessionId The session id which should be used to create the
     *  new session; if <code>null</code>, the session
     *  id will be assigned by this method, and available via the getId()
     *  method of the returned session.
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession(String sessionId);
    
}
