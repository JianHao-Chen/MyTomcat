package My.coyote;

import java.io.IOException;

import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.MimeHeaders;

public final class Request {

	// -------------- Constructors --------------
	
	public Request() {
		
	}
	
	
	// ----------------- Instance Variables -----------------
	private MessageBytes methodMB = MessageBytes.newInstance();
	private MessageBytes unparsedURIMB = MessageBytes.newInstance();
	private MessageBytes uriMB = MessageBytes.newInstance();
	private MessageBytes protoMB = MessageBytes.newInstance();
	
	private MimeHeaders headers = new MimeHeaders();
	
	
	// Time of the request - usefull to avoid repeated calls to System.currentTime
    private long startTime = 0L;
	
	
	 /**
     * Associated input buffer.
     */
    private InputBuffer inputBuffer = null;
	
    
    private int bytesRead=0;
	
	private RequestInfo reqProcessorMX = new RequestInfo(this);
	
	// ------------------- Properties --------------------
	
	public MessageBytes method() {
        return methodMB;
    }
	
	public MessageBytes unparsedURI() {
        return unparsedURIMB;
    }
	
	public MessageBytes requestURI() {
        return uriMB;
    }
	
	public MessageBytes protocol() {
	        return protoMB;
	}
	
	public MimeHeaders getMimeHeaders() {
        return headers;
    }
	
	public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
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
