package javax.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;



/**
 * 
 * Defines a set of methods that a servlet uses to communicate with its
 * servlet container, for example, to get the MIME type of a file, dispatch
 * requests, or write to a log file.
 *
 * <p>There is one context per "web application" per Java Virtual Machine.  (A
 * "web application" is a collection of servlets and content installed under a
 * specific subset of the server's URL namespace such as <code>/catalog</code>
 * and possibly installed via a <code>.war</code> file.) 
 *
 * <p>In the case of a web
 * application marked "distributed" in its deployment descriptor, there will
 * be one context instance for each virtual machine.  In this situation, the 
 * context cannot be used as a location to share global information (because
 * the information won't be truly global).  Use an external resource like 
 * a database instead.
 *
 * <p>The <code>ServletContext</code> object is contained within 
 * the {@link ServletConfig} object, which the Web server provides the
 * servlet when the servlet is initialized.
 */

public interface ServletContext {

	
	
	
	/**
     * Returns the servlet container attribute with the given name, 
     * or <code>null</code> if there is no attribute by that name.
     * An attribute allows a servlet container to give the
     * servlet additional information not
     * already provided by this interface. See your
     * server documentation for information about its attributes.
     * A list of supported attributes can be retrieved using
     * <code>getAttributeNames</code>.
     *
     * <p>The attribute is returned as a <code>java.lang.Object</code>
     * or some subclass.
     * Attribute names should follow the same convention as package
     * names. The Java Servlet API specification reserves names
     * matching <code>java.*</code>, <code>javax.*</code>,
     * and <code>sun.*</code>.
     *
     *
     * @param name 	a <code>String</code> specifying the name 
     *			of the attribute
     *
     * @return 		an <code>Object</code> containing the value 
     *			of the attribute, or <code>null</code>
     *			if no attribute exists matching the given
     *			name
     *
     * @see 		ServletContext#getAttributeNames
     *
     */
  
    public Object getAttribute(String name);
    
    
    /**
    *
    * Binds an object to a given attribute name in this servlet context. If
    * the name specified is already used for an attribute, this
    * method will replace the attribute with the new to the new attribute.
    * <p>If listeners are configured on the <code>ServletContext</code> the  
    * container notifies them accordingly.
    * <p>
    * If a null value is passed, the effect is the same as calling 
    * <code>removeAttribute()</code>.
    * 
    * <p>Attribute names should follow the same convention as package
    * names. The Java Servlet API specification reserves names
    * matching <code>java.*</code>, <code>javax.*</code>, and
    * <code>sun.*</code>.
    *
    *
    * @param name 	a <code>String</code> specifying the name 
    *			of the attribute
    *
    * @param object 	an <code>Object</code> representing the
    *			attribute to be bound
    *
    *
    *
    */
   
   public void setAttribute(String name, Object object);
   
   
   
   /**
    * Returns a <code>String</code> containing the real path 
    * for a given virtual path. For example, the path "/index.html"
    * returns the absolute file path on the server's filesystem would be
    * served by a request for "http://host/contextPath/index.html",
    * where contextPath is the context path of this ServletContext..
    *
    * <p>The real path returned will be in a form
    * appropriate to the computer and operating system on
    * which the servlet container is running, including the
    * proper path separators. This method returns <code>null</code>
    * if the servlet container cannot translate the virtual path
    * to a real path for any reason (such as when the content is
    * being made available from a <code>.war</code> archive).
    *
    *
    * @param path 	a <code>String</code> specifying a virtual path
    *
    *
    * @return 		a <code>String</code> specifying the real path,
    *                  or null if the translation cannot be performed
    *			
    *
    */

   public String getRealPath(String path);
   
   
   
   /**
    * Returns the MIME type of the specified file, or <code>null</code> if 
    * the MIME type is not known. The MIME type is determined
    * by the configuration of the servlet container, and may be specified
    * in a web application deployment descriptor. Common MIME
    * types are <code>"text/html"</code> and <code>"image/gif"</code>.
    *
    *
    * @param   file    a <code>String</code> specifying the name
    *			of a file
    *
    * @return 		a <code>String</code> specifying the file's MIME type
    *
    */

   public String getMimeType(String file);
   
   
   /**
    * Returns the resource located at the named path as
    * an <code>InputStream</code> object.
    *
    * <p>The data in the <code>InputStream</code> can be 
    * of any type or length. The path must be specified according
    * to the rules given in <code>getResource</code>.
    * This method returns <code>null</code> if no resource exists at
    * the specified path. 
    * 
    * <p>Meta-information such as content length and content type
    * that is available via <code>getResource</code>
    * method is lost when using this method.
    *
    * <p>The servlet container must implement the URL handlers
    * and <code>URLConnection</code> objects necessary to access
    * the resource.
    *
    * <p>This method is different from 
    * <code>java.lang.Class.getResourceAsStream</code>,
    * which uses a class loader. This method allows servlet containers 
    * to make a resource available
    * to a servlet from any location, without using a class loader.
    * 
    *
    * @param path 	a <code>String</code> specifying the path
    *			to the resource
    *
    * @return 		the <code>InputStream</code> returned to the 
    *			servlet, or <code>null</code> if no resource
    *			exists at the specified path 
    *
    *
    */

   public InputStream getResourceAsStream(String path);
   
   
   /**
    * Returns a URL to the resource that is mapped to a specified
    * path. The path must begin with a "/" and is interpreted
    * as relative to the current context root.
    *
    * <p>This method allows the servlet container to make a resource 
    * available to servlets from any source. Resources 
    * can be located on a local or remote
    * file system, in a database, or in a <code>.war</code> file. 
    *
    * <p>The servlet container must implement the URL handlers
    * and <code>URLConnection</code> objects that are necessary
    * to access the resource.
    *
    * <p>This method returns <code>null</code>
    * if no resource is mapped to the pathname.
    *
    * <p>Some containers may allow writing to the URL returned by
    * this method using the methods of the URL class.
    *
    * <p>The resource content is returned directly, so be aware that 
    * requesting a <code>.jsp</code> page returns the JSP source code.
    * Use a <code>RequestDispatcher</code> instead to include results of 
    * an execution.
    *
    * <p>This method has a different purpose than
    * <code>java.lang.Class.getResource</code>,
    * which looks up resources based on a class loader. This
    * method does not use class loaders.
    * 
    * @param path 				a <code>String</code> specifying
    *						the path to the resource
    *
    * @return 					the resource located at the named path,
    * 						or <code>null</code> if there is no resource
    *						at that path
    *
    * @exception MalformedURLException 	if the pathname is not given in 
    * 						the correct form
    *
    */
   
   public URL getResource(String path) throws MalformedURLException;
   
}
