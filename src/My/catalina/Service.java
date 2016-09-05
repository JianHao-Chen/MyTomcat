package My.catalina;

import My.catalina.connector.Connector;

/**
* A <strong>Service</strong> is a group of one or more
* <strong>Connectors</strong> that share a single <strong>Container</strong>
* to process their incoming requests.  This arrangement allows, for example,
* a non-SSL and SSL connector to share the same population of web apps.
* <p>
* A given JVM can contain any number of Service instances; however, they are
* completely independent of each other and share only the basic JVM facilities
* and classes on the system class path.
*
*/

public interface Service {

	// ---------------------- Properties ----------------------
	/**
    * Return the <code>Container</code> that handles requests for all
    * <code>Connectors</code> associated with this Service.
    */
    public Container getContainer();
	
    
    /**
     * Set the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     *
     * @param container The new Container
     */
    public void setContainer(Container container);
    

    /**
     * Return the name of this Service.
     */
    public String getName();

    /**
     * Set the name of this Service.
     *
     * @param name The new service name
     */
    public void setName(String name);

    /**
     * Return the <code>Server</code> with which we are associated (if any).
     */
    public Server getServer();

    /**
     * Set the <code>Server</code> with which we are associated (if any).
     *
     * @param server The server that owns this Service
     */
    public void setServer(Server server);

    
    
    
 // ---------------------- Public Methods ----------------------
    
    /**
     * Add a new Connector to the set of defined Connectors, and associate it
     * with this Service's Container.
     *
     * @param connector The Connector to be added
     */
    public void addConnector(Connector connector);

    /**
     * Find and return the set of Connectors associated with this Service.
     */
    public Connector[] findConnectors();

    /**
     * Remove the specified Connector from the set associated from this
     * Service.  The removed Connector will also be disassociated from our
     * Container.
     *
     * @param connector The Connector to be removed
     */
    public void removeConnector(Connector connector);

    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     *
     * @exception LifecycleException If this server was already initialized.
     */
    public void initialize() throws LifecycleException;

    /**
     * Adds a named executor to the service
     * @param ex Executor
     */
    public void addExecutor(Executor ex);

    /**
     * Retrieves all executors
     * @return Executor[]
     */
    public Executor[] findExecutors();

    /**
     * Retrieves executor by name, null if not found
     * @param name String
     * @return Executor
     */
    public Executor getExecutor(String name);
    
    /**
     * Removes an executor from the service
     * @param ex Executor
     */
    public void removeExecutor(Executor ex);
}
