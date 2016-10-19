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

}
