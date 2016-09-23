package My.catalina.core;

import java.io.IOException;

import javax.servlet.ServletException;

import My.catalina.Context;
import My.catalina.connector.Request;
import My.catalina.connector.Response;
import My.catalina.valves.ValveBase;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class StandardHostValve extends ValveBase {

	private static Log log = LogFactory.getLog(StandardHostValve.class);
	
	
	
	 // ------------- Public Methods -------------
	
	/**
     * Select the appropriate child Context to process this request,
     * based on the specified request URI.  If no matching Context can
     * be found, return an appropriate HTTP error.
     */

	public final void invoke(Request request, Response response)
    	throws IOException, ServletException {
		
		// Select the Context to be used for this Request
        Context context = request.getContext();
        
        if (context == null) {
        	//response.sendError
        	return;
        }
        
        // Bind the context CL to the current thread
        if( context.getLoader() != null ) {
        	// Not started - it should check for availability first
            // This should eventually move to Engine, it's generic.
            Thread.currentThread().setContextClassLoader
                    (context.getLoader().getClassLoader());
        }
        
        // Ask this Context to process this request
        context.getPipeline().getFirst().invoke(request, response);
        
	}
	
	
}
