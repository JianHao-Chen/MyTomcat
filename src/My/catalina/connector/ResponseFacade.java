package My.catalina.connector;

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
	
}
