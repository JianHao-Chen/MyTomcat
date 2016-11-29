package My.catalina.ha.session;

import java.util.Map;

import My.catalina.ha.ClusterListener;
import My.catalina.ha.ClusterManager;
import My.catalina.ha.ClusterMessage;

public class ClusterSessionListener extends ClusterListener{

	/**
     * Callback from the cluster, when a message is received, The cluster will
     * broadcast it invoking the messageReceived on the receiver.
     */
	public void messageReceived(ClusterMessage myobj) {
		if (myobj != null && myobj instanceof SessionMessage) {
			SessionMessage msg = (SessionMessage) myobj;
			String ctxname = msg.getContextName();
			
			//check if the message is a EVT_GET_ALL_SESSIONS,
            //if so, wait until we are fully started up
			Map managers = cluster.getManagers() ;
			if (ctxname == null) {
				//..
			}
			else{
				ClusterManager mgr = (ClusterManager) managers.get(ctxname);
				
				if (mgr != null)
                    mgr.messageDataReceived(msg);
                else if (log.isWarnEnabled())
                    log.warn("Context manager doesn't exist:" + ctxname);
			}
		}
		return;
		
	}
	
	
	
	/**
     * Accept only SessionMessage
     * 
     * @param msg
     *            ClusterMessage
     * @return boolean - returns true to indicate that messageReceived should be
     *         invoked. If false is returned, the messageReceived method will
     *         not be invoked.
     */
    public boolean accept(ClusterMessage msg) {
        return (msg instanceof SessionMessage);
    }

	
}
