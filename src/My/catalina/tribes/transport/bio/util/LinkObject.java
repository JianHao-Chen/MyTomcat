package My.catalina.tribes.transport.bio.util;

import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.ErrorHandler;
import My.catalina.tribes.Member;
import My.catalina.tribes.group.InterceptorPayload;

/**
 * The class <b>LinkObject</b> implements an element
 * for a linked list, consisting of a general
 * data object and a pointer to the next element.
 */

public class LinkObject {

	private ChannelMessage msg;
    private LinkObject next;
    private byte[] key ;
    private Member[] destination;
    private InterceptorPayload payload;
    
    
    /**
     * Construct a new element from the data object.
     * Sets the pointer to null.
     *
     * @param key The key
     * @param payload The data object.
     */
    public LinkObject(ChannelMessage msg, Member[] destination, InterceptorPayload payload) {
        this.msg = msg;
        this.next = null;
        this.key = msg.getUniqueId();
        this.payload = payload;
        this.destination = destination;
    }
    
    /**
     * Set the next element.
     * @param next The next element.
     */
    public void append(LinkObject next) {
        this.next = next;
    }

    /**
     * Get the next element.
     * @return The next element.
     */
    public LinkObject next() {
        return next;
    }
    
    public void setNext(LinkObject next) {
        this.next = next;
    }

    /**
     * Get the data object from the element.
     * @return The data object from the element.
     */
    public ChannelMessage data() {
        return msg;
    }

    /**
     * Get the unique message id
     * @return the unique message id
     */
    public byte[] getKey() {
        return key;
    }

    public ErrorHandler getHandler() {
        return payload!=null?payload.getErrorHandler():null;
    }

    public InterceptorPayload getPayload() {
        return payload;
    }

    public Member[] getDestination() {
        return destination;
    }
    
}
