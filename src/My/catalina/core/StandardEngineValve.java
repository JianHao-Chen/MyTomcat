package My.catalina.core;

import java.io.IOException;

import javax.servlet.ServletException;

import My.catalina.connector.Request;
import My.catalina.connector.Response;
import My.catalina.valves.ValveBase;

/**
 * Valve that implements the default basic behavior for the
 * <code>StandardEngine</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 */

public final class StandardEngineValve extends ValveBase {
	
	// --------------------------------------------------------- Public Methods
	/**
     * Select the appropriate child Host to process this request,
     * based on the requested server name.  If no matching Host can
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
}
