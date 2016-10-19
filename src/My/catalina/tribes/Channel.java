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

}
