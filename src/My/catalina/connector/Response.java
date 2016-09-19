package My.catalina.connector;

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
    
}
