package javax.servlet;

import java.io.IOException;
import java.util.Enumeration;

public abstract class GenericServlet 
	implements Servlet, ServletConfig, java.io.Serializable{

	private transient ServletConfig config;
	
	/**
    *
    * Does nothing. All of the servlet initialization
    * is done by one of the <code>init</code> methods.
    *
    */

   public GenericServlet() { }
   
   
   
  /**
    * Called by the servlet container to indicate to a servlet that the
    * servlet is being taken out of service.  See {@link Servlet#destroy}.
    *
    * 
    */

   public void destroy() {
   }
   
   /**
    * Returns a <code>String</code> containing the value of the named
    * initialization parameter, or <code>null</code> if the parameter does
    * not exist.  See {@link ServletConfig#getInitParameter}.
    *
    * <p>This method is supplied for convenience. It gets the 
    * value of the named parameter from the servlet's 
    * <code>ServletConfig</code> object.
    *
    * @param name 		a <code>String</code> specifying the name 
    *				of the initialization parameter
    *
    * @return String 		a <code>String</code> containing the value
    *				of the initialization parameter
    *
    */ 

   public String getInitParameter(String name) {
	return getServletConfig().getInitParameter(name);
   }
   
   
   /**
    * Returns the names of the servlet's initialization parameters 
    * as an <code>Enumeration</code> of <code>String</code> objects,
    * or an empty <code>Enumeration</code> if the servlet has no
    * initialization parameters.  See {@link
    * ServletConfig#getInitParameterNames}.
    *
    * <p>This method is supplied for convenience. It gets the 
    * parameter names from the servlet's <code>ServletConfig</code> object. 
    *
    *
    * @return Enumeration 	an enumeration of <code>String</code>
    *				objects containing the names of 
    *				the servlet's initialization parameters
    *
    */

    public Enumeration getInitParameterNames() {
	return getServletConfig().getInitParameterNames();
    }   
    
    
    /**
     * Returns this servlet's {@link ServletConfig} object.
     *
     * @return ServletConfig 	the <code>ServletConfig</code> object
     *				that initialized this servlet
     *
     */
    
    public ServletConfig getServletConfig() {
	return config;
    }
    
    
    /**
     * Returns a reference to the {@link ServletContext} in which this servlet
     * is running.  See {@link ServletConfig#getServletContext}.
     *
     * <p>This method is supplied for convenience. It gets the 
     * context from the servlet's <code>ServletConfig</code> object.
     *
     *
     * @return ServletContext 	the <code>ServletContext</code> object
     *				passed to this servlet by the <code>init</code>
     *				method
     *
     */

    public ServletContext getServletContext() {
	return getServletConfig().getServletContext();
    }
    
    
    /**
     * Returns information about the servlet, such as 
     * author, version, and copyright. 
     * By default, this method returns an empty string.  Override this method
     * to have it return a meaningful value.  See {@link
     * Servlet#getServletInfo}.
     *
     *
     * @return String 		information about this servlet, by default an
     * 				empty string
     *
     */
    
    public String getServletInfo() {
	return "";
    }
    
    
    
    /**
    *
    * Called by the servlet container to indicate to a servlet that the
    * servlet is being placed into service.  See {@link Servlet#init}.
    *
    * <p>This implementation stores the {@link ServletConfig}
    * object it receives from the servlet container for later use.
    * When overriding this form of the method, call 
    * <code>super.init(config)</code>.
    *
    * @param config 			the <code>ServletConfig</code> object
    *					that contains configutation
    *					information for this servlet
    *
    * @exception ServletException 	if an exception occurs that
    *					interrupts the servlet's normal
    *					operation
    *
    * 
    * @see 				UnavailableException
    *
    */

   public void init(ServletConfig config) throws ServletException {
	this.config = config;
	this.init();
   }
   
   
   /**
   *
   * A convenience method which can be overridden so that there's no need
   * to call <code>super.init(config)</code>.
   *
   * <p>Instead of overriding {@link #init(ServletConfig)}, simply override
   * this method and it will be called by
   * <code>GenericServlet.init(ServletConfig config)</code>.
   * The <code>ServletConfig</code> object can still be retrieved via {@link
   * #getServletConfig}. 
   *
   * @exception ServletException 	if an exception occurs that
   *					interrupts the servlet's
   *					normal operation
   *
   */
  
  public void init() throws ServletException {
	  this.config = config;
	  this.init();
  }
  
  
  
  /**
   * Called by the servlet container to allow the servlet to respond to
   * a request.  See {@link Servlet#service}.
   * 
   * <p>This method is declared abstract so subclasses, such as 
   * <code>HttpServlet</code>, must override it.
   *
   *
   *
   * @param req 	the <code>ServletRequest</code> object
   *			that contains the client's request
   *
   * @param res 	the <code>ServletResponse</code> object
   *			that will contain the servlet's response
   *
   * @exception ServletException 	if an exception occurs that
   *					interferes with the servlet's
   *					normal operation occurred
   *
   * @exception IOException 		if an input or output
   *					exception occurs
   *
   */

  public abstract void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException;
  
  /**
   * Returns the name of this servlet instance.
   * See {@link ServletConfig#getServletName}.
   *
   * @return          the name of this servlet instance
   *
   *
   *
   */

  public String getServletName() {
      return config.getServletName();
  }
  
}
