package My.catalina.connector;

import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
    
    
    
    public String getHeader(String name) {

    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }

        return request.getHeader(name);
    }
    
    
    public long getDateHeader(String name) {

    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }

        return request.getDateHeader(name);
    }
    
    
    
    public Locale getLocale() {
    	
    	 if (request == null) {
             throw new IllegalStateException("requestFacade.nullRequest");
         }
    	 
    	 return request.getLocale();
    	
    }
    
    
    public Cookie[] getCookies() {
    	
    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }

        Cookie[] ret = null;
        
        ret = request.getCookies();
        
        return ret;
    }
    
    
    public String getParameter(String name) {
    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }
    	
    	return request.getParameter(name);
    }
    
    
    
    public String getProtocol() {
    	
    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }
    	
    	return request.getProtocol();
    }
    
    
    public HttpSession getSession() {

        if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }

        return getSession(true);
    }
    
    
    public HttpSession getSession(boolean create) {
    	
    	if (request == null) {
            throw new IllegalStateException("requestFacade.nullRequest");
        }
    	
    	return request.getSession(create);
    	
    }
	
}
