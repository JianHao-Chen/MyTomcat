package My.catalina.tribes;

/**
 * A ChannelInterceptor is an interceptor that intercepts 
 * messages and membership messages in the channel stack.
 * This allows interceptors to modify the message or perform
 * other actions when a message is sent or received.<br>
 * Interceptors are tied together in a linked list.
 */

public interface ChannelInterceptor 
	extends MembershipListener, Heartbeat{

	
	/**
     * An interceptor can react to a message based on a set bit on the 
     * message options. <br>
     * When a message is sent, the options can be retrieved from ChannelMessage.getOptions()
     * and if the bit is set, this interceptor will react to it.<br>
     * A simple evaluation if an interceptor should react to the message would be:<br>
     * <code>boolean react = (getOptionFlag() == (getOptionFlag() & ChannelMessage.getOptions()));</code><br>
     * The default option is 0, meaning there is no way for the application to trigger the
     * interceptor. The interceptor itself will decide.<br>
     * @return int
     * @see ChannelMessage#getOptions()
     */
    public int getOptionFlag();
    
    /**
     * Sets the option flag
     * @param flag int
     * @see #getOptionFlag()
     */
    public void setOptionFlag(int flag);
	
	
	/**
     * Set the next interceptor in the list of interceptors
     * @param next ChannelInterceptor
     */
    public void setNext(ChannelInterceptor next) ;

    /**
     * Retrieve the next interceptor in the list
     * @return ChannelInterceptor - returns the next interceptor in the list or null if no more interceptors exist
     */
    public ChannelInterceptor getNext();

    /**
     * Set the previous interceptor in the list
     * @param previous ChannelInterceptor
     */
    public void setPrevious(ChannelInterceptor previous);

    /**
     * Retrieve the previous interceptor in the list
     * @return ChannelInterceptor - returns the previous interceptor in the list or null if no more interceptors exist
     */
    public ChannelInterceptor getPrevious();
    
    /**
     * Intercepts the <code>Channel.hasMembers()</code> method
     * @return boolean - if the channel has members in its membership group
     * @see Channel#hasMembers()
     */
    public boolean hasMembers() ;

    /**
     * Intercepts the code>Channel.getMembers()</code> method
     * @return Member[]
     * @see Channel#getMembers()
     */
    public Member[] getMembers() ;

    /**
     * Intercepts the code>Channel.getMember(Member)</code> method
     * @param mbr Member
     * @return Member - the actual member information, including stay alive
     * @see Channel#getMember(Member)
     */
    public Member getMember(Member mbr);
    
    /**
     * Intercepts the code>Channel.getLocalMember(boolean)</code> method
     * @param incAliveTime boolean
     * @return Member
     * @see Channel#getLocalMember(boolean)
     */
    public Member getLocalMember(boolean incAliveTime) ;
    
    
    /**
     * Starts up the channel. This can be called multiple times for individual services to start
     * The svc parameter can be the logical or value of any constants
     * @param svc int value of <BR>
     * Channel.DEFAULT - will start all services <BR>
     * Channel.MBR_RX_SEQ - starts the membership receiver <BR>
     * Channel.MBR_TX_SEQ - starts the membership broadcaster <BR>
     * Channel.SND_TX_SEQ - starts the replication transmitter<BR>
     * Channel.SND_RX_SEQ - starts the replication receiver<BR>
     * @throws ChannelException if a startup error occurs or the service is already started.
     * @see Channel
     */
    public void start(int svc) throws ChannelException;

    /**
     * Shuts down the channel. This can be called multiple times for individual services to shutdown
     * The svc parameter can be the logical or value of any constants
     * @param svc int value of <BR>
     * Channel.DEFAULT - will shutdown all services <BR>
     * Channel.MBR_RX_SEQ - stops the membership receiver <BR>
     * Channel.MBR_TX_SEQ - stops the membership broadcaster <BR>
     * Channel.SND_TX_SEQ - stops the replication transmitter<BR>
     * Channel.SND_RX_SEQ - stops the replication receiver<BR>
     * @throws ChannelException if a startup error occurs or the service is already started.
     * @see Channel
     */
    public void stop(int svc) throws ChannelException;
}
