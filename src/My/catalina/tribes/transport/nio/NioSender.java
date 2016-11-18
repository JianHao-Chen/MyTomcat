package My.catalina.tribes.transport.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
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
    		//...
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
