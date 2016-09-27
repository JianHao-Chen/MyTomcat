package My.catalina.core;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import My.catalina.Context;
import My.catalina.connector.Request;
import My.catalina.connector.Response;
import My.catalina.valves.ValveBase;
import My.tomcat.util.buf.MessageBytes;

final class StandardWrapperValve extends ValveBase{

	// ----------------- Instance Variables -----------------
	
	private volatile int requestCount;
	
	/**
     * Invoke the servlet we are managing, respecting the rules regarding
     * servlet lifecycle and SingleThreadModel support.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
	@Override
	public final void invoke(Request request, Response response) throws IOException,
			ServletException {
		
		// Initialize local variables we may need
        boolean unavailable = false;
        Throwable throwable = null;
        // This should be a Request attribute...
        
        requestCount++;
        
        StandardWrapper wrapper = (StandardWrapper) getContainer();
        Servlet servlet = null;
        Context context = (Context) wrapper.getParent();
        
        // Check for the application being marked unavailable
        if (!context.getAvailable()) {
        	
        	unavailable = true;
        }

        // Check for the servlet being marked unavailable
        if (!unavailable && wrapper.isUnavailable()) {
        	
        	unavailable = true;
        }
        
        
        // Allocate a servlet instance to process this request
        try {
        	if (!unavailable) {
        		servlet = wrapper.allocate();
        	}
        }catch (UnavailableException e) {
        	
        }
        catch (ServletException e) {
        	
        }
        catch (Throwable e) {
        	
        }
        
        
        
        // Acknowledge the request
        try {
        	response.sendAcknowledgement();
        }catch (IOException e) {
        	
        }catch (Throwable e) {
        	
        }
        
        
        MessageBytes requestPathMB = null;
        if (request != null) {
            requestPathMB = request.getRequestPathMB();
        }
        
        request.setAttribute
        	(ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
        			ApplicationFilterFactory.REQUEST_INTEGER);
        
        request.setAttribute
        	(ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
        			requestPathMB);
        
        // Create the filter chain for this request
        ApplicationFilterFactory factory =
            ApplicationFilterFactory.getInstance();
        // FilterChain implements latter.

        ApplicationFilterChain filterChain =
            factory.createFilterChain(request, wrapper, servlet);
        
        
        
        // Call the filter chain for this request
        // NOTE: This also calls the servlet's service() method
        try {
        	
        	if ((servlet != null) && (filterChain != null)) {
        		
        		filterChain.doFilter
                (request.getRequest(), response.getResponse());
        	}
        	
        	
        }catch (IOException e) {
        	
        }catch (UnavailableException e) {
        	
        }catch (ServletException e) {
        	
        }
        
        
        
        
	}

}
