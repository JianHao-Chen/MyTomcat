package My.catalina.core;

import java.io.IOException;

import javax.servlet.ServletException;

import My.catalina.CometEvent;
import My.catalina.Container;
import My.catalina.connector.Request;
import My.catalina.connector.Response;
import My.catalina.valves.ValveBase;

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
}
