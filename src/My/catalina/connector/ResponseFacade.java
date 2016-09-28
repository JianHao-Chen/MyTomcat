package My.catalina.connector;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Facade class that wraps a Coyote response object. 
 * All methods are delegated to the wrapped response.
 */

public class ResponseFacade implements HttpServletResponse{

	 // -------------------- Constructors --------------------
	
	/**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public ResponseFacade(Response response) {

         this.response = response;
    }
    
    // ------------------------Instance Variables -------------
   
    /**
     * The wrapped response.
     */
    protected Response response = null;
	
    
    
    
    
	// --------------- ServletResponse Methods ---------------
    
    public boolean isCommitted() {

        if (response == null) {
            throw new IllegalStateException("responseFacade.nullResponse");
        }

        return (response.isAppCommitted());
    }
    
    
    public void setStatus(int sc) {

        if (isCommitted())
            return;

        response.setStatus(sc);

    }
    
    
    public void sendError(int sc)
    	throws IOException {

    	if (isCommitted())
    		throw new IllegalStateException
            	(/*sm.getString("responseBase.reset.ise")*/);

    	response.setAppCommitted(true);

    	response.sendError(sc);

    }

	public void setHeader(String name, String value) {
		if (isCommitted())
            return;

        response.setHeader(name, value);
	}
}
