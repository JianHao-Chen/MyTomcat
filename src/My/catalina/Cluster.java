package My.catalina;

/**
 * A <b>Cluster</b> works as a Cluster client/server for the local host
 * Different Cluster implementations can be used to support different
 * ways to communicate within the Cluster. A Cluster implementation is
 * responsible for setting up a way to communicate within the Cluster
 * and also supply "ClientApplications" with <code>ClusterSender</code>
 * used when sending information in the Cluster and
 * <code>ClusterInfo</code> used for receiving information in the Cluster.
 */

public interface Cluster {

	// ----------------------- Properties -----------------------
	
	/**
     * Return descriptive information about this Cluster implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();
    
	/**
     * Return the name of the cluster that this Server is currently
     * configured to operate within.
     *
     * @return The name of the cluster associated with this server
     */
    public String getClusterName();

    /**
     * Set the name of the cluster to join, if no cluster with
     * this name is present create one.
     *
     * @param clusterName The clustername to join
     */
    public void setClusterName(String clusterName);
    
    
    
    /**
     * Set the Container associated with our Cluster
     *
     * @param container The Container to use
     */
    public void setContainer(Container container);

    /**
     * Get the Container associated with our Cluster
     *
     * @return The Container associated with our Cluster
     */
    public Container getContainer();
    
    
    /**
     * Create a new manager which will use this cluster to replicate its
     * sessions.
     *
     * @param name Name (key) of the application with which the manager is
     * associated
     */
    public Manager createManager(String name);
}
