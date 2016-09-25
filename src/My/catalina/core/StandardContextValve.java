package My.catalina.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import My.catalina.CometEvent;
import My.catalina.Container;
import My.catalina.Wrapper;
import My.catalina.connector.Request;
import My.catalina.connector.Response;
import My.catalina.valves.ValveBase;
import My.tomcat.util.buf.MessageBytes;

public class StandardContextValve extends ValveBase {

	private StandardContext context = null;
	
	// ---------------------- Properties ----------------------
	
	
	// --------------------- Public Methods ---------------------
	
	 /**
     * Cast to a StandardContext right away, as it will be needed later.
     * 
     * @see org.apache.catalina.Contained#setContainer(org.apache.catalina.Container)
     */
    public void setContainer(Container container) {
        super.setContainer(container);
        context = (StandardContext) container;
    }
    
    
    /**
     * Select the appropriate child Wrapper to process this request,
     * based on the specified request URI.  If no matching Wrapper can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public final void invoke(Request request, Response response)
        throws IOException, ServletException {
    	
    	// Disallow any direct access to resources under WEB-INF or META-INF
    	MessageBytes requestPathMB = request.getRequestPathMB();
    	if ((requestPathMB.startsWithIgnoreCase("/META-INF/", 0))
                || (requestPathMB.equalsIgnoreCase("/META-INF"))
                || (requestPathMB.startsWithIgnoreCase("/WEB-INF/", 0))
                || (requestPathMB.equalsIgnoreCase("/WEB-INF"))) {
    		notFound(response);
            return;
    	}
    	
    	
    	// Wait if we are reloading
        boolean reloaded = false;
        while (context.getPaused()) {
        	reloaded = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                ;
            }
        }
    	
    	
    	// Reloading will have stopped the old webappclassloader and
        // created a new one
        if (reloaded &&
        		context.getLoader() != null &&
                context.getLoader().getClassLoader() != null) {
        	Thread.currentThread().setContextClassLoader(
                    context.getLoader().getClassLoader());
        }
        
        
        // Select the Wrapper to be used for this Request
        Wrapper wrapper = request.getWrapper();
    	
        if (wrapper == null) {
        	notFound(response);
        }
        else if (wrapper.isUnavailable()) {
        	// May be as a result of a reload, try and find the new wrapper
        	wrapper = (Wrapper) container.findChild(wrapper.getName());
            if (wrapper == null) {
                notFound(response);
                return;
            }
        }
        
        // Normal request processing
        // listener ...
        
        
        wrapper.getPipeline().getFirst().invoke(request, response);
        
    }
    
    
    
    /**
     * Select the appropriate child Wrapper to process this request,
     * based on the specified request URI.  If no matching Wrapper can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public final void event(Request request, Response response, CometEvent event)
        throws IOException, ServletException {
    	
    }
    
    
	// ----------------- Private Methods -----------------
    
    /**
     * Report a "not found" error for the specified resource.  FIXME:  We
     * should really be using the error reporting settings for this web
     * application, but currently that code runs at the wrapper level rather
     * than the context level.
     *
     * @param response The response we are creating
     */
    private void notFound(HttpServletResponse response) {
    	
    }
    
    
}
