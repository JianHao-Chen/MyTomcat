package My.catalina.tribes;

/**
 * Channel interface<br>
 * A channel is a representation of a group of nodes all participating in some sort of
 * communication with each other.<br>
 * The channel is the main API class for Tribes, this is essentially the only class
 * that an application needs to be aware of. Through the channel the application can:<br>
 * 1. send messages<br>
 * 2. receive message (by registering a <code>ChannelListener</code><br>
 * 3. get all members of the group <code>getMembers()</code><br>
 * 4. receive notifications of members added and members disappeared by
 *    registerering a <code>MembershipListener</code><br>
 * <br>
 * The channel has 5 major components:<br>
 * 1. Data receiver, with a built in thread pool to receive messages from other peers<br>
 * 2. Data sender, an implementation for sending data using NIO or java.io<br>
 * 3. Membership listener,listens for membership broadcasts<br>
 * 4. Membership broadcaster, broadcasts membership pings.<br>
 * 5. Channel interceptors, the ability to manipulate messages as they are sent or arrive<br><br>
 * The channel layout is:
 * <pre><code>
 *  ChannelListener_1..ChannelListener_N MembershipListener_1..MembershipListener_N [Application Layer]
 *            \          \                  /                   /
 *             \          \                /                   /
 *              \          \              /                   /
 *               \          \            /                   /
 *                \          \          /                   /
 *                 \          \        /                   /
 *                  ---------------------------------------
 *                                  |
 *                                  |
 *                               Channel
 *                                  |
 *                         ChannelInterceptor_1
 *                                  |                                               [Channel stack]
 *                         ChannelInterceptor_N
 *                                  |
 *                             Coordinator (implements MessageListener,MembershipListener,ChannelInterceptor)
 *                          --------------------
 *                         /        |           \ 
 *                        /         |            \
 *                       /          |             \
 *                      /           |              \
 *                     /            |               \
 *           MembershipService ChannelSender ChannelReceiver                        [IO layer]
 * </code></pre>
 */


public interface Channel {

	/**
     * Start and stop sequences can be controlled by these constants
     * This allows you to start separate components of the channel <br>
     * DEFAULT - starts or stops all components in the channel
     * @see #start(int)
     * @see #stop(int)
     */
    public static final int DEFAULT = 15;
	
    /**
     * Start and stop sequences can be controlled by these constants
     * This allows you to start separate components of the channel <br>
     * SND_RX_SEQ - starts or stops the data receiver. Start means opening a server socket
     * in case of a TCP implementation
     * @see #start(int)
     * @see #stop(int)
     */
    public static final int SND_RX_SEQ = 1;
    
    /**
     * Start and stop sequences can be controlled by these constants
     * This allows you to start separate components of the channel <br>
     * SND_TX_SEQ - starts or stops the data sender. This should not open any sockets,
     * as sockets are opened on demand when a message is being sent
     * @see #start(int)
     * @see #stop(int)
     */
    public static final int SND_TX_SEQ = 2;
    
    
    /**
     * Send options, when a message is sent, it can have an option flag
     * to trigger certain behavior. Most flags are used to trigger channel interceptors
     * as the message passes through the channel stack. <br>
     * However, there are five default flags that every channel implementation must implement<br>
     * SEND_OPTIONS_BYTE_MESSAGE - The message is a pure byte message and no marshalling or unmarshalling will
     * be performed.<br>
     * 
     * @see #send(Member[], Serializable , int)
     * @see #send(Member[], Serializable, int, ErrorHandler)
     */
    public static final int SEND_OPTIONS_BYTE_MESSAGE = 0x0001;
    
    /**
     * Send options, when a message is sent, it can have an option flag
     * to trigger certain behavior. Most flags are used to trigger channel interceptors
     * as the message passes through the channel stack. <br>
     * However, there are five default flags that every channel implementation must implement<br>
     * SEND_OPTIONS_USE_ACK - Message is sent and an ACK is received when the message has been received by the recipient<br>
     * If no ack is received, the message is not considered successful<br>
     * @see #send(Member[], Serializable , int)
     * @see #send(Member[], Serializable, int, ErrorHandler)
     */
    public static final int SEND_OPTIONS_USE_ACK = 0x0002;
    
    /**
     * Send options, when a message is sent, it can have an option flag
     * to trigger certain behavior. Most flags are used to trigger channel interceptors
     * as the message passes through the channel stack. <br>
     * However, there are five default flags that every channel implementation must implement<br>
     * SEND_OPTIONS_SYNCHRONIZED_ACK - Message is sent and an ACK is received when the message has been received and 
     * processed by the recipient<br>
     * If no ack is received, the message is not considered successful<br>
     * @see #send(Member[], Serializable , int)
     * @see #send(Member[], Serializable, int, ErrorHandler)
     */
    public static final int SEND_OPTIONS_SYNCHRONIZED_ACK = 0x0004;
    
    
    /**
     * Send options, when a message is sent, it can have an option flag
     * to trigger certain behavior. Most flags are used to trigger channel interceptors
     * as the message passes through the channel stack. <br>
     * However, there are five default flags that every channel implementation must implement<br>
     * SEND_OPTIONS_ASYNCHRONOUS - Message is sent and an ACK is received when the message has been received and 
     * processed by the recipient<br>
     * If no ack is received, the message is not considered successful<br>
     * @see #send(Member[], Serializable , int)
     * @see #send(Member[], Serializable, int, ErrorHandler)
     */
    public static final int SEND_OPTIONS_ASYNCHRONOUS = 0x0008;
    
    
    /**
     * Adds an interceptor to the channel message chain.
     * @param interceptor ChannelInterceptor
     */
    public void addInterceptor(ChannelInterceptor interceptor);
    
    
    /**
     * Starts up the channel. This can be called multiple times for individual services to start
     * The svc parameter can be the logical or value of any constants
     * @param svc int value of <BR>
     * DEFAULT - will start all services <BR>
     * MBR_RX_SEQ - starts the membership receiver <BR>
     * MBR_TX_SEQ - starts the membership broadcaster <BR>
     * SND_TX_SEQ - starts the replication transmitter<BR>
     * SND_RX_SEQ - starts the replication receiver<BR>
     * <b>Note:</b> In order for the membership broadcaster to 
     * transmit the correct information, it has to be started after the replication receiver.
     * @throws ChannelException if a startup error occurs or the service is already started or an error occurs.
     */
    public void start(int svc) throws ChannelException;
    
	
	/**
     * Add a membership listener, will get notified when a new member joins, leaves or crashes
     * <br>If the membership listener implements the Heartbeat interface
     * the <code>heartbeat()</code> method will be invoked when the heartbeat runs on the channel
     * @param listener MembershipListener
     * @see MembershipListener
     */
    public void addMembershipListener(MembershipListener listener);
    
    /**
     * remove a membership listener, listeners are removed based on Object.hashCode and Object.equals
     * @param listener MembershipListener
     * @see MembershipListener
     */
    public void removeMembershipListener(MembershipListener listener);
    
    /**
     * Add a channel listener, this is a callback object when messages are received
     * <br>If the channel listener implements the Heartbeat interface
     * the <code>heartbeat()</code> method will be invoked when the heartbeat runs on the channel
     * @param listener ChannelListener
     * @see ChannelListener
     * @see Heartbeat
     */
    public void addChannelListener(ChannelListener listener);
    
    /**
     * remove a channel listener, listeners are removed based on Object.hashCode and Object.equals
     * @param listener ChannelListener
     * @see ChannelListener
     */
    public void removeChannelListener(ChannelListener listener);
}
