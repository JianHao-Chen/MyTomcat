package My.catalina.ha.session;

import My.catalina.ha.ClusterListener;
import My.catalina.ha.ClusterMessage;

public class JvmRouteSessionIDBinderListener extends ClusterListener{

	
	/**
     * Accept only SessionIDMessages
     * 
     * @param msg
     *            ClusterMessage
     * @return boolean - returns true to indicate that messageReceived should be
     *         invoked. If false is returned, the messageReceived method will
     *         not be invoked.
     */
    public boolean accept(ClusterMessage msg) {
        /*return (msg instanceof SessionIDMessage);*/
    	return false;
    }

	@Override
	public void messageReceived(ClusterMessage msg) {
		// TODO Auto-generated method stub
		
	}
}
