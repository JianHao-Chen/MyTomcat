package My.catalina.tribes.group.interceptors;

import My.catalina.tribes.Channel;
import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;
import My.catalina.tribes.group.ChannelInterceptorBase;
import My.catalina.tribes.group.InterceptorPayload;
import My.catalina.tribes.transport.bio.util.FastQueue;

/**
*
* The message dispatcher is a way to enable asynchronous communication
* through a channel. The dispatcher will look for the <code>Channel.SEND_OPTIONS_ASYNCHRONOUS</code>
* flag to be set, if it is, it will queue the message for delivery and immediately return to the sender.
* 
*/

public class MessageDispatchInterceptor 
	extends ChannelInterceptorBase implements Runnable{

	protected static My.juli.logging.Log log = My.juli.logging.LogFactory.getLog(MessageDispatchInterceptor.class);

	protected long maxQueueSize = 1024*1024*64; //64MB
	
	protected FastQueue queue = new FastQueue();
	
	protected boolean run = false;
    protected Thread msgDispatchThread = null;
    protected long currentSize = 0;
    
    
    public MessageDispatchInterceptor() {
        setOptionFlag(Channel.SEND_OPTIONS_ASYNCHRONOUS);
    }
    
    
    public void sendMessage(Member[] destination, ChannelMessage msg, InterceptorPayload payload) 
    	throws ChannelException {
    	boolean async = (msg.getOptions() & Channel.SEND_OPTIONS_ASYNCHRONOUS) == Channel.SEND_OPTIONS_ASYNCHRONOUS;
    	
    	if ( async && run ) {
    		
    	}
    	
    }
    
    
    
    public void startQueue() {
    	 msgDispatchThread = new Thread(this);
    }
    
    
    
    
    public void setOptionFlag(int flag) {
    	if ( flag != Channel.SEND_OPTIONS_ASYNCHRONOUS ) 
    		log.warn("Warning, you are overriding the asynchronous option flag, this will disable the Channel.SEND_OPTIONS_ASYNCHRONOUS that other apps might use.");
        super.setOptionFlag(flag);
    }
    
    
    
    
    public void start(int svc) throws ChannelException {
    	//start the thread
        if (!run ) {
        	synchronized (this) {
        		//only start with the sender
        		if ( !run && ((svc & Channel.SND_TX_SEQ)==Channel.SND_TX_SEQ) ) {
        			startQueue();
        		}
        	}
        }
        super.start(svc);
    }
    
    
    
    
    public void run() {
    	
    }
    
    
    
}
