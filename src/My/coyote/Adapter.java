package My.coyote;

import My.tomcat.util.net.SocketStatus;

/**
 * Adapter. This represents the entry point in a 
 * coyote-based servlet container.
 */

public interface Adapter {

	/** 
     * Call the service method, and notify all listeners
     *
     * @exception Exception if an error happens during handling of
     *   the request. Common errors are:
     *   <ul><li>IOException if an input/output error occurs and we are
     *   processing an included servlet (otherwise it is swallowed and
     *   handled by the top level error handler mechanism)
     *       <li>ServletException if a servlet throws an exception and
     *  we are processing an included servlet (otherwise it is swallowed
     *  and handled by the top level error handler mechanism)
     *  </ul>
     *  Tomcat should be able to handle and log any other exception ( including
     *  runtime exceptions )
     */
	
	public void service(Request req, Response res)
    throws Exception;

	public boolean event(Request req, Response res, SocketStatus status)
    throws Exception;

	public void log(Request req, Response res, long time);
}
