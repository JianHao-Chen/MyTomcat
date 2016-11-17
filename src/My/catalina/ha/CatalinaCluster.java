package My.catalina.ha;

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
	
}
