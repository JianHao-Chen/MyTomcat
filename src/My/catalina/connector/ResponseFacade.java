package My.catalina.connector;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
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
    
    
    public ServletOutputStream getOutputStream()
    	throws IOException {
    	
    	ServletOutputStream sos = response.getOutputStream();
    	if (isFinished())
    		response.setSuspended(true);
    	return (sos);
    }
    
    
    public PrintWriter getWriter()
    throws IOException {
    	
    	PrintWriter writer = response.getWriter();
        if (isFinished())
            response.setSuspended(true);
        return (writer);
    	
    }
    
    
    public boolean isCommitted() {

        if (response == null) {
            throw new IllegalStateException("responseFacade.nullResponse");
        }

        return (response.isAppCommitted());
    }
    
    
    public void finish() {

        if (response == null) {
            throw new IllegalStateException("responseFacade.nullResponse");
        }

        response.setSuspended(true);
    }


    public boolean isFinished() {

        if (response == null) {
            throw new IllegalStateException("responseFacade.nullResponse");
        }

        return response.isSuspended();
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
	
	
	public void setContentType(String type) {
		
		if (isCommitted())
            return;
		
		response.setContentType(type);      
	}
	
	
	public void setContentLength(int len) {

        if (isCommitted())
            return;

        response.setContentLength(len);

    }
	
	
	public void setBufferSize(int size) {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setBufferSize(size);

    }
	
	
	
	 public void sendError(int sc, String msg)
     throws IOException {
		 
		 if (isCommitted())
	            throw new IllegalStateException();
	            
	     response.setAppCommitted(true);      
		 
	     response.sendError(sc, msg);
	 }
	 
	 
	 public void addCookie(Cookie cookie) {

	 	if (isCommitted())
	    	return;

	    response.addCookie(cookie);

	}
	 
	 
	 public String encodeURL(String url) {
		 if (response == null) {
	            throw new IllegalStateException("responseFacade.nullResponse");
	        }

	        return response.encodeURL(url);
	 }
}
