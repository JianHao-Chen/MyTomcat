package javax.servlet;



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
}
