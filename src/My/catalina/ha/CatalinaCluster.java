package My.catalina.ha;

import My.catalina.Cluster;

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
    
    
	
	
}
