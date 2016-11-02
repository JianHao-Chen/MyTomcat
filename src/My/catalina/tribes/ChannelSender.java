package My.catalina.tribes;

/** 
 * The <code>ChannelSender</code> interface is the data sender component 
* at the bottom layer, the IO layer (for layers see the javadoc for the {@link Channel} interface).<br>
* The channel sender must support "silent" members, ie, be able to send a message to a member
* that is not in the membership, but is part of the destination parameter
*/

public interface ChannelSender extends Heartbeat{

	
	/**
     * Start the channel sender
     * @throws IOException if preprocessing takes place and an error happens
     */
    public void start() throws java.io.IOException;
    
    
}
