package My.catalina.connector;

import javax.servlet.http.HttpServletRequest;

import My.catalina.Context;
import My.catalina.Host;
import My.catalina.Wrapper;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.mapper.MappingData;

/**
 * Wrapper object for the Coyote request.
 */

public class Request implements HttpServletRequest{

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
    
    /**
     * Associated context.
     */
    protected Context context = null;

    /**
     * Return the Context within which this Request is being processed.
     */
    public Context getContext() {
        return (this.context);
    }


    /**
     * Set the Context within which this Request is being processed.  This
     * must be called as soon as the appropriate Context is identified, because
     * it identifies the value to be returned by <code>getContextPath()</code>,
     * and thus enables parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    
    
    
    /**
     * Return the Host within which this Request is being processed.
     */
    public Host getHost() {
    	 if (getContext() == null)
             return null;
    	 return (Host)getContext().getParent();
    }
    
    
    
    /**
     * Mapping data.
     */
    protected MappingData mappingData = new MappingData();

    /**
     * Return mapping data.
     */
    public MappingData getMappingData() {
        return (mappingData);
    }
    
    
    /**
     * Get the request path.
     * 
     * @return the request path
     */
    public MessageBytes getRequestPathMB() {
        return (mappingData.requestPath);
    }
    
    
    /**
     * Associated wrapper.
     */
    protected Wrapper wrapper = null;

    /**
     * Return the Wrapper within which this Request is being processed.
     */
    public Wrapper getWrapper() {
        return (this.wrapper);
    }


    /**
     * Set the Wrapper within which this Request is being processed.  This
     * must be called as soon as the appropriate Wrapper is identified, and
     * before the Request is ultimately passed to an application servlet.
     * @param wrapper The newly associated Wrapper
     */
    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }
    
    
    
}
