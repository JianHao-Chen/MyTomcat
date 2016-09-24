package My.catalina.core;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import My.catalina.Wrapper;

/**
 * Standard implementation of the <b>Wrapper</b> interface that represents
 * an individual servlet definition.  No child Containers are allowed, and
 * the parent Container must be a Context.
 */

public class StandardWrapper extends ContainerBase
	implements ServletConfig, Wrapper{

	// --------------- Instance Variables --------------- 
	
	/**
     * The fully qualified servlet class name for this servlet.
     */
    protected String servletClass = null;
	
    
    /**
     * True if this StandardWrapper is for the JspServlet
     */
    protected boolean isJspServlet;
    
    
    /**
     * The initialization parameters for this servlet, keyed by
     * parameter name.
     */
    protected HashMap parameters = new HashMap();
    
    /**
     * The load-on-startup order value (negative value means load on
     * first call) for this servlet.
     */
    protected int loadOnStartup = -1;
    
    
    /**
     * Mappings associated with the wrapper.
     */
    protected ArrayList mappings = new ArrayList();
    
	// ---------------------- Properties ----------------------
    
    /**
     * Return the fully qualified servlet class name for this servlet.
     */
    public String getServletClass() {

        return (this.servletClass);

    }


    /**
     * Set the fully qualified servlet class name for this servlet.
     *
     * @param servletClass Servlet class name
     */
    public void setServletClass(String servletClass) {

        String oldServletClass = this.servletClass;
        this.servletClass = servletClass;
        
        if (Constants.JSP_SERVLET_CLASS.equals(servletClass)) {
            isJspServlet = true;
        }
    }
    
    
    
    /**
     * Add a new servlet initialization parameter for this servlet.
     *
     * @param name Name of this initialization parameter to add
     * @param value Value of this initialization parameter to add
     */
    public void addInitParameter(String name, String value) {

        synchronized (parameters) {
            parameters.put(name, value);
        }
        fireContainerEvent("addInitParameter", name);

    }
    
    
    
    /**
     * Return the load-on-startup order value (negative value means
     * load on first call).
     */
    public int getLoadOnStartup() {

        if (isJspServlet && loadOnStartup < 0) {
            /*
             * JspServlet must always be preloaded, because its instance is
             * used during registerJMX (when registering the JSP
             * monitoring mbean)
             */
             return Integer.MAX_VALUE;
        } else {
            return (this.loadOnStartup);
        }
    }


    /**
     * Set the load-on-startup order value (negative value means
     * load on first call).
     *
     * @param value New load-on-startup value
     */
    public void setLoadOnStartup(int value) {

        int oldLoadOnStartup = this.loadOnStartup;
        this.loadOnStartup = value;
    }
    
    
    /**
     * Set the load-on-startup order value from a (possibly null) string.
     * Per the specification, any missing or non-numeric value is converted
     * to a zero, so that this servlet will still be loaded at startup
     * time, but in an arbitrary order.
     *
     * @param value New load-on-startup value
     */
    public void setLoadOnStartupString(String value) {

        try {
            setLoadOnStartup(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            setLoadOnStartup(0);
        }
    }

    public String getLoadOnStartupString() {
        return Integer.toString( getLoadOnStartup());
    }
    
    
    
    /**
     * Add a mapping associated with the Wrapper.
     *
     * @param mapping The new wrapper mapping
     */
    public void addMapping(String mapping) {

        synchronized (mappings) {
            mappings.add(mapping);
        }
        fireContainerEvent("addMapping", mapping);

    }
    
    
    /**
     * Remove a mapping associated with the wrapper.
     *
     * @param mapping The pattern to remove
     */
    public void removeMapping(String mapping) {

        synchronized (mappings) {
            mappings.remove(mapping);
        }
        fireContainerEvent("removeMapping", mapping);

    }
    
    
	
	@Override
	public String getServletName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	protected void registerJMX(StandardContext ctx) {
		
	}

}
