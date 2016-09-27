package My.catalina.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import My.catalina.Globals;
import My.naming.resources.CacheEntry;
import My.naming.resources.ProxyDirContext;

/**
 * <p>The default resource-serving servlet for most web applications,
 * used to serve static resources such as HTML pages and images.
 */

public class DefaultServlet extends HttpServlet{
	
	
	
    
	// ------------------ Instance Variables ------------------
    
	/**
     * The input buffer size to use when serving resources.
     */
    protected int input = 2048;
    
    
    
    /**
     * Should we generate directory listings?
     */
    protected boolean listings = false;


    /**
     * Read only flag. By default, it's set to true.
     */
    protected boolean readOnly = true;


    /**
     * The output buffer size to use when serving resources.
     */
    protected int output = 2048;
	
	
    /**
     * Proxy directory context.
     */
    protected ProxyDirContext resources = null;
	

    
	// ---------------------- Public Methods ----------------------
    
    
    /**
     * Initialize this servlet.
     */
    public void init() throws ServletException {
    	
    	
    	// check on the specified buffer sizes
    	if (input < 256)
            input = 256;
        if (output < 256)
            output = 256;
        
        
        
        // Load the proxy dir context.
        resources = (ProxyDirContext) getServletContext()
        	.getAttribute(Globals.RESOURCES_ATTR);
        
    }
    
    
	// ------------------------- Protected Methods ---------------------
    
    /**
     * Return the relative path associated with this servlet.
     *
     * @param request The servlet request we are processing
     */
    protected String getRelativePath(HttpServletRequest request) {
    	
    	// IMPORTANT: DefaultServlet can be mapped to '/' or '/path/*' but always
        // serves resources from the web app root with context rooted paths.
        // i.e. it can not be used to mount the web app root under a sub-path
        // This method must construct a complete context rooted path, although
        // subclasses can change this behaviour.
    	
    	
    	//extract the desired path directly from the request
    	String result = request.getPathInfo();
    	if (result == null) {
    		result = request.getServletPath();
    	}
    	else {
    		result = request.getServletPath() + result;
    	}
    	
    	if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return (result);
    	
    }
    
    
    
    
    
	
	/**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
        throws IOException, ServletException {

        // Serve the requested resource, including the data content
        serveResource(request, response, true);

    }

    /**
     * Serve the specified resource, optionally including the data content.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param content Should the content be included?
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void serveResource(HttpServletRequest request,
                                 HttpServletResponse response,
                                 boolean content)
        throws IOException, ServletException {
    	
    	// Identify the requested resource path
        String path = getRelativePath(request);
        
        CacheEntry cacheEntry = resources.lookupCache(path);
        
    }

	
	
}
