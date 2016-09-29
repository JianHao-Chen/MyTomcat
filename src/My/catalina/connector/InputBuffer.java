package My.catalina.connector;

import java.io.IOException;
import java.io.Reader;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.CharChunk;

public class InputBuffer extends Reader
	implements ByteChunk.ByteInputChannel, CharChunk.CharInputChannel,
    CharChunk.CharOutputChannel {
	
	
	// ------------------ Instance Variables ------------------
	
	/**
     * Associated Coyote request.
     */
    private My.coyote.Request coyoteRequest;
    
    
    /**
     * Flag which indicates if the input buffer is closed.
     */
    private boolean closed = false;
    
    
    
    
	
	// ---------------------- Properties ----------------------
	 /**
     * Associated Coyote request.
     * 
     * @param coyoteRequest Associated Coyote request
     */
    public void setRequest(My.coyote.Request coyoteRequest) {
	this.coyoteRequest = coyoteRequest;
    }


    /**
     * Get associated Coyote request.
     * 
     * @return the associated Coyote request
     */
    public My.coyote.Request getRequest() {
        return this.coyoteRequest;
    }
	
	
	
	
	
	
	
	

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() throws IOException {
		 closed = true;
		
	}

	@Override
	public void realWriteChars(char[] cbuf, int off, int len)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int realReadChars(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int realReadBytes(byte[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
