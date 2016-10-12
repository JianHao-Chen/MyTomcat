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
}
