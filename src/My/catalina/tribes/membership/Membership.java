package My.catalina.tribes.membership;

/**
 * A <b>membership</b> implementation using simple multicast.
 * This is the representation of a multicast membership.
 * This class is responsible for maintaining a list of active cluster nodes in the cluster.
 * If a node fails to send out a heartbeat, the node will be dismissed.
 */

public class Membership {

	protected static final MemberImpl[] EMPTY_MEMBERS = new MemberImpl[0];
	
	private final Object membersLock = new Object();
	
	/**
     * The name of this membership, has to be the same as the name for the local
     * member
     */
    protected MemberImpl local;
}
