package My.catalina.connector;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class Response implements HttpServletResponse{

	
	
	// -------------------------- Properties --------------------------


    /**
     * Associated Catalina connector.
     */
    protected Connector connector;

    /**
     * Return the Connector through which this Request was received.
     */
    public Connector getConnector() {
        return (this.connector);
    }

    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
        
        //connector.getProtocol() is 
        //"My.coyote.http11.Http11NioProtocol"
     
        outputBuffer = new OutputBuffer();
     
        outputStream = new CoyoteOutputStream(outputBuffer);
        writer = new CoyoteWriter(outputBuffer);
    }
    
    
    
    
    /**
     * The associated output buffer.
     */
    protected OutputBuffer outputBuffer;


    /**
     * The associated output stream.
     */
    protected CoyoteOutputStream outputStream;
    
    /**
     * The associated writer.
     */
    protected CoyoteWriter writer;
    
    
    /**
     * The application commit flag.
     */
    protected boolean appCommitted = false;
    
    
    /**
     * The included flag.
     */
    protected boolean included = false;
    
    
    /**
     * Coyote response.
     */
    protected My.coyote.Response coyoteResponse;

    /**
     * Set the Coyote response.
     * 
     * @param coyoteResponse The Coyote response
     */
    public void setCoyoteResponse(My.coyote.Response coyoteResponse) {
        this.coyoteResponse = coyoteResponse;
        outputBuffer.setResponse(coyoteResponse);
    }

    /**
     * Get the Coyote response.
     */
    public My.coyote.Response getCoyoteResponse() {
        return (coyoteResponse);
    }
    
    
    
    
    /**
     * The request with which this response is associated.
     */
    protected Request request = null;

    /**
     * Return the Request with which this Response is associated.
     */
    public My.catalina.connector.Request getRequest() {
        return (this.request);
    }

    /**
     * Set the Request with which this Response is associated.
     *
     * @param request The new associated request
     */
    public void setRequest(My.catalina.connector.Request request) {
        this.request = (Request) request;
    }
    
    
    
    /**
     * The facade associated with this response.
     */
    protected ResponseFacade facade = null;

    /**
     * Return the <code>ServletResponse</code> for which this object
     * is the facade.
     */
    public HttpServletResponse getResponse() {
        if (facade == null) {
            facade = new ResponseFacade(this);
        }
        return (facade);
    }
    
    
    
    
    
	// ---------------------- Response Methods----------------------
    
    /**
     * Application commit flag accessor.
     */
    public boolean isAppCommitted() {
        return (this.appCommitted || isCommitted() || isSuspended()
                || ((getContentLength() > 0) 
                    && (getContentCount() >= getContentLength())));
    }
    
    
    
    /**
     * Set the suspended flag.
     * 
     * @param suspended The new suspended flag value
     */
    public void setSuspended(boolean suspended) {
        outputBuffer.setSuspended(suspended);
    }


    /**
     * Suspended flag accessor.
     */
    public boolean isSuspended() {
        return outputBuffer.isSuspended();
    }
    
    
    
    /**
     * Return the content length that was set or calculated for this Response.
     */
    public int getContentLength() {
        return (coyoteResponse.getContentLength());
    }
    
    
    /**
     * Return the number of bytes actually written to the output stream.
     */
    public int getContentCount() {
        return outputBuffer.getContentWritten();
    }
    
    
    /**
     * Set the application commit flag.
     * 
     * @param appCommitted The new application committed flag value
     */
    public void setAppCommitted(boolean appCommitted) {
        this.appCommitted = appCommitted;
    }
    
    
    /**
     * Send an error response with the specified status and a
     * default message.
     *
     * @param status HTTP status code to send
     *
     * @exception IllegalStateException if this response has
     *  already been committed
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int status) 
        throws IOException {
        sendError(status, null);
    }
    
    
    
    /**
     * Send an error response with the specified status and message.
     *
     * @param status HTTP status code to send
     * @param message Corresponding message to send
     *
     * @exception IllegalStateException if this response has
     *  already been committed
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int status, String message) 
        throws IOException {
    	
    }
    
    
    
    
	// -------------- ServletResponse Methods --------------
    
    /**
     * Has the output of this response already been committed?
     */
    public boolean isCommitted() {
        return (coyoteResponse.isCommitted());
    }

    
    
    
	// ---------------- HttpServletResponse Methods ----------------
    
    
    /**
     * Send an acknowledgment of a request.
     * 
     * @exception IOException if an input/output error occurs
     */
    public void sendAcknowledgement()
        throws IOException {
    	
    	coyoteResponse.acknowledge();
    }

    
    /**
     * Set the HTTP status to be returned with this response.
     *
     * @param status The new HTTP status
     */
    public void setStatus(int status) {
        setStatus(status, null);
    }
    
    
    
    /**
     * Set the HTTP status and message to be returned with this response.
     *
     * @param status The new HTTP status
     * @param message The associated text message
     *
     * @deprecated As of Version 2.1 of the Java Servlet API, this method
     *  has been deprecated due to the ambiguous meaning of the message
     *  parameter.
     */
    public void setStatus(int status, String message) {

        if (isCommitted())
            return;

        // Ignore any call from an included servlet
        if (included)
            return;

        coyoteResponse.setStatus(status);
        coyoteResponse.setMessage(message);

    }
    
    

    /**
     * Set the specified header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Value to be set
     */
	public void setHeader(String name, String value) {
		if (name == null || name.length() == 0 || value == null) {
            return;
        }

        if (isCommitted())
            return;
        
        // Ignore any call from an included servlet
        if (included)
            return;

        coyoteResponse.setHeader(name, value);
		
	}
    	
    
}
