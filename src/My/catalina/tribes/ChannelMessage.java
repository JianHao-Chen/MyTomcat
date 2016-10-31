package My.catalina.tribes;

import java.io.Serializable;

/**
 * Message that is passed through the interceptor stack after the 
 * data serialized in the Channel object and then passed down to the 
 * interceptor and eventually down to the ChannelSender component
 */

public interface ChannelMessage extends Serializable{

}
