package My.catalina.tribes;

/**
 * ChannelReceiver Interface<br>
 * The <code>ChannelReceiver</code> interface is the data receiver component 
 * at the bottom layer, the IO layer (for layers see the javadoc for the {@link Channel} interface).
 * This class may optionally implement a thread pool for parallel processing of incoming messages.
 */

public interface ChannelReceiver extends Heartbeat{

	/**
     * Start listening for incoming messages on the host/port
     * @throws java.io.IOException
     */
    public void start() throws java.io.IOException;

    /**
     * Stop listening for messages
     */
    public void stop();
    
    
    /**
     * String representation of the IPv4 or IPv6 address that this host is listening
     * to.
     * @return the host that this receiver is listening to
     */
    public String getHost();
    
    
    /**
     * Returns the listening port
     * @return port
     */
    public int getPort();
    
	
	/**
     * Sets the message listener to receive notification of incoming
     * @param listener MessageListener
     * @see MessageListener
     */
    public void setMessageListener(MessageListener listener);
    
    /**
     * Returns the message listener that is associated with this receiver
     * @return MessageListener
     * @see MessageListener
     */
    public MessageListener getMessageListener();
}
