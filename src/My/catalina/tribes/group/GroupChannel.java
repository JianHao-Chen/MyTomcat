package My.catalina.tribes.group;

import java.util.ArrayList;

import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelListener;
import My.catalina.tribes.ManagedChannel;
import My.catalina.tribes.MembershipListener;

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

	/**
     * Flag to determine if the channel manages its own heartbeat
     * If set to true, the channel will start a local thread for the heart beat.
     */
    protected boolean heartbeat = true;
    
    /**
     * If <code>heartbeat == true</code> then how often do we want this
     * heartbeat to run. default is one minute
     */
    protected long heartbeatSleeptime = 5*1000;//every 5 seconds
    
    
    
    
    
    
    
    
	
	
	
	@Override
	public void heartbeat() {
		// TODO Auto-generated method stub
		
	}
	
	
	 /**
     * Starts the channel
     * @param svc int - what service to start
     * @throws ChannelException
     * @see org.apache.catalina.tribes.Channel#start(int)
     */
    public synchronized void start(int svc) throws ChannelException {
    	
    	setupDefaultStack();
    	
    }
	
    /**
     * Sets up the default implementation interceptor stack
     * if no interceptors have been added
     * @throws ChannelException
     */
    protected synchronized void setupDefaultStack() throws ChannelException {
    	
    }
	
	
	
	
	

	
	/**
     * A list of membership listeners that subscribe to membership announcements
     */
    protected ArrayList membershipListeners = new ArrayList();
	
	/**
     * Adds a membership listener to the channel.<br>
     * Membership listeners are uniquely identified using the equals(Object) method
     * @param membershipListener MembershipListener
     */
    public void addMembershipListener(MembershipListener membershipListener) {
        if (!this.membershipListeners.contains(membershipListener) )
            this.membershipListeners.add(membershipListener);
    }

    /**
     * Removes a membership listener from the channel.<br>
     * Membership listeners are uniquely identified using the equals(Object) method
     * @param membershipListener MembershipListener
     */

    public void removeMembershipListener(MembershipListener membershipListener) {
        membershipListeners.remove(membershipListener);
    }
    
    
    
    /**
     * A list of channel listeners that subscribe to incoming messages
     */
    protected ArrayList channelListeners = new ArrayList();
    
    /**
     * Adds a channel listener to the channel.<br>
     * Channel listeners are uniquely identified using the equals(Object) method
     * @param channelListener ChannelListener
     */
    public void addChannelListener(ChannelListener channelListener) {
        if (!this.channelListeners.contains(channelListener) ) {
            this.channelListeners.add(channelListener);
        } else {
            throw new IllegalArgumentException("Listener already exists:"+channelListener+"["+channelListener.getClass().getName()+"]");
        }
    }
    
    /**
    *
    * Removes a channel listener from the channel.<br>
    * Channel listeners are uniquely identified using the equals(Object) method
    * @param channelListener ChannelListener
    */
   public void removeChannelListener(ChannelListener channelListener) {
       channelListeners.remove(channelListener);
   }
	
}
