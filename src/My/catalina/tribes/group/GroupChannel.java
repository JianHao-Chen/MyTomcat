package My.catalina.tribes.group;

import My.catalina.tribes.ManagedChannel;

/**
 * The default implementation of a Channel.<br>
 * The GroupChannel manages the replication channel. It coordinates
 * message being sent and received with membership announcements.
 * The channel has an chain of interceptors that can modify the message or perform other logic.<br>
 * It manages a complete group, both membership and replication.
 */

public class GroupChannel 
	extends ChannelInterceptorBase 
	implements ManagedChannel{

	@Override
	public void heartbeat() {
		// TODO Auto-generated method stub
		
	}

	
	
	
}
