package My.catalina.tribes;

/**
 * Can be implemented by the ChannelListener and Membership listeners to receive heartbeat
 * notifications from the Channel
 */

public interface Heartbeat {
	/**
     * Heartbeat invokation for resources cleanup etc
     */
    public void heartbeat();
}
