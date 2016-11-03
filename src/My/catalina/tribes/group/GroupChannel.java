package My.catalina.tribes.group;

import java.util.ArrayList;
import java.util.Iterator;

import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelInterceptor;
import My.catalina.tribes.ChannelListener;
import My.catalina.tribes.ManagedChannel;
import My.catalina.tribes.Member;
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
    
    
    /**
     * Internal heartbeat thread
     */
    protected HeartbeatThread hbthread = null;
    
    /**
     * The  <code>ChannelCoordinator</code> coordinates the bottom layer components:<br>
     * - MembershipService<br>
     * - ChannelSender <br>
     * - ChannelReceiver<br>
     */
    protected ChannelCoordinator coordinator = new ChannelCoordinator();
    
    /**
     * The first interceptor in the inteceptor stack.
     * The interceptors are chained in a linked list, so we only need a reference to the
     * first one
     */
    protected ChannelInterceptor interceptors = null;
    
    
    /**
     * Creates a GroupChannel. This constructor will also
     * add the first interceptor in the GroupChannel.<br>
     * The first interceptor is always the channel itself.
     */
    public GroupChannel() {
    	addInterceptor(this);
    }
	
    
    /**
     * Adds an interceptor to the stack for message processing<br>
     * Interceptors are ordered in the way they are added.<br>
     * <code>channel.addInterceptor(A);</code><br>
     * <code>channel.addInterceptor(C);</code><br>
     * <code>channel.addInterceptor(B);</code><br>
     * Will result in a interceptor stack like this:<br>
     * <code>A -> C -> B</code><br>
     * The complete stack will look like this:<br>
     * <code>Channel -> A -> C -> B -> ChannelCoordinator</code><br>
     * @param interceptor ChannelInterceptorBase
     */
    public void addInterceptor(ChannelInterceptor interceptor) {
    	if ( interceptors == null ) {
    		interceptors = interceptor;
            interceptors.setNext(coordinator);
            interceptors.setPrevious(null);
            coordinator.setPrevious(interceptors);
    	}else{
    		ChannelInterceptor last = interceptors;
    		while ( last.getNext() != coordinator ) {
    			last = last.getNext();
    		}
    		last.setNext(interceptor);
            interceptor.setNext(coordinator);
            interceptor.setPrevious(last);
            coordinator.setPrevious(interceptor);
    		
    	}
    	
    }
    
    
	
	
    /**
     * Sends a heartbeat through the interceptor stack.<br>
     * Invoke this method from the application on a periodic basis if
     * you have turned off internal heartbeats <code>channel.setHeartbeat(false)</code>
     */
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
    	
    	super.start(svc);
    	
    }
    
    
    
    
    /**
     * Returns the first interceptor of the stack. Useful for traversal.
     * @return ChannelInterceptor
     */
    public ChannelInterceptor getFirstInterceptor() {
    	if (interceptors != null) 
    		return interceptors;
        else 
        	return coordinator;
    }
    
    
    /**
     * Returns an iterator of all the interceptors in this stack
     * @return Iterator
     */
    public Iterator getInterceptors() {
    	return new InterceptorIterator(this.getNext(),this.coordinator);
    }
    
	
    /**
     * Sets up the default implementation interceptor stack
     * if no interceptors have been added
     * @throws ChannelException
     */
    protected synchronized void setupDefaultStack() throws ChannelException {
    	
    	if ( getFirstInterceptor() != null &&
                ((getFirstInterceptor().getNext() instanceof ChannelCoordinator))) {
    		//...
    	}
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
   
   
   
   
   
   /**
    * memberAdded gets invoked by the interceptor below the channel
    * and the channel will broadcast it to the membership listeners
    * @param member Member - the new member
    */
	public void memberAdded(Member member) {
		//notify upwards
		for (int i=0; i<membershipListeners.size(); i++ ) {
			MembershipListener membershipListener = 
				(MembershipListener)membershipListeners.get(i);
			if (membershipListener != null) 
				membershipListener.memberAdded(member);
		}
   }
   
   
   
   
   /**
   *
   * <p>Title: Interceptor Iterator</p>
   *
   * <p>Description: An iterator to loop through the interceptors in a channel</p>
   *
   * @version 1.0
   */
  public static class InterceptorIterator implements Iterator {
	  
	  private ChannelInterceptor end;
      private ChannelInterceptor start;
	  
      public InterceptorIterator(ChannelInterceptor start, ChannelInterceptor end) {
          this.end = end;
          this.start = start;
      }
      
      public boolean hasNext() {
          return start!=null && start != end;
      }
      
      public Object next() {
    	  Object result = null;
          if ( hasNext() ) {
        	  result = start;
              start = start.getNext();
          }
          return result;
      }
      
      public void remove() {
          //empty operation
      }
	  
  }
   
   
   
   
   
   
   
   
   /**
   *
   * <p>Title: Internal heartbeat thread</p>
   *
   * <p>Description: if <code>Channel.getHeartbeat()==true</code> then a thread of this class
   * is created</p>
   *
   * @version 1.0
   */
  public static class HeartbeatThread extends Thread {
	  
	  protected static My.juli.logging.Log log = My.juli.logging.LogFactory.getLog(HeartbeatThread.class);
	  
	  protected static int counter = 1;
	  
	  protected static synchronized int inc() {
          return counter++;
      }
	  
	  protected boolean doRun = true;
      protected GroupChannel channel;
      protected long sleepTime;
      
      public HeartbeatThread(GroupChannel channel, long sleepTime) {
    	  super();
          this.setPriority(MIN_PRIORITY);
          setName("GroupChannel-Heartbeat-"+inc());
          setDaemon(true);
          this.channel = channel;
          this.sleepTime = sleepTime;
      }
      
      public void stopHeartbeat() {
          doRun = false;
          interrupt();
      }
      
      public void run() {
    	  while (doRun) {
    		  try {
    			  Thread.sleep(sleepTime);
                  channel.heartbeat();
    		  }catch ( InterruptedException x ) {
    			  interrupted();
    		  }
    		  catch ( Exception x ) {
                  log.error("Unable to send heartbeat through Tribes interceptor stack. Will try to sleep again.",x);
              }
    	  }
      }
	  
  }




















   
   
   
	
}
