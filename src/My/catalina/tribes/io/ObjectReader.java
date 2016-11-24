package My.catalina.tribes.io;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import My.catalina.tribes.ChannelMessage;

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
    
    
    public synchronized void access() {
        this.accessed = true;
        this.lastAccess = System.currentTimeMillis();
    }
    
    public synchronized void finish() {
        this.accessed = false;
        this.lastAccess = System.currentTimeMillis();
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
    
    
    
    /**
     * Append new bytes to buffer. 
     */
    public int append(ByteBuffer data, int len, boolean count) throws java.io.IOException {
    	buffer.append(data,len);
    	int pkgCnt = -1;
        if ( count ) 
        	pkgCnt = buffer.countPackages();
        return pkgCnt;
    }
    
    /**
     * Send buffer to cluster listener (callback).
     * Is message complete receiver send message to callback?
     *
     * @see org.apache.catalina.tribes.transport.ClusterReceiverBase#messageDataReceived(ChannelMessage)
     * @see XByteBuffer#doesPackageExist()
     * @see XByteBuffer#extractPackage(boolean)
     *
     * @return number of received packages/messages
     * @throws java.io.IOException
     */
    public ChannelMessage[] execute() throws java.io.IOException {
    	int pkgCnt = buffer.countPackages();
    	ChannelMessage[] result = new ChannelMessage[pkgCnt];
    	for (int i=0; i<pkgCnt; i++)  {
    		ChannelMessage data = buffer.extractPackage(true);
    		result[i] = data;
    	}
    	return result;
    }
    
    
    
    
    public boolean hasPackage() {
        return buffer.countPackages(true)>0;
    }
    
    public int count() {
        return buffer.countPackages();
    }
}
