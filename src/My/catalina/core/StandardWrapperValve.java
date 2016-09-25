package My.catalina.core;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import My.catalina.Context;
import My.catalina.connector.Request;
import My.catalina.connector.Response;
import My.catalina.valves.ValveBase;

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
        
        
        
	}

}
