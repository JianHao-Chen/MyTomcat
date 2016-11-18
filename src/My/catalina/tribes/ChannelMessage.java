package My.catalina.tribes;

import java.io.Serializable;

/**
 * Message that is passed through the interceptor stack after the 
 * data serialized in the Channel object and then passed down to the 
 * interceptor and eventually down to the ChannelSender component
 */

public interface ChannelMessage extends Serializable{

	
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
}
