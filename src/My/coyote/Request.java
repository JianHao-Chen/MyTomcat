package My.coyote;

import java.io.IOException;

import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.http.MimeHeaders;

public final class Request {

	// -------------- Constructors --------------
	
	public Request() {
		
	}
	
	
	// ----------------- Instance Variables -----------------
	
	
	private MimeHeaders headers = new MimeHeaders();
	
	
	 /**
     * Associated input buffer.
     */
    private InputBuffer inputBuffer = null;
	
    
    private int bytesRead=0;
	
	private RequestInfo reqProcessorMX = new RequestInfo(this);
	
	// ------------------- Properties --------------------
	
	public MimeHeaders getMimeHeaders() {
        return headers;
    }
	
	
	
	
	
	 // -------------------- Input Buffer --------------------


    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }


    public void setInputBuffer(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }
    
    /**
     * Read data from the input buffer and put it into a byte chunk.
     *
     * The buffer is owned by the protocol implementation - it will be reused on the next read.
     * The Adapter must either process the data in place or copy it to a separate buffer if it needs
     * to hold it. In most cases this is done during byte->char conversions or via InputStream. Unlike
     * InputStream, this interface allows the app to process data in place, without copy.
     *
     */
    public int doRead(ByteChunk chunk) 
    	throws IOException {
    	int n = inputBuffer.doRead(chunk, this);
        if (n > 0) {
            bytesRead+=n;
        }
        return n;
    }
	
	
	// -------------------- Info  --------------------
	
	public RequestInfo getRequestProcessor() {
        return reqProcessorMX;
    }
	
}
