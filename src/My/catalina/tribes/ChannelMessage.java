package My.catalina.tribes;

import java.io.Serializable;

import My.catalina.tribes.io.XByteBuffer;

/**
 * Message that is passed through the interceptor stack after the 
 * data serialized in the Channel object and then passed down to the 
 * interceptor and eventually down to the ChannelSender component
 */

public interface ChannelMessage extends Serializable{

	/**
     * Get the address that this message originated from.  
     * Almost always <code>Channel.getLocalMember(boolean)</code><br>
     * This would be set to a different address 
     * if the message was being relayed from a host other than the one
     * that originally sent it.
     * @return the source or reply-to address of this message
     */
    public Member getAddress();

    /**
     * Sets the source or reply-to address of this message
     * @param member Member
     */
    public void setAddress(Member member);
	
	
	
	/**
     * Each message must have a globally unique Id.
     * interceptors heavily depend on this id for message processing
     * @return byte
     */
    public byte[] getUniqueId();
    
    
    /**
     * The message options is a 32 bit flag set
     * that triggers interceptors and message behavior.
     * @see Channel#send(Member[], Serializable, int) 
     * @see ChannelInterceptor#getOptionFlag
     * @return int - the option bits set for this message
     */
    public int getOptions();
    
    /**
     * sets the option bits for this message
     * @param options int
     * @see #getOptions()
     */
    public void setOptions(int options);
    
    
    /**
     * The byte buffer that contains the actual message payload
     * @param buf XByteBuffer
     */
    public void setMessage(XByteBuffer buf);
    
    /**
     * returns the byte buffer that contains the actual message payload
     * @return XByteBuffer
     */
    public XByteBuffer getMessage();
    
    
    /**
     * Shallow clone, what gets cloned depends on the implementation
     * @return ChannelMessage
     */
    public Object clone();

    /**
     * Deep clone, all fields MUST get cloned
     * @return ChannelMessage
     */
    public Object deepclone();
}
