package My.catalina.tribes.transport.nio;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import My.catalina.tribes.io.XByteBuffer;
import My.catalina.tribes.transport.AbstractSender;
import My.catalina.tribes.transport.DataSender;

public class NioSender extends AbstractSender implements DataSender{

	protected static My.juli.logging.Log log = 
		My.juli.logging.LogFactory.getLog(NioSender.class);
	
	protected Selector selector;    
    protected SocketChannel socketChannel;
    
    /*
     * STATE VARIABLES *
     */
    protected ByteBuffer readbuf = null;
    protected ByteBuffer writebuf = null;
    protected byte[] current = null;
    protected XByteBuffer ackbuf = new XByteBuffer(128,true);
    protected int remaining = 0;
    protected boolean complete;
    
    protected boolean connecting = false;
    
    
    public NioSender() {
        super();
    }
    
    
    
    /**
     * State machine to send data
     */
    public boolean process(SelectionKey key, boolean waitForAck) throws IOException {
    	int ops = key.readyOps();
    	key.interestOps(key.interestOps() & ~ops);
    	
    	//in case disconnect has been called
    	if ((!isConnected()) && (!connecting)) 
    		throw new IOException("Sender has been disconnected, can't selection key.");
    	
    	if ( !key.isValid() ) 
    		throw new IOException("Key is not valid, it must have been cancelled.");
    	
    	
    	if ( key.isConnectable() ) {
    		if ( socketChannel.finishConnect() ) {
    			completeConnect();
    			if ( current != null ) 
    				key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    			return false;
    		}
    		else  { 
    			//...
    		}
    	}
    	else if ( key.isWritable() ) {
    		boolean writecomplete = write(key);
    		if ( writecomplete ) {
    			//we are completed, should we read an ack?
                if ( waitForAck ) {
                	//register to read the ack
                    key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                }
                else {
                	//if not, we are ready, setMessage will reregister us for another write interest
                    //do a health check, we have no way of verify a disconnected
                    //socket since we don't register for OP_READ on waitForAck=false
                	read(key);//this causes overhead
                	setRequestCount(getRequestCount()+1);
                	return true;
                }
    		}
    	}
    	
    	return false;
    }
    
    private void completeConnect() throws SocketException {
    	//we connected, register ourselves for writing
        setConnected(true);
        connecting = false;
        setRequestCount(0);
        setConnectTime(System.currentTimeMillis());
        
        socketChannel.socket().setSendBufferSize(getTxBufSize());
        socketChannel.socket().setReceiveBufferSize(getRxBufSize());
        socketChannel.socket().setSoTimeout((int)getTimeout());
        socketChannel.socket().setSoLinger(getSoLingerOn(),getSoLingerOn()?getSoLingerTime():0);
        socketChannel.socket().setTcpNoDelay(getTcpNoDelay());
        socketChannel.socket().setKeepAlive(getSoKeepAlive());
        socketChannel.socket().setReuseAddress(getSoReuseAddress());
        socketChannel.socket().setOOBInline(getOoBInline());
        socketChannel.socket().setSoLinger(getSoLingerOn(),getSoLingerTime());
        socketChannel.socket().setTrafficClass(getSoTrafficClass());
        
    }
    
    
    protected boolean read(SelectionKey key) throws IOException {
    	//if there is no message here, we are done
        if ( current == null ) 
        	return true;
        
        int read = socketChannel.read(readbuf);
        
        if ( read == -1 ) 
        	throw new IOException("Unable to receive an ack message. EOF on socket channel has been reached.");
        else if ( read == 0 ) //no data read
        	return false;
        
        //...
        
        return false;
    }
    
    
    protected boolean write(SelectionKey key) throws IOException {
    	if ( (!isConnected()) || (this.socketChannel==null)) {
            throw new IOException("NioSender is not connected, this should not occur.");
        }
    	
    	if ( current != null ) {
    		if ( remaining > 0 ) {
    			int byteswritten = socketChannel.write(writebuf);
    			if (byteswritten == -1 ) 
    				throw new EOFException();
    			
    			remaining -= byteswritten;
    			
    			//if the entire message was written from the buffer
                //reset the position counter
                if ( remaining < 0 ) {
                    remaining = 0;
                }
    		}
    		return (remaining==0);
    	}
    	//no message to send, we can consider that complete
        return true;
    }
    
    
    
