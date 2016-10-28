package My.catalina.tribes;

/**
 * ChannelReceiver Interface<br>
 * The <code>ChannelReceiver</code> interface is the data receiver component 
 * at the bottom layer, the IO layer (for layers see the javadoc for the {@link Channel} interface).
 * This class may optionally implement a thread pool for parallel processing of incoming messages.
 */

public interface ChannelReceiver extends Heartbeat{

}
