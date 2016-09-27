package My.catalina.core;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import My.catalina.Globals;
import My.catalina.Wrapper;
import My.catalina.connector.Request;

public final class ApplicationFilterFactory {

	// -------------------------------------------------------------- Constants


    public static final int ERROR = 1;
    public static final Integer ERROR_INTEGER = new Integer(ERROR);
    public static final int FORWARD = 2;
    public static final Integer FORWARD_INTEGER = new Integer(FORWARD);
    public static final int INCLUDE = 4;
    public static final Integer INCLUDE_INTEGER = new Integer(INCLUDE);
    public static final int REQUEST = 8;
    public static final Integer REQUEST_INTEGER = new Integer(REQUEST);

    public static final String DISPATCHER_TYPE_ATTR = 
        Globals.DISPATCHER_TYPE_ATTR;
    public static final String DISPATCHER_REQUEST_PATH_ATTR = 
        Globals.DISPATCHER_REQUEST_PATH_ATTR;

    private static ApplicationFilterFactory factory = null;
    
    
	// --------------------- Constructors ---------------------
    /*
     * Prevent instanciation outside of the getInstanceMethod().
     */
    private ApplicationFilterFactory() {
    }
    
    
	// -------------------- Public Methods --------------------
    
    /**
     * Return the fqctory instance.
     */
    public static ApplicationFilterFactory getInstance() {
        if (factory == null) {
            factory = new ApplicationFilterFactory();
        }
        return factory;
    }
    
    
    /**
     * Construct and return a FilterChain implementation that will wrap the
     * execution of the specified servlet instance.  If we should not execute
     * a filter chain at all, return <code>null</code>.
     *
     * @param request The servlet request we are processing
     * @param servlet The servlet instance to be wrapped
     */
    public ApplicationFilterChain createFilterChain
        (ServletRequest request, Wrapper wrapper, Servlet servlet) {
    	
    	// get the dispatcher type
        int dispatcher = -1; 
        
        if (request.getAttribute(DISPATCHER_TYPE_ATTR) != null) {
            Integer dispatcherInt = 
                (Integer) request.getAttribute(DISPATCHER_TYPE_ATTR);
            dispatcher = dispatcherInt.intValue();
        }
        
        String requestPath = null;
        Object attribute = request.getAttribute(DISPATCHER_REQUEST_PATH_ATTR);
        
        if (attribute != null){
            requestPath = attribute.toString();
        }
        
        HttpServletRequest hreq = null;
        
        if (request instanceof HttpServletRequest) 
        	hreq = (HttpServletRequest)request;
        
        // If there is no servlet to execute, return null
        if (servlet == null)
            return (null);
        
        // Create and initialize a filter chain object
        ApplicationFilterChain filterChain = null;
        if (request instanceof Request) {
        	
        	Request req = (Request) request;
        	filterChain = (ApplicationFilterChain) req.getFilterChain();
        	if (filterChain == null) {
        		filterChain = new ApplicationFilterChain();
        		req.setFilterChain(filterChain);
        	}
        }
        
        
        filterChain.setServlet(servlet);
        
        return filterChain;
        
        
    }
    
    
    
}
