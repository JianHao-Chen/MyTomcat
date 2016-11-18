package My.catalina.tribes.group.interceptors;

import My.catalina.tribes.Channel;
import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;
import My.catalina.tribes.group.ChannelInterceptorBase;
import My.catalina.tribes.group.InterceptorPayload;
import My.catalina.tribes.transport.bio.util.FastQueue;
import My.catalina.tribes.transport.bio.util.LinkObject;

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
    protected boolean useDeepClone = true;
    
    
    public MessageDispatchInterceptor() {
        setOptionFlag(Channel.SEND_OPTIONS_ASYNCHRONOUS);
    }
    
    
    public void sendMessage(Member[] destination, ChannelMessage msg, InterceptorPayload payload) 
    	throws ChannelException {
    	
    	boolean async = (msg.getOptions() & Channel.SEND_OPTIONS_ASYNCHRONOUS) == Channel.SEND_OPTIONS_ASYNCHRONOUS;
    	
    	if ( async && run ) {
    		if ( (getCurrentSize()+msg.getMessage().getLength()) > maxQueueSize ) {
    			// implements later.
    		}
    		
    		//add to queue
    		if ( useDeepClone )
    			msg = (ChannelMessage)msg.deepclone();
    		
    		if (!addToQueue(msg, destination, payload) ) {
    			throw new ChannelException("Unable to add the message to the async queue, queue bug?");
    		}
    		addAndGetCurrentSize(msg.getMessage().getLength());
    	}
    	else {
            super.sendMessage(destination, msg, payload);
        }
    	
    }
    
    public boolean addToQueue(ChannelMessage msg, Member[] destination, InterceptorPayload payload) {
    //	return queue.add(msg,destination,payload);
    	return false;
    }
    
    
    
    public void startQueue() {
    	 msgDispatchThread = new Thread(this);
    }
    
    
    
    
    public void setOptionFlag(int flag) {
    	if ( flag != Channel.SEND_OPTIONS_ASYNCHRONOUS ) 
    		log.warn("Warning, you are overriding the asynchronous option flag, this will disable the Channel.SEND_OPTIONS_ASYNCHRONOUS that other apps might use.");
        super.setOptionFlag(flag);
    }
    
    
    public long getCurrentSize() {
    	return currentSize;
    }
    
    public synchronized long addAndGetCurrentSize(long inc) {
        currentSize += inc;
        return currentSize;
    }
    
    public synchronized long setAndGetCurrentSize(long value) {
        currentSize = value;
        return value;
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
    
    
    
    protected LinkObject sendAsyncData(LinkObject link) {
    	
    	ChannelMessage msg = link.data();
    	Member[] destination = link.getDestination();
    	
    	try {
    		super.sendMessage(destination,msg,null);
    		//....
    		
    	}
    	catch ( Exception x ) {
    		ChannelException cx = null;
    		if ( x instanceof ChannelException ) 
    			cx = (ChannelException)x;
    		else 
    			cx = new ChannelException(x);
    		
    		try {
    			if (link.getHandler() != null) 
    				;//	link.getHandler().handleError(cx, new UniqueId(msg.getUniqueId()));	
    		}
    		catch ( Exception ex ) {
                log.error("Unable to report back error message.",ex);
            }
    		
    	}
    	finally {
    		addAndGetCurrentSize(-msg.getMessage().getLength());
    		link = link.next();
    	}
    	
    	return link;
    }
    
    
    
}
