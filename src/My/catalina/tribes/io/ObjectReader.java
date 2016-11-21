package My.catalina.tribes.io;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * The object reader object is an object used in conjunction with
 * java.nio TCP messages. This object stores the message bytes in a
 * <code>XByteBuffer</code> until a full package has been received.
 * This object uses an XByteBuffer which is an extendable object buffer that also allows
 * for message encoding and decoding.
 */

public class ObjectReader {

	protected static My.juli.logging.Log log = My.juli.logging.LogFactory.getLog(ObjectReader.class);
	
	private XByteBuffer buffer;
    
    protected long lastAccess = System.currentTimeMillis();
    
    protected boolean accessed = false;
    private boolean cancelled;
    
    /**
     * Creates an <code>ObjectReader</code> for a TCP NIO socket channel
     * @param channel - the channel to be read.
     */
    public ObjectReader(SocketChannel channel) {
        this(channel.socket());
    }
    
    /**
     * Creates an <code>ObjectReader</code> for a TCP socket
     * @param socket Socket
     */
    public ObjectReader(Socket socket) {
        try{
            this.buffer = new XByteBuffer(socket.getReceiveBufferSize(), true);
        }catch ( IOException x ) {
            //unable to get buffer size
            log.warn("Unable to retrieve the socket receiver buffer size, setting to default 43800 bytes.");
            this.buffer = new XByteBuffer(43800,true);
        }
    }
    
    
    
    
    public int bufferSize() {
        return buffer.getLength();
    }
    
    public void close() {
        this.buffer = null;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    
}
