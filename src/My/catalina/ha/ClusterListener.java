package My.catalina.ha;

import java.io.Serializable;

import My.catalina.tribes.Member;

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
    
    
    public final void messageReceived(Serializable msg, Member member) {
        if ( msg instanceof ClusterMessage ) 
        	messageReceived((ClusterMessage)msg);
    }
    public final boolean accept(Serializable msg, Member member) {
        if ( msg instanceof ClusterMessage ) 
        	return true;
        return false;
    }
    
    
    /**
     * Callback from the cluster, when a message is received, The cluster will
     * broadcast it invoking the messageReceived on the receiver.
     * 
     * @param msg
     *            ClusterMessage - the message received from the cluster
     */
    public abstract void messageReceived(ClusterMessage msg) ;
    
    
    /**
     * Accept only SessionIDMessages
     * 
     * @param msg
     *            ClusterMessage
     * @return boolean - returns true to indicate that messageReceived should be
     *         invoked. If false is returned, the messageReceived method will
     *         not be invoked.
     */
    public abstract boolean accept(ClusterMessage msg) ;
    
    
}
