package javax.servlet.http;

import javax.servlet.ServletRequest;

/**
*
* Extends the {@link javax.servlet.ServletRequest} interface
* to provide request information for HTTP servlets. 
*
* <p>The servlet container creates an <code>HttpServletRequest</code> 
* object and passes it as an argument to the servlet's service
* methods (<code>doGet</code>, <code>doPost</code>, etc).
*/

public interface HttpServletRequest extends ServletRequest{

}
