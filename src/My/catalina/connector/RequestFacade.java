package My.catalina.connector;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * Facade class that wraps a Coyote request object.  
 * All methods are delegated to the wrapped request.
 */

public class RequestFacade implements HttpServletRequest{

	// -------------------- Constructors --------------------
	
	 /**
     * Construct a wrapper for the specified request.
     *
     * @param request The request to be wrapped
     */
    public RequestFacade(Request request) {

        this.request = request;

    }
    
    
	// ------------------------ Instance Variables --------------------
    /**
     * The wrapped request.
     */
    protected Request request = null;
    
    
	// ---------------------- Public Methods ----------------------
    
    
    
	// ---------------------- ServletRequest Methods ---------------
    
    public Object getAttribute(String name) {
    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }

        return request.getAttribute(name);
    }
    
    
    public String getMethod() {

    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }

        return request.getMethod();
    }
    
    public String getPathInfo() {

    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }

        return request.getPathInfo();
    }
    
    
    public String getServletPath() {
    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }

        return request.getServletPath();
    }
    
	
}
