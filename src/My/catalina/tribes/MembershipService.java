package My.catalina.tribes;

/**
 * MembershipService Interface<br>
 * The <code>MembershipService</code> interface is the membership component 
 * at the bottom layer, the IO layer (for layers see the javadoc for the {@link Channel} interface).<br>
 */

public interface MembershipService {

	public static final int MBR_RX = Channel.MBR_RX_SEQ;
    public static final int MBR_TX = Channel.MBR_TX_SEQ;
    
    
    
    
    
    /**
     * Starts the membership service. If a membership listeners is added
     * the listener will start to receive membership events.
     * @param level - level MBR_RX starts listening for members, level MBR_TX 
     * starts broad casting the server
     * @throws java.lang.Exception if the service fails to start.
     * @throws java.lang.IllegalArgumentException if the level is incorrect.
     */
    public void start(int level) throws java.lang.Exception;
    
    
    
    
	/**
     * Sets the local member properties for broadcasting
     */
    public void setLocalMemberProperties(String listenHost, int listenPort);
    
    /**
     * Sets the membership listener, only one listener can be added.
     * If you call this method twice, the last listener will be used.
     * @param listener The listener
     */
    public void setMembershipListener(MembershipListener listener);
    
    
    
    /**
     * Returns the member object that defines this member
     */
    public Member getLocalMember(boolean incAliveTime);
    
}
