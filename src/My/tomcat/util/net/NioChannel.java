package My.tomcat.util.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import My.tomcat.util.net.NioEndpoint.Poller;
import My.tomcat.util.net.SecureNioChannel.ApplicationBufferHandler;

/**
 * Base class for a SocketChannel wrapper used by the endpoint.
 */

public class NioChannel implements ByteChannel{

	protected static ByteBuffer emptyBuf = ByteBuffer.allocate(0);
	
	protected SocketChannel sc = null;
	
	protected ApplicationBufferHandler bufHandler;
	
	protected Poller poller;
    
	
	// ------------------------- constructor --------------------------
	
	public NioChannel(SocketChannel channel, ApplicationBufferHandler bufHandler) throws IOException {
        this.sc = channel;
        this.bufHandler = bufHandler;
    }
	
	
	// -------------------------- properties --------------------------
	
	 public void reset() throws IOException {
		 bufHandler.getReadBuffer().clear();
		 bufHandler.getWriteBuffer().clear();
	 }
	 
	 public int getBufferSize() {
		 if ( bufHandler == null ) return 0;
	        int size = 0;
	        size += bufHandler.getReadBuffer()!=null?bufHandler.getReadBuffer().capacity():0;
	        size += bufHandler.getWriteBuffer()!=null?bufHandler.getWriteBuffer().capacity():0;
	        return size;
	 }
	 
	 
	 
	 
	 public void setPoller(Poller poller) {
	        this.poller = poller;
	 }
	 
	 
	 /**
	     * getBufHandler
	     *
	     * @return ApplicationBufferHandler
	     * @todo Implement this org.apache.tomcat.util.net.SecureNioChannel method
	     */
	public ApplicationBufferHandler getBufHandler() {
	    return bufHandler;
	}
	
	 
	 
	 /**
	     * getIOChannel
	     *
	     * @return SocketChannel
	     */
	public SocketChannel getIOChannel() {
		return sc;
	}
	
	public Poller getPoller() {
        return poller;
    }
	
	public Object getAttachment(boolean remove) {
		Poller pol = getPoller();
		Selector selector = pol==null?null:pol.getSelector();
		SelectionKey key = selector==null?null:getIOChannel().keyFor(selector);
		Object o = key==null?null:key.attachment();
		if(key!=null && o!=null && remove) key.attach(null);
		return o;
	}
	
	@Override
	public int read(ByteBuffer dst) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
