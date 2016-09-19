package My.catalina.connector;

import javax.servlet.http.HttpServletRequest;

/**
 * Wrapper object for the Coyote request.
 */

public final class Request implements HttpServletRequest{

	 // -------------------- Properties -------------------- 


    /**
     * Coyote request.
     */
    protected My.coyote.Request coyoteRequest;

    /**
     * Set the Coyote request.
     * 
     * @param coyoteRequest The Coyote request
     */
    public void setCoyoteRequest(My.coyote.Request coyoteRequest) {
        this.coyoteRequest = coyoteRequest;
        inputBuffer.setRequest(coyoteRequest);
    }

    /**
     * Get the Coyote request.
     */
    public My.coyote.Request getCoyoteRequest() {
        return (this.coyoteRequest);
    }
    
    
    
	// ---------------------- Variables ----------------------------
    
    /**
     * The associated input buffer.
     */
    protected InputBuffer inputBuffer = new InputBuffer();
    
    
	
	// --------------- Request Methods ---------------
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
    }
	
	
	
    /**
     * The response with which this request is associated.
     */
    protected My.catalina.connector.Response response = null;

    /**
     * Return the Response with which this Request is associated.
     */
    public My.catalina.connector.Response getResponse() {
        return (this.response);
    }

    /**
     * Set the Response with which this Request is associated.
     *
     * @param response The new associated response
     */
    public void setResponse(My.catalina.connector.Response response) {
        this.response = response;
    }
}
