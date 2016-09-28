package My.catalina.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import My.catalina.Globals;
import My.naming.resources.CacheEntry;
import My.naming.resources.ProxyDirContext;
import My.naming.resources.ResourceAttributes;

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
    
    
    /**
     * Should the Accept-Ranges: bytes header be send with static resources?
     */
    protected boolean useAcceptRanges = true;
	

    
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
        
        if (!cacheEntry.exists) {
        	
        	// response send error.
        }
        
        
        // If the resource is not a collection, and the resource path
        // ends with "/" or "\", return NOT FOUND
        if (cacheEntry.context == null) {
        	
        	if (path.endsWith("/") || (path.endsWith("\\"))) {
        		
        		// Check if we're included so we can return the appropriate 
                // missing resource name in the error
        		
        		
        		// response send error.
        		// return;
        	}
        }
        
        boolean isError = false;
        
        Integer status =
            (Integer) request.getAttribute("javax.servlet.error.status_code");
        /*if (status != null) {
            isError = status.intValue() >= HttpServletResponse.SC_BAD_REQUEST;
        }*/
        
        
        // Check if the conditions specified in the optional If headers are
        // satisfied.
        if (cacheEntry.context == null) {
        
        	// Checking If headers
        	boolean included =
                (request.getAttribute(Globals.INCLUDE_CONTEXT_PATH_ATTR) != null);
        	
        	if (!included && !isError &&
                    !checkIfHeaders(request, response, cacheEntry.attributes)) {
                return;
            }
        }
        
        
        
        // Find content type.
        String contentType = cacheEntry.attributes.getMimeType();
        if (contentType == null) {
        	contentType = getServletContext().getMimeType(cacheEntry.name);
        	cacheEntry.attributes.setMimeType(contentType);
        }
        
        
        ArrayList ranges = null;
        long contentLength = -1L;
        
        if (cacheEntry.context != null) {
        	
        	// implements latter.
        }
        else {
        	
        	if (!isError) {
        		
        		if (useAcceptRanges) {
        			// Accept ranges header
                    response.setHeader("Accept-Ranges", "bytes");
        		}
        		
        		// Parse range specifier
        		ranges = parseRange(request, response, cacheEntry.attributes);
        		
        		// ETag header
                response.setHeader("ETag", cacheEntry.attributes.getETag());
                
             // Last-Modified header
                response.setHeader("Last-Modified",
                        cacheEntry.attributes.getLastModifiedHttp());
        	}
        	
        }
        
        
    }
    
    
    /**
     * Check if the conditions specified in the optional If headers are
     * satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceAttributes The resource information
     * @return boolean true if the resource meets all the specified conditions,
     * and false if any of the conditions is not satisfied, in which case
     * request processing is stopped
     */
    protected boolean checkIfHeaders(HttpServletRequest request,
                                     HttpServletResponse response,
                                     ResourceAttributes resourceAttributes)
        throws IOException {
    	
    	 return checkIfMatch(request, response, resourceAttributes)
         && checkIfModifiedSince(request, response, resourceAttributes)
    	 && checkIfNoneMatch(request, response, resourceAttributes)
         && checkIfUnmodifiedSince(request, response, resourceAttributes);
    	
    }
    
    
    /**
     * Check if the if-match condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    protected boolean checkIfMatch(HttpServletRequest request,
                                 HttpServletResponse response,
                                 ResourceAttributes resourceAttributes)
        throws IOException {
    	
    	 String eTag = resourceAttributes.getETag();
    	 String headerValue = request.getHeader("If-Match");
    	 
    	 if (headerValue != null) {
    		 
    	 }
    	 return true;
    }
    
    
    
    /**
     * Check if the if-modified-since condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    protected boolean checkIfModifiedSince(HttpServletRequest request,
                                         HttpServletResponse response,
                                         ResourceAttributes resourceAttributes)
        throws IOException {
    	
    	try {
    		long headerValue = request.getDateHeader("If-Modified-Since");
    		long lastModified = resourceAttributes.getLastModified();
    		
    		if (headerValue != -1) {
    			 // If an If-None-Match header has been specified, if modified since
                 // is ignored.
    			if ((request.getHeader("If-None-Match") == null)
                        && (lastModified < headerValue + 1000)) {
    				
    				// The entity has not been modified since the date
                    // specified by the client. This is not an error case.
    				
    				//response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    			}
    		}
    		
    	}catch (IllegalArgumentException illegalArgument) {
            return true;
        }
    	
    	return true;
    	
    }
    
    
    
    /**
     * Check if the if-none-match condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    protected boolean checkIfNoneMatch(HttpServletRequest request,
                                     HttpServletResponse response,
                                     ResourceAttributes resourceAttributes)
        throws IOException {
    	
    	String eTag = resourceAttributes.getETag();
    	String headerValue = request.getHeader("If-None-Match");
    	
    	if (headerValue != null) {
    		
    		boolean conditionSatisfied = false;
    		
    		if (!headerValue.equals("*")) {
    			
    			StringTokenizer commaTokenizer =
                    new StringTokenizer(headerValue, ",");
    			
    			while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
    				 String currentToken = commaTokenizer.nextToken();
    				 if (currentToken.trim().equals(eTag))
                         conditionSatisfied = true;
    			}
    		}
    		else {
                conditionSatisfied = true;
            }
    		
    		
    		if (conditionSatisfied) {
    			
    			// For GET and HEAD, we should respond with
                // 304 Not Modified.
                // For every other method, 412 Precondition Failed is sent
                // back.
    			
    			if ( ("GET".equals(request.getMethod()))
                        || ("HEAD".equals(request.getMethod())) ) {
    				
    				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    response.setHeader("ETag", eTag);
                    
                    return false;
    			}
    			else {
    				response.sendError
                    	(HttpServletResponse.SC_PRECONDITION_FAILED);
    				return false;
    			}
    		}
    	}
    	
    	 return true;
    }
    
    
    
    /**
     * Check if the if-unmodified-since condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    protected boolean checkIfUnmodifiedSince(HttpServletRequest request,
                                           HttpServletResponse response,
                                           ResourceAttributes resourceAttributes)
        throws IOException {
    	
    	try {
    		long lastModified = resourceAttributes.getLastModified();
            long headerValue = request.getDateHeader("If-Unmodified-Since");
            
            if (headerValue != -1) {
            	if ( lastModified >= (headerValue + 1000)) {
                    // The entity has not been modified since the date
                    // specified by the client. This is not an error case.
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }
            }
    	}catch(IllegalArgumentException illegalArgument) {
            return true;
        }
        return true;
    	
    }
    
    
    
    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    protected ArrayList parseRange(HttpServletRequest request,
                                HttpServletResponse response,
                                ResourceAttributes resourceAttributes)
        throws IOException {
    	
    	// Checking If-Range
    	String headerValue = request.getHeader("If-Range");
    	if (headerValue != null) {
    		// implements latter.
    	}
    	
    	
    	long fileLength = resourceAttributes.getContentLength();
    	
    	if (fileLength == 0)
            return null;
    	
    	// Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader("Range");
        
        if (rangeHeader == null)
            return null;
        
        /// implements latter.
    	
        return null;
    }

	
	
}
