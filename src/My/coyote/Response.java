package My.coyote;

import java.io.IOException;
import java.util.Locale;

import My.tomcat.util.buf.ByteChunk;
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
    
    
    
    // General informations
    private long bytesWritten=0;
    
    
    /**
     * Has the charset been explicitly set.
     */
    protected boolean charsetSet = false;
    
    
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
    	
    	int semicolonIndex = -1;
    	
    	if (type == null) {
            this.contentType = null;
            return;
        }
    	
    	/*
         * Remove the charset param (if any) from the Content-Type, and use it
         * to set the response encoding.
         * The most recent response encoding setting will be appended to the
         * response's Content-Type (as its charset param) by getContentType();
         */
    	boolean hasCharset = false;
    	int len = type.length();
        int index = type.indexOf(';');
        while (index != -1) {
        	
        	semicolonIndex = index;
            index++;
            while (index < len && Character.isSpace(type.charAt(index))) {
                index++;
            }
            if (index+8 < len
                    && type.charAt(index) == 'c'
                    && type.charAt(index+1) == 'h'
                    && type.charAt(index+2) == 'a'
                    && type.charAt(index+3) == 'r'
                    && type.charAt(index+4) == 's'
                    && type.charAt(index+5) == 'e'
                    && type.charAt(index+6) == 't'
                    && type.charAt(index+7) == '=') {
                hasCharset = true;
                break;
            }
            index = type.indexOf(';', index);
        }
        
        
        if (!hasCharset) {
        	this.contentType = type;
        	return;
        }
        
        
        // has charset param, implements latter.
        
        
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
    
    
    
    public String getContentType() {

        String ret = contentType;

        if (ret != null 
            && characterEncoding != null
            && charsetSet) {
            ret = ret + ";charset=" + characterEncoding;
        }

        return ret;
    }
    
    
    /** 
     * Write a chunk of bytes.
     */
    public void doWrite(ByteChunk chunk)
        throws IOException
    {
        outputBuffer.doWrite(chunk, this);
        bytesWritten+=chunk.getLength();
    }
    
    
    
    public void recycle() {
    	
    	contentType = null;
        contentLanguage = null;
        
        locale = DEFAULT_LOCALE;
        characterEncoding = Constants.DEFAULT_CHARACTER_ENCODING;
        
        contentLength = -1;
        status = 200;
        message = null;
        
        commited = false;
 
        errorURI = null;
        headers.clear();
        
        // update counters
        bytesWritten=0;
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
    
    
    public void finish() throws IOException {
        action(ActionCode.ACTION_CLOSE, this);
    }
    
}
