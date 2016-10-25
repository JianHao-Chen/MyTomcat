package My.catalina.ha;

/**
 * Receive SessionID cluster change from other backup node after primary session
 * node is failed.
 */

public abstract class ClusterListener {

	public static My.juli.logging.Log log = My.juli.logging.LogFactory.getLog(ClusterListener.class);
	
	
	//------------------ Instance Variables --------------------
	
	protected CatalinaCluster cluster = null;
	
	//--------------------- Constructor ----------------------

    public ClusterListener() {
    }
    
    // ----------------- Getters/Setters -----------------
    
    public CatalinaCluster getCluster() {
        return cluster;
    }

    public void setCluster(CatalinaCluster cluster) {
    	this.cluster = cluster;
    }
    
    
    // override
    public boolean equals(Object listener) {
        return super.equals(listener);
    }

    public int hashCode() {
        return super.hashCode();
    }
}
