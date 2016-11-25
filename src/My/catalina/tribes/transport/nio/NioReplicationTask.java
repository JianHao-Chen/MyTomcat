package My.catalina.tribes.transport.nio;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.io.ChannelData;
import My.catalina.tribes.io.ListenCallback;
import My.catalina.tribes.io.ObjectReader;
import My.catalina.tribes.transport.AbstractRxTask;
import My.catalina.tribes.transport.Constants;


public class NioReplicationTask extends AbstractRxTask{
	
	private static My.juli.logging.Log log = My.juli.logging.LogFactory.getLog( NioReplicationTask.class );

	private ByteBuffer buffer = null;
    private SelectionKey key;
    private int rxBufSize;
    private NioReceiver receiver;
    
    public NioReplicationTask (ListenCallback callback, NioReceiver receiver){
    	super(callback);
        this.receiver = receiver;
    }
    
    
    
    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = rxBufSize;
    }

    public int getRxBufSize() {
        return rxBufSize;
    }
    
    
    // loop forever waiting for work to do
    public synchronized void run() {
    	if ( buffer == null ) {
    		if ( (getOptions() & OPTION_DIRECT_BUFFER) == OPTION_DIRECT_BUFFER) {
    			buffer = ByteBuffer.allocateDirect(getRxBufSize());
    		}
    		else {
                buffer = ByteBuffer.allocate(getRxBufSize());
            }
    	}
    	else {
            buffer.clear();
        }
    	
    	
    	if (key == null) {
            return;	// just in case
        }
    	
    	try {
    		ObjectReader reader = (ObjectReader)key.attachment();
    		if ( reader == null ) {
    			//..
    		}
    		else
    			drainChannel(key, reader);
    		
    	}
    	catch (Exception e) {
    		
    	}
    	
    }
    
    
    /**
     * Called to initiate a unit of work by this worker thread
     * on the provided SelectionKey object.  This method is
     * synchronized, as is the run() method, so only one key
     * can be serviced at a given time.
     * Before waking the worker thread, and before returning
     * to the main selection loop, this key's interest set is
     * updated to remove OP_READ.  This will cause the selector
     * to ignore read-readiness for this channel while the
     * worker thread is servicing it.
     */
    public synchronized void serviceChannel (SelectionKey key) {
    	
    	ObjectReader reader = (ObjectReader)key.attachment();
    	if ( reader != null ) 
    		reader.setLastAccess(System.currentTimeMillis());
    	
    	this.key = key;
    	key.interestOps (key.interestOps() & (~SelectionKey.OP_READ));
        key.interestOps (key.interestOps() & (~SelectionKey.OP_WRITE));
    	
    }
    
    
    /**
     * The actual code which drains the channel associated with
     * the given key.  This method assumes the key has been
     * modified prior to invocation to turn off selection
     * interest in OP_READ.  When this method completes it
     * re-enables OP_READ and calls wakeup() on the selector
     * so the selector will resume watching this channel.
     */
    protected void drainChannel (final SelectionKey key, ObjectReader reader) throws Exception {
    	reader.setLastAccess(System.currentTimeMillis());
    	reader.access();
    	
    	SocketChannel channel = (SocketChannel) key.channel();
    	int count;
        buffer.clear();			// make buffer empty
        
        // loop while data available, channel is non-blocking
        while ((count = channel.read (buffer)) > 0) {
        	buffer.flip();		// make buffer readable
        	reader.append(buffer,count,false);
        	buffer.clear();		// make buffer empty
        	
        	//do we have at least one package?
            if ( reader.hasPackage() )
            	break;
        }
        
        int pkgcnt = reader.count();
        
        if (count < 0 && pkgcnt == 0 ) {
        	//end of stream, and no more packages to process
        	//...
        }
        
        ChannelMessage[] msgs = pkgcnt == 0? ChannelData.EMPTY_DATA_ARRAY : reader.execute();
    	
        //register to read new data, before we send it off to avoid dead locks
        registerForRead(key,reader);
        
        for ( int i=0; i<msgs.length; i++ ) {
        	/**
             * Use send ack here if you want to ack the request to the remote 
             * server before completing the request
             * This is considered an asynchronized request
             */
        	if (ChannelData.sendAckAsync(msgs[i].getOptions())) 
        		sendAck(key,channel,Constants.ACK_COMMAND);
        	
        	try {
        		
        		//process the message.
                getCallback().messageDataReceived(msgs[i]);
        	}
        }
        
    }
    
    
    /**
     * send a reply-acknowledgement
     */
    protected void sendAck(SelectionKey key, SocketChannel channel, byte[] command) {
    	//...
    }
    
    
    protected void registerForRead(final SelectionKey key, ObjectReader reader) {
    	reader.finish();
    	
    	//register our OP_READ interest
        Runnable r = new Runnable() {
        	public void run() {
        		try {
        			if (key.isValid()) {
        				
        			}
        		}
        	}
        };
    	
        receiver.addEvent(r);
    }
    
    
}
