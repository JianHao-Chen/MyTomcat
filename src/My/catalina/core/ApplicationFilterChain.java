package My.catalina.core;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApplicationFilterChain 
	implements FilterChain{

	
	// ---------------- Constructors ----------------
	/**
     * Construct a new chain instance with no defined filters.
     */
    public ApplicationFilterChain() {

        super();

    }
    
    
	// ----------------- Instance Variables -----------------
    /**
     * Filters.
     */
    private ApplicationFilterConfig[] filters = 
        new ApplicationFilterConfig[0];
	
    /**
     * The int which is used to maintain the current position 
     * in the filter chain.
     */
    private int pos = 0;


    /**
     * The int which gives the current number of filters in the chain.
     */
    private int n = 0;
    
    
    /**
     * The servlet instance to be executed by this chain.
     */
    private Servlet servlet = null;


    
    
	// --------------------- FilterChain Methods ---------------------
    
    
    /**
     * Invoke the next filter in this chain, passing the specified request
     * and response.  If there are no more filters in this chain, invoke
     * the <code>service()</code> method of the servlet itself.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {
    	
    	internalDoFilter(request,response);
    }
    
    
    
    
    
    private void internalDoFilter(ServletRequest request, ServletResponse response)
    	throws IOException, ServletException {
    	
    	// Call the next filter if there is one
    	if (pos < n) {
    		// implement latter.
    	}
    	
    	
    	// We fell off the end of the chain -- call the servlet instance
    	try {
    		
    		 if ((request instanceof HttpServletRequest) &&
    	                (response instanceof HttpServletResponse)) {
    			 
    			 servlet.service(
    					 (HttpServletRequest) request,
                         (HttpServletResponse) response);
    			 
    		 }
    	}catch (IOException e) {
    		
    	}catch (ServletException e) {
    		
    	}
    }
    
    
    
    
    /**
     * Release references to the filters and wrapper executed by this chain.
     */
    void release() {
    	
    	for (int i = 0; i < n; i++) {
            filters[i] = null;
        }
    	
    	n = 0;
        pos = 0;
        servlet = null;
    }
    
    
    
    
    
	// ------------------------ Package Methods ------------------------
    
    
    /**
     * Set the servlet that will be executed at the end of this chain.
     *
     * @param servlet The Wrapper for the servlet to be executed
     */
    void setServlet(Servlet servlet) {

        this.servlet = servlet;

    }
    
    
}