    /**
     * connect - blocking in this operation
     */
    public synchronized void connect() throws IOException {
    	if ( connecting ) return;
    	connecting = true;
    	if ( isConnected() ) 
    		throw new IOException("NioSender is already in connected state.");
    	
    	if ( readbuf == null ) {
    		readbuf = getReadBuffer();
    	}
    	else {
            readbuf.clear();
        }
    	
    	if ( writebuf == null ) {
            writebuf = getWriteBuffer();
        } else {
            writebuf.clear();
        }
    	
    	InetSocketAddress addr = 
    		new InetSocketAddress(getAddress(),getPort());
    	
    	if ( socketChannel != null ) 
    		throw new IOException("Socket channel has already been established. Connection might be in progress.");
    	
    	socketChannel = SocketChannel.open();
    	socketChannel.configureBlocking(false);
    	
    	if ( socketChannel.connect(addr) ) {
    	    completeConnect();
            socketChannel.register(getSelector(), SelectionKey.OP_WRITE, this);
    	}
    	else{
    		socketChannel.register(getSelector(), SelectionKey.OP_CONNECT, this);
    	}
    }
    
    
    public void disconnect() {
    	
    	try {
    		connecting = false;
    		setConnected(false);
    		if ( socketChannel != null ) {
    			try {
    				try {socketChannel.socket().close();}catch ( Exception x){}
    				
    				try {socketChannel.close();}catch ( Exception x){}
    			}
    			finally {
                    socketChannel = null;
                }
    		}
    	}
    	catch ( Exception x ) {
    		log.error("Unable to disconnect NioSender. msg="+x.getMessage());
    	}
    	
    }
    

    public void reset() {
    	if ( isConnected() && readbuf == null) {
    		readbuf = getReadBuffer();
    	}
    	
    	if ( readbuf != null ) 
    		readbuf.clear();
    	if ( writebuf != null ) 
    		writebuf.clear();
    	
    	current = null;
        ackbuf.clear();
        remaining = 0;
        complete = false;
        
        setAttempt(0);
        setRequestCount(0);
        setConnectTime(-1);
    }
    
    private ByteBuffer getReadBuffer() { 
        return getBuffer(getRxBufSize());
    }
    
    private ByteBuffer getWriteBuffer() {
        return getBuffer(getTxBufSize());
    }

    private ByteBuffer getBuffer(int size) {
        return (getDirectBuffer()?ByteBuffer.allocateDirect(size):ByteBuffer.allocate(size));
    }
    
    
    /**
     * sendMessage
     */
    public synchronized void setMessage(byte[] data) throws IOException {
    	setMessage(data,0,data.length);
    }
    
    public synchronized void setMessage(byte[] data,int offset, int length) throws IOException {
    	if ( data != null ) {
    		current = data;
            remaining = length;
            ackbuf.clear();
            
            if ( writebuf != null ) 
            	writebuf.clear();
            else 
            	writebuf = getBuffer(length);
            
            if ( writebuf.capacity() < length ) 
            	writebuf = getBuffer(length);
            
            writebuf.put(data,offset,length);
            writebuf.flip();
            
            if (isConnected()) {
            	socketChannel.register(getSelector(), SelectionKey.OP_WRITE, this);
            }
    	}
    }
    
    
    public Selector getSelector() {
        return selector;
    }
    
    public void setSelector(Selector selector) {
        this.selector = selector;
    }
    
    
    public boolean isComplete() {
        return complete;
    }
    public void setComplete(boolean complete) {
        this.complete = complete;
    }
	
}
