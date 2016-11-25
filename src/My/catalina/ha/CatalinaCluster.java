package My.catalina.ha;

import java.util.Map;

import My.catalina.ha.ClusterMessage;
import My.catalina.Cluster;
import My.catalina.tribes.Member;

/**
 * A <b>CatalinaCluster</b> interface allows to plug in and out the 
 * different cluster implementations
 */

public interface CatalinaCluster extends Cluster{

	// ------------------ Instance Variables ------------------
	/**
     * Descriptive information about this component implementation.
     */
    public String info = "CatalinaCluster/2.0";
    
    
    /**
     * Returns all the members currently participating in the cluster.
     *
     * @return Member[]
     */
    public Member[] getMembers();
    
    
    
    /**
     * Sends a message to all the members in the cluster
     * @param msg ClusterMessage
     */
    public void send(ClusterMessage msg);
    
    /**
     * Sends a message to a all members at local cluster domain
     *
     * @param msg ClusterMessage
     */
    public void sendClusterDomain(ClusterMessage msg);
    
    
    /**
     * @return The map of managers
     */
    public Map getManagers();
	
}
