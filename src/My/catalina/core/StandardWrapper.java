package My.catalina.core;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import My.catalina.Wrapper;
import My.tomcat.util.modeler.Registry;

/**
 * Standard implementation of the <b>Wrapper</b> interface that represents
 * an individual servlet definition.  No child Containers are allowed, and
 * the parent Container must be a Context.
 */

public class StandardWrapper extends ContainerBase
	implements ServletConfig, Wrapper{

	// ---------------------- Constructors ----------------------
	
	/**
     * Create a new StandardWrapper component with the default basic Valve.
     */
    public StandardWrapper() {
    	
    	super();
    	swValve=new StandardWrapperValve();
    	pipeline.setBasic(swValve);
        broadcaster = new NotificationBroadcasterSupport();
    }
	
	
	
	// --------------- Instance Variables --------------- 
	
    /**
     * The date and time at which this servlet will become available (in
     * milliseconds since the epoch), or zero if the servlet is available.
     * If this value equals Long.MAX_VALUE, the unavailability of this
     * servlet is considered permanent.
     */
    protected long available = 0L;
    
    
    /**
     * Are we unloading our servlet instance at the moment?
     */
    protected boolean unloading = false;
    
    
    /**
     * Does this servlet implement the SingleThreadModel interface?
     */
    protected boolean singleThreadModel = false;
    
    
    /**
     * The (single) initialized instance of this servlet.
     */
    protected Servlet instance = null;
    
    
    /**
     * The count of allocations that are currently active (even if they
     * are for the same instance, as will be true on a non-STM servlet).
     */
    protected AtomicInteger countAllocated = new AtomicInteger(0);
    
    
	/**
     * The fully qualified servlet class name for this servlet.
     */
    protected String servletClass = null;
	
    
    /**
     * True if this StandardWrapper is for the JspServlet
     */
    protected boolean isJspServlet;
    
    /**
     * The broadcaster that sends j2ee notifications. 
     */
    protected NotificationBroadcasterSupport broadcaster = null;
    
    protected StandardWrapperValve swValve;
    
    /**
     * The notification sequence number.
     */
    protected long sequenceNumber = 0;
    
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
    
    
    /**
     * Return the mappings associated with this wrapper.
     */
    public String[] findMappings() {

        synchronized (mappings) {
            return (String[]) mappings.toArray(new String[mappings.size()]);
        }

    }
    
    
    /**
     * FIXME: Fooling introspection ...
     */
    public Wrapper findMappingObject() {
        return (Wrapper) getMappingObject();
    }
    
    
    
    /**
     * Is this servlet currently unavailable?
     */
    public boolean isUnavailable() {

        if (available == 0L)
            return (false);
        else if (available <= System.currentTimeMillis()) {
            available = 0L;
            return (false);
        } else
            return (true);

    }
    
    
    
    /**
     * Allocate an initialized instance of this Servlet that is ready to have
     * its <code>service()</code> method called.  If the servlet class does
     * not implement <code>SingleThreadModel</code>, the (only) initialized
     * instance may be returned immediately.  If the servlet class implements
     * <code>SingleThreadModel</code>, the Wrapper implementation must ensure
     * that this instance is not allocated again until it is deallocated by a
     * call to <code>deallocate()</code>.
     *
     * @exception ServletException if the servlet init() method threw
     *  an exception
     * @exception ServletException if a loading error occurs
     */
    public Servlet allocate() throws ServletException {
    	
    	// If we are currently unloading this servlet, throw an exception
        if (unloading)
            throw new ServletException("standardWrapper.unloading");
        
        boolean newInstance = false;
        
        // If not SingleThreadedModel, return the same 
        // instance every time
        if (!singleThreadModel) {
        	
        	// Load and initialize our instance if necessary
        	
        	
        	
        	
        	
        	if (!singleThreadModel) {
            	
            	if (!newInstance) {
            		countAllocated.incrementAndGet();
            	}
            	return (instance);
            }
        }
        
        return null;
        
        
        
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
		
		String parentName = ctx.getName();
		parentName = ("".equals(parentName)) ? "/" : parentName;
		
		String hostName = ctx.getParent().getName();
        hostName = (hostName==null) ? "DEFAULT" : hostName;
        
        String domain = ctx.getDomain();
        
        String webMod= "//" + hostName + parentName;
        String onameStr = domain + ":j2eeType=Servlet,name=" + getName() +
        						",WebModule=" + webMod + ",J2EEApplication=" +
        						ctx.getJ2EEApplication() + ",J2EEServer=" +
        						ctx.getJ2EEServer();
        
        try {
        	oname = new ObjectName(onameStr);
        	controller = oname;
        	Registry.getRegistry(null, null)
            		.registerComponent(this, oname, null );
        	
        	// Send j2ee.object.created notification 
            if (this.getObjectName() != null) {
                Notification notification = new Notification(
                                                "j2ee.object.created", 
                                                this.getObjectName(), 
                                                sequenceNumber++);
                broadcaster.sendNotification(notification);
            }
        }catch( Exception ex ) {
        	
        }
		
	}

}
