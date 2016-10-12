package My.catalina.connector;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.C2BConverter;

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
     * Do a flush on the next operation.
     */
    private boolean doFlush = false;
    
    
    
    /**
     * Encoding to use.
     */
    private String enc;
    
    
    /**
     * Encoder is set.
     */
    private boolean gotEnc = false;
    
    
    /**
     * List of encoders.
     */
    protected HashMap encoders = new HashMap();
    
    
    /**
     * Current char to byte converter.
     */
    protected C2BConverter conv;
    
    
    /**
     * Associated Coyote response.
     */
    private My.coyote.Response coyoteResponse;
    
    /**
     * Suspended flag. All output bytes will be swallowed if this is true.
     */
    private boolean suspended = false;
    
    
    /**
     * Byte chunk used to output bytes.
     */
    private ByteChunk outputChunk = new ByteChunk();
    
    
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
    
    
    /**
     * Recycle the output buffer.
     */
    public void recycle() {
    	initial = true;
    	bytesWritten = 0;
        charsWritten = 0;
        
        bb.recycle(); 
        
        closed = false;
        doFlush = false;
        suspended = false;
    }
	 
	
	
	
	
	
	
	// ---------------- Bytes Handling Methods----------------
	
    public void writeByte(int b) throws IOException {
		 if (suspended)
			 return;
		 bb.append((byte) b);
		 bytesWritten++;
	 }
    
    
    
    
    public int getContentWritten() {
        long size = bytesWritten + charsWritten ;
        if (size < Integer.MAX_VALUE) {
            return (int) size;
        }
        return -1;
    }
    
    
    
    /** 
     * True if this buffer hasn't been used ( since recycle() ) -
     * i.e. no chars or bytes have been added to the buffer.  
     */
    public boolean isNew() {
        return (bytesWritten == 0) && (charsWritten == 0);
    }
    
    
    public void setBufferSize(int size) {
        if (size > bb.getLimit()) {// ??????
            bb.setLimit(size);
        }
    }
    
	
    /** 
     * Sends the buffer data to the client output, checking the
     * state of Response and calling the right interceptors.
     * 
     * @param buf Byte buffer to be written to the response
     * @param off Offset
     * @param cnt Length
     * 
     * @throws IOException An underlying IOException occurred
     */
	public void realWriteBytes(byte[] buf, int off, int cnt)
			throws IOException {
		if (closed)
            return;
        if (coyoteResponse == null)
            return;
        
        
        // If we really have something to write
        if (cnt > 0) {
        	
        	// real write to the adapter
        	outputChunk.setBytes(buf, off, cnt);
        	try {
        		coyoteResponse.doWrite(outputChunk);
        	}catch (IOException e) {
        		// An IOException on a write is almost always due to
                // the remote client aborting the request.  Wrap this
                // so that it can be handled better by the error dispatcher.
        		
        		// throw new ClientAbortException(e);
        	}
        }
        
	}


	public void write(char[] cbuf, int off, int len) throws IOException {
		if (suspended)
            return;

        conv.convert(cbuf, off, len);
        conv.flushBuffer();
        charsWritten += len;
	}
	
	
	
	public void write(byte b[], int off, int len) throws IOException {

        if (suspended)
            return;

        writeBytes(b, off, len);

    }
	
	
	private void writeBytes(byte b[], int off, int len) 
    throws IOException {
		if (closed)
            return;
		
		bb.append(b, off, len);
		bytesWritten += len;
		
		// if called from within flush(), then immediately flush
        // remaining bytes
        if (doFlush) {
        	 bb.flushBuffer();
        }
	}
	
	
	 /**
     * Append a string to the buffer
     */
    public void write(String s, int off, int len)
        throws IOException {

        if (suspended)
            return;

        charsWritten += len;
        if (s == null)
            s = "null";
        conv.convert(s, off, len);
        conv.flushBuffer();

    }
    
    
    public void checkConverter() 
    throws IOException {

    if (!gotEnc)
        setConverter();

    }
    
    
    protected void setConverter() 
    throws IOException {
    	
    	if (coyoteResponse != null)
            enc = coyoteResponse.getCharacterEncoding();
    	
    	gotEnc = true;
    	
    	conv = (C2BConverter) encoders.get(enc);
    	if (conv == null) {
    		
    		conv = new C2BConverter(bb, enc);
    	}
    	
    	encoders.put(enc, conv);
    	
    }
	

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}

	
	/**
     * Close the output buffer. This tries to calculate the response size if 
     * the response has not been committed yet.
     * 
     * @throws IOException An underlying IOException occurred
     */
	public void close() throws IOException {
		if (closed)
            return;
		if (suspended)
	        return;
		
		if ((!coyoteResponse.isCommitted()) 
	            && (coyoteResponse.getContentLengthLong() == -1)) {
			
			// If this didn't cause a commit of the response, the final content
            // length can be calculated
            if (!coyoteResponse.isCommitted()) {
                coyoteResponse.setContentLength(bb.getLength());
            }
		}
		
		doFlush(false);
		
		closed = true;
		
		// The request should have been completely read by the time the response
        // is closed.
		
		Request req = (Request) coyoteResponse.getRequest().getNote(
                CoyoteAdapter.ADAPTER_NOTES);
		req.inputBuffer.close();
		
		coyoteResponse.finish();
	}
	
	
	/**
     * Flush bytes or chars contained in the buffer.
     * 
     * @throws IOException An underlying IOException occurred
     */
    protected void doFlush(boolean realFlush)
        throws IOException {
    	
    	
    	if (suspended)
            return;
    	
    	try {
    		doFlush = true;
    		if (initial) {
    			coyoteResponse.sendHeaders();
    			initial = false;
    		}
    		if (bb.getLength() > 0) {
    			bb.flushBuffer();
    		}
    	}finally {
    		doFlush = false;
    	}
    	
    	
    	if (realFlush) {
    		//...
    	}
    	
    }

	
	
}
