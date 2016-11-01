package My.catalina.tribes;

/**
 * MembershipService Interface<br>
 * The <code>MembershipService</code> interface is the membership component 
 * at the bottom layer, the IO layer (for layers see the javadoc for the {@link Channel} interface).<br>
 */

public interface MembershipService {

	/**
     * Sets the local member properties for broadcasting
     */
    public void setLocalMemberProperties(String listenHost, int listenPort);
}
