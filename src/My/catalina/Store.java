package My.catalina;

import java.io.IOException;

/**
 * A <b>Store</b> is the abstraction of a Catalina component that provides
 * persistent storage and loading of Sessions and their associated user data.
 * Implementations are free to save and load the Sessions to any media they
 * wish, but it is assumed that saved Sessions are persistent across
 * server or context restarts.
 */

public interface Store {

	// ------------------------------------------------------------- Properties
	
	/**
     * Return the Manager instance associated with this Store.
     */
    public Manager getManager();


    /**
     * Set the Manager associated with this Store.
     *
     * @param manager The Manager which will use this Store.
     */
    public void setManager(Manager manager);


    /**
     * Return the number of Sessions present in this Store.
     *
     * @exception IOException if an input/output error occurs
     */
    public int getSize() throws IOException;
    
    
	// --------------------------------------------------------- Public Methods

    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store.  If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */
    public String[] keys() throws IOException;
    
    
    /**
     * Load and return the Session associated with the specified session
     * identifier from this Store, without removing it.  If there is no
     * such stored Session, return <code>null</code>.
     *
     * @param id Session identifier of the session to load
     *
     * @exception ClassNotFoundException if a deserialization error occurs
     * @exception IOException if an input/output error occurs
     */
    public Session load(String id)
        throws ClassNotFoundException, IOException;


    /**
     * Remove the Session with the specified session identifier from
     * this Store, if present.  If no such Session is present, this method
     * takes no action.
     *
     * @param id Session identifier of the Session to be removed
     *
     * @exception IOException if an input/output error occurs
     */
    public void remove(String id) throws IOException;


    /**
     * Remove all Sessions from this Store.
     */
    public void clear() throws IOException;
    
    
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */
    public void save(Session session) throws IOException;
    
}
