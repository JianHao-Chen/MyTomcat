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
	private int serverPort = -1;
    private MessageBytes serverNameMB = MessageBytes.newInstance();
	
	
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
	
	
	private Response response;
	
	
	
	
	/**
     * HTTP specific fields. (remove them ?)
     */
    private long contentLength = -1;
    private MessageBytes contentTypeMB = null;
    
	
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
    
    
    
    /** 
     * Return the buffer holding the server name, if
     * any. Use isNull() to check if there is no value
     * set.
     * This is the "virtual host", derived from the
     * Host: header.
     */
    public MessageBytes serverName() {
        return serverNameMB;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(int serverPort ) {
        this.serverPort=serverPort;
    }
    
    
    
	// -------------------- Associated response --------------------
	public Response getResponse() {
        return response;
    }

    public void setResponse( Response response ) {
        this.response=response;
        response.setRequest( this );
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
	
	
	
	
	// -------------------- encoding/type --------------------
	public void setContentLength(int len) {
        this.contentLength = len;
    }


    public int getContentLength() {
        long length = getContentLengthLong();

        if (length < Integer.MAX_VALUE) {
            return (int) length;
        }
        return -1;
    }

    public long getContentLengthLong() {
        if( contentLength > -1 ) return contentLength;

        MessageBytes clB = headers.getUniqueValue("content-length");
        contentLength = (clB == null || clB.isNull()) ? -1 : clB.getLong();

        return contentLength;
    }

    public String getContentType() {
        contentType();
        if ((contentTypeMB == null) || contentTypeMB.isNull()) 
            return null;
        return contentTypeMB.toString();
    }


    public void setContentType(String type) {
        contentTypeMB.setString(type);
    }


    public MessageBytes contentType() {
        if (contentTypeMB == null)
            contentTypeMB = headers.getValue("content-type");
        return contentTypeMB;
    }


    public void setContentType(MessageBytes mb) {
        contentTypeMB=mb;
    }
	
	
	
}
