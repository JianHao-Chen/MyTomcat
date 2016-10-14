package javax.servlet;

import java.util.Locale;

public interface ServletRequest {

	
	/**
    *
    * Returns the value of the named attribute as an <code>Object</code>,
    * or <code>null</code> if no attribute of the given name exists. 
    *
    * <p> Attributes can be set two ways.  The servlet container may set
    * attributes to make available custom information about a request.
    * For example, for requests made using HTTPS, the attribute
    * <code>javax.servlet.request.X509Certificate</code> can be used to
    * retrieve information on the certificate of the client.  Attributes
    * can also be set programatically using 
    * {@link ServletRequest#setAttribute}.  This allows information to be
    * embedded into a request before a {@link RequestDispatcher} call.
    *
    * <p>Attribute names should follow the same conventions as package
    * names. This specification reserves names matching <code>java.*</code>,
    * <code>javax.*</code>, and <code>sun.*</code>. 
    *
    * @param name	a <code>String</code> specifying the name of 
    *			the attribute
    *
    * @return		an <code>Object</code> containing the value 
    *			of the attribute, or <code>null</code> if
    *			the attribute does not exist
    *
    */

   public Object getAttribute(String name);
   
   
   /**
   *
   * Returns the preferred <code>Locale</code> that the client will 
   * accept content in, based on the Accept-Language header.
   * If the client request doesn't provide an Accept-Language header,
   * this method returns the default locale for the server.
   *
   *
   * @return		the preferred <code>Locale</code> for the client
   *
   */

  public Locale getLocale();
  
  
  
  /**
   * Returns the value of a request parameter as a <code>String</code>,
   * or <code>null</code> if the parameter does not exist. Request parameters
   * are extra information sent with the request.  For HTTP servlets,
   * parameters are contained in the query string or posted form data.
   *
   * <p>You should only use this method when you are sure the
   * parameter has only one value. If the parameter might have
   * more than one value, use {@link #getParameterValues}.
   *
   * <p>If you use this method with a multivalued
   * parameter, the value returned is equal to the first value
   * in the array returned by <code>getParameterValues</code>.
   *
   * <p>If the parameter data was sent in the request body, such as occurs
   * with an HTTP POST request, then reading the body directly via {@link
   * #getInputStream} or {@link #getReader} can interfere
   * with the execution of this method.
   *
   * @param name 	a <code>String</code> specifying the 
   *			name of the parameter
   *
   * @return		a <code>String</code> representing the 
   *			single value of the parameter
   *
   * @see 		#getParameterValues
   *
   */

  public String getParameter(String name);
  
  
  /**
   * Returns the name and version of the protocol the request uses
   * in the form <i>protocol/majorVersion.minorVersion</i>, for 
   * example, HTTP/1.1. For HTTP servlets, the value
   * returned is the same as the value of the CGI variable 
   * <code>SERVER_PROTOCOL</code>.
   *
   * @return		a <code>String</code> containing the protocol 
   *			name and version number
   *
   */
  
  public String getProtocol();
}
