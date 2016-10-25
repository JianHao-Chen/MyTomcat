package My.catalina.tribes;

/**
 * The Member interface, defines a member in the group.
 * Each member can carry a set of properties, defined by the actual implementation.<BR>
 * A member is identified by the host/ip/uniqueId<br>
 * The host is what interface the member is listening to, to receive data<br>
 * The port is what port the member is listening to, to receive data<br>
 * The uniqueId defines the session id for the member. This is an important feature
 * since a member that has crashed and the starts up again on the same port/host is 
 * not guaranteed to be the same member, so no state transfers will ever be confused
 */
 
public interface Member {

	 /**
     * When a member leaves the cluster, the payload of the memberDisappeared member
     * will be the following bytes. This indicates a soft shutdown, and not a crash
     */
    public static final byte[] SHUTDOWN_PAYLOAD = new byte[] {66, 65, 66, 89, 45, 65, 76, 69, 88};
    
    /**
     * Returns the name of this node, should be unique within the group.
     */
    public String getName();
}
