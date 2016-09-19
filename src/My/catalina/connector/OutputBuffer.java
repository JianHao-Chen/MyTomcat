package My.catalina.connector;

import java.io.IOException;
import java.io.Writer;
import My.tomcat.util.buf.ByteChunk;

public class OutputBuffer extends Writer
	implements ByteChunk.ByteOutputChannel{

	// ------------------------ Constants------------------------ 
	
	public static final int DEFAULT_BUFFER_SIZE = 8*1024;
	
	
	
	// --------------------- Constructors---------------------
	/**
     * Default constructor. Allocate the buffer with the default buffer size.
     */
    public OutputBuffer() {

        this(DEFAULT_BUFFER_SIZE);

    }
    
    /**
     * Alternate constructor which allows specifying the initial buffer size.
     * 
     * @param size Buffer size to use
     */
    public OutputBuffer(int size) {
    	bb = new ByteChunk(size);
        bb.setLimit(size);
        bb.setByteOutputChannel(this);
    }
	
    
    
	// ------------------------ Instance Variables---------------------- 
	
    /**
     * The byte buffer.
     */
    private ByteChunk bb;


    /**
     * State of the output buffer.
     */
    private boolean initial = true;


    /**
     * Number of bytes written.
     */
    private long bytesWritten = 0;


    /**
     * Number of chars written.
     */
    private long charsWritten = 0;


    /**
     * Flag which indicates if the output buffer is closed.
     */
    private boolean closed = false;
    
    
    /**
     * Associated Coyote response.
     */
    private My.coyote.Response coyoteResponse;
    
    /**
     * Suspended flag. All output bytes will be swallowed if this is true.
     */
    private boolean suspended = false;
    
    
	// ----------------------------- Properties-----------------------------
	
    /**
     * Associated Coyote response.
     * 
     * @param coyoteResponse Associated Coyote response
     */
    public void setResponse(My.coyote.Response coyoteResponse) {
	this.coyoteResponse = coyoteResponse;
    }


    /**
     * Get associated Coyote response.
     * 
     * @return the associated Coyote response
     */
    public My.coyote.Response getResponse() {
        return this.coyoteResponse;
    }


    /**
     * Is the response output suspended ?
     * 
     * @return suspended flag value
     */
    public boolean isSuspended() {
        return this.suspended;
    }


    /**
     * Set the suspended flag.
     * 
     * @param suspended New suspended flag value
     */
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }


    /**
     * Is the response output closed ?
     * 
     * @return closed flag value
     */
    public boolean isClosed() {
        return this.closed;
    }
    
	// ---------------------- Public Methods----------------------
    
    
    
	 
	
	
	
	
	
	
	// ---------------- Bytes Handling Methods----------------
	
    public void writeByte(int b) throws IOException {
		 if (suspended)
			 return;
		 bb.append((byte) b);
		 bytesWritten++;
	 }
	
	
	@Override
	public void realWriteBytes(byte[] cbuf, int off, int len)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	
	
}
