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
	
	
	/**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * @param dst The buffer into which bytes are to be transferred
     * @return The number of bytes read, possibly zero, 
     * 			or <tt>-1</tt> if the channel has reached end-of-stream
     * @throws IOException If some other I/O error occurs
     * @todo Implement this java.nio.channels.ReadableByteChannel method
     */
	@Override
	public int read(ByteBuffer dst) throws IOException {
		return sc.read(dst);
	}

	
	 /**
     * Tells whether or not this channel is open.
     *
     * @return <tt>true</tt> if, and only if, this channel is open
     * @todo Implement this java.nio.channels.Channel method
     */
	@Override
	public boolean isOpen() {
		return sc.isOpen();
	}

	
	/**
     * Closes this channel.
     *
     * @throws IOException If an I/O error occurs
     * @todo Implement this java.nio.channels.Channel method
     */
	@Override
	public void close() throws IOException {
		sc.socket().close();
		sc.close();
	}

	/**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * @param src The buffer from which bytes are to be retrieved
     * @return The number of bytes written, possibly zero
     * @throws IOException If some other I/O error occurs
     * @todo Implement this java.nio.channels.WritableByteChannel method
     */
	@Override
	public int write(ByteBuffer src) throws IOException {
		return sc.write(src);
	}

}
