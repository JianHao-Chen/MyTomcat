package My.coyote;

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
    
    
    // -------------------- Per-Response "notes" --------------------


    public final void setNote(int pos, Object value) {
        notes[pos] = value;
    }


    public final Object getNote(int pos) {
        return notes[pos];
    }
}
