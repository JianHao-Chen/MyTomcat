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
 
 
 /**
 *
 * Returns the value of the specified request header
 * as a <code>String</code>. If the request did not include a header
 * of the specified name, this method returns <code>null</code>.
 * If there are multiple headers with the same name, this method
 * returns the first head in the request.
 * The header name is case insensitive. You can use
 * this method with any request header.
 *
 * @param name		a <code>String</code> specifying the
 *				header name
 *
 * @return			a <code>String</code> containing the
 *				value of the requested
 *				header, or <code>null</code>
 *				if the request does not
 *				have a header of that name
 *
 */			

public String getHeader(String name); 



/**
*
* Returns the value of the specified request header
* as a <code>long</code> value that represents a 
* <code>Date</code> object. Use this method with
* headers that contain dates, such as
* <code>If-Modified-Since</code>. 
*
* <p>The date is returned as
* the number of milliseconds since January 1, 1970 GMT.
* The header name is case insensitive.
*
* <p>If the request did not have a header of the
* specified name, this method returns -1. If the header
* can't be converted to a date, the method throws
* an <code>IllegalArgumentException</code>.
*
* @param name		a <code>String</code> specifying the
*				name of the header
*
* @return			a <code>long</code> value
*				representing the date specified
*				in the header expressed as
*				the number of milliseconds
*				since January 1, 1970 GMT,
*				or -1 if the named header
*				was not included with the
*				request
*
* @exception	IllegalArgumentException	If the header value
*							can't be converted
*							to a date
*
*/

public long getDateHeader(String name);


/**
*
* Returns an array containing all of the <code>Cookie</code>
* objects the client sent with this request.
* This method returns <code>null</code> if no cookies were sent.
*
* @return		an array of all the <code>Cookies</code>
*			included with this request, or <code>null</code>
*			if the request has no cookies
*
*
*/

public Cookie[] getCookies();



/**
*
* Returns the current <code>HttpSession</code>
* associated with this request or, if there is no
* current session and <code>create</code> is true, returns 
* a new session.
*
* <p>If <code>create</code> is <code>false</code>
* and the request has no valid <code>HttpSession</code>,
* this method returns <code>null</code>.
*
* <p>To make sure the session is properly maintained,
* you must call this method before 
* the response is committed. If the container is using cookies
* to maintain session integrity and is asked to create a new session
* when the response is committed, an IllegalStateException is thrown.
*
*
*
*
* @param create	<code>true</code> to create
*			a new session for this request if necessary; 
*			<code>false</code> to return <code>null</code>
*			if there's no current session
*			
*
* @return 		the <code>HttpSession</code> associated 
*			with this request or <code>null</code> if
* 			<code>create</code> is <code>false</code>
*			and the request has no valid session
*
* @see	#getSession()
*
*
*/

public HttpSession getSession(boolean create);


/**
*
* Returns the current session associated with this request,
* or if the request does not have a session, creates one.
* 
* @return		the <code>HttpSession</code> associated
*			with this request
*
* @see	#getSession(boolean)
*
*/

public HttpSession getSession();
 
}
