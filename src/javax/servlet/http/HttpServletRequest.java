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

	/**
    *
    * Returns the name of the HTTP method with which this 
    * request was made, for example, GET, POST, or PUT.
    * Same as the value of the CGI variable REQUEST_METHOD.
    *
    * @return			a <code>String</code> 
    *				specifying the name
    *				of the method with which
    *				this request was made
    *
    */

   public String getMethod();
   
   
   /**
   *
   * Returns any extra path information associated with
   * the URL the client sent when it made this request.
   * The extra path information follows the servlet path
   * but precedes the query string and will start with
   * a "/" character.
   *
   * <p>This method returns <code>null</code> if there
   * was no extra path information.
   *
   * <p>Same as the value of the CGI variable PATH_INFO.
   *
   *
   * @return		a <code>String</code>, decoded by the
   *			web container, specifying 
   *			extra path information that comes
   *			after the servlet path but before
   *			the query string in the request URL;
   *			or <code>null</code> if the URL does not have
   *			any extra path information
   *
   */
   
  public String getPathInfo();
  
  /**
  *
  * Returns the part of this request's URL that calls
  * the servlet. This path starts with a "/" character
  * and includes either the servlet name or a path to
  * the servlet, but does not include any extra path
  * information or a query string. Same as the value of
  * the CGI variable SCRIPT_NAME.
  *
  * <p>This method will return an empty string ("") if the
  * servlet used to process this request was matched using
  * the "/*" pattern.
  *
  * @return		a <code>String</code> containing
  *			the name or path of the servlet being
  *			called, as specified in the request URL,
  *			decoded, or an empty string if the servlet
  *			used to process the request is matched
  *			using the "/*" pattern.
  *
  */

 public String getServletPath();
 
}
