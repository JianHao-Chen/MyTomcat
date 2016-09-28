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

    
 // -------------------- Headers --------------------
    /**
     * Warning: This method always returns <code>false<code> for Content-Type
     * and Content-Length.
     */
    public boolean containsHeader(String name) {
        return headers.getHeader(name) != null;
    }


    public void setHeader(String name, String value) {
        char cc=name.charAt(0);
        if( cc=='C' || cc=='c' ) {
            if( checkSpecialHeader(name, value) )
            return;
        }
        headers.setValue(name).setString( value);
    }


    public void addHeader(String name, String value) {
        char cc=name.charAt(0);
        if( cc=='C' || cc=='c' ) {
            if( checkSpecialHeader(name, value) )
            return;
        }
        headers.addValue(name).setString( value );
    }
    
    
    
    /** 
     * Set internal fields for special header names. 
     * Called from set/addHeader.
     * Return true if the header is special, no need to set the header.
     */
    private boolean checkSpecialHeader( String name, String value) {
        // XXX Eliminate redundant fields !!!
        // ( both header and in special fields )
        if( name.equalsIgnoreCase( "Content-Type" ) ) {
            setContentType( value );
            return true;
        }
        if( name.equalsIgnoreCase( "Content-Length" ) ) {
            try {
                long cL=Long.parseLong( value );
                setContentLength( cL );
                return true;
            } catch( NumberFormatException ex ) {
                // Do nothing - the spec doesn't have any "throws" 
                // and the user might know what he's doing
                return false;
            }
        }
        if( name.equalsIgnoreCase( "Content-Language" ) ) {
            // XXX XXX Need to construct Locale or something else
        }
        return false;
    }
    
    
    /** Signal that we're done with the headers, and body will follow.
     *  Any implementation needs to notify ContextManager, to allow
     *  interceptors to fix headers.
     */
    public void sendHeaders() throws IOException {
        action(ActionCode.ACTION_COMMIT, this);
        commited = true;
    }
    
    
    
    /**
     * Sets the content type.
     *
     * This method must preserve any response charset that may already have 
     * been set via a call to response.setContentType(), response.setLocale(),
     * or response.setCharacterEncoding().
     *
     * @param type the content type
     */
    public void setContentType(String type) {
    	
    }
    
    
    
    
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
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
