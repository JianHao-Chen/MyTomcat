package My.coyote;

import java.io.IOException;
import java.util.Locale;

import My.tomcat.util.http.MimeHeaders;

public final class Response {

	// ----------------------------------------------------------- Constructors


    public Response() {
    }
    
 // ----------------------------------------------------- Class Variables

    /**
     * Default locale as mandated by the spec.
     */
    private static Locale DEFAULT_LOCALE = Locale.getDefault();


    // ----------------------------------------------------- Instance Variables
    
    /**
     * Status code.
     */
    protected int status = 200;
    
    /**
     * Status message.
     */
    protected String message = null;


    /**
     * Response headers.
     */
    protected MimeHeaders headers = new MimeHeaders();


    /**
     * Associated output buffer.
     */
    protected OutputBuffer outputBuffer;
    
    
    /**
     * Notes.
     */
    protected Object notes[] = new Object[Constants.MAX_NOTES];
    
    
    /**
     * Committed flag.
     */
    protected boolean commited = false;
    
    
    /**
     * Action hook.
     */
    public ActionHook hook;
    
    
    /**
     * HTTP specific fields.
     */
    protected String contentType = null;
    protected String contentLanguage = null;
    protected String characterEncoding = Constants.DEFAULT_CHARACTER_ENCODING;
    protected long contentLength = -1;
    private Locale locale = DEFAULT_LOCALE;
    
    
    
    /**
     * Request error URI.
     */
    protected String errorURI = null;

    protected Request req;
    
 // ------------------------------------------------------------- Properties
    
    public Request getRequest() {
        return req;
    }

    public void setRequest( Request req ) {
        this.req=req;
    }

    public OutputBuffer getOutputBuffer() {
        return outputBuffer;
    }


    public void setOutputBuffer(OutputBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }


    public MimeHeaders getMimeHeaders() {
        return headers;
    }
    
    
    public ActionHook getHook() {
        return hook;
    }


    public void setHook(ActionHook hook) {
        this.hook = hook;
    }
    
    
	// -------------------- Actions --------------------
    
    public void action(ActionCode actionCode, Object param) {
        if (hook != null) {
            if( param==null ) 
                hook.action(actionCode, this);
            else
                hook.action(actionCode, param);
        }
    }

    
    
	// -------------------- State --------------------
   
    public int getStatus() {
        return status;
    }

    
    /** 
     * Set the response status 
     */ 
    public void setStatus( int status ) {
        this.status = status;
    }


    /**
     * Get the status message.
     */
    public String getMessage() {
        return message;
    }


    /**
     * Set the status message.
     */
    public void setMessage(String message) {
        this.message = message;
    }


    public boolean isCommitted() {
        return commited;
    }


    public void setCommitted(boolean v) {
        this.commited = v;
    }
    
    
    
    public int getContentLength() {
        long length = getContentLengthLong();
        
        if (length < Integer.MAX_VALUE) {
            return (int) length;
        }
        return -1;
    }
    
    public long getContentLengthLong() {
        return contentLength;
    }
    
    
    
    // -------------------- Per-Response "notes" --------------------


    public final void setNote(int pos, Object value) {
        notes[pos] = value;
    }


    public final Object getNote(int pos) {
        return notes[pos];
    }
    
    
    
    
    
	// -------------------- Methods --------------------
    
    public void acknowledge() throws IOException {
        action(ActionCode.ACTION_ACK, this);
    }
    
}
