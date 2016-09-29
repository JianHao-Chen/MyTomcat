package My.catalina.core;


import java.io.PrintStream;
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
import javax.servlet.UnavailableException;

import My.catalina.Context;
import My.catalina.Loader;
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
     * The facade associated with this wrapper.
     */
    protected StandardWrapperFacade facade =
        new StandardWrapperFacade(this);
    
    
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
     * Return the available date/time for this servlet, in milliseconds since
     * the epoch.  If this date/time is Long.MAX_VALUE, it is considered to mean
     * that unavailability is permanent and any request for this servlet will return
     * an SC_NOT_FOUND error.  If this date/time is in the future, any request for
     * this servlet will return an SC_SERVICE_UNAVAILABLE error.  If it is zero,
     * the servlet is currently available.
     */
    public long getAvailable() {

        return (this.available);

    }
    
    
    
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
     * Return <code>true</code> if loading this servlet is allowed.
     */
    protected boolean isServletAllowed(Object servlet) {
 
        return true;
        
    }
    
    
    /**
     * Set the available date/time for this servlet, in milliseconds since the
     * epoch.  If this date/time is Long.MAX_VALUE, it is considered to mean
     * that unavailability is permanent and any request for this servlet will return
     * an SC_NOT_FOUND error. If this date/time is in the future, any request for
     * this servlet will return an SC_SERVICE_UNAVAILABLE error.
     *
     * @param available The new available date/time
     */
    public void setAvailable(long available) {
    	 long oldAvailable = this.available;
         if (available > System.currentTimeMillis())
             this.available = available;
         else
             this.available = 0L;
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
     * Process an UnavailableException, marking this servlet as unavailable
     * for the specified amount of time.
     *
     * @param unavailable The exception that occurred, or <code>null</code>
     *  to mark this servlet as permanently unavailable
     */
    public void unavailable(UnavailableException unavailable) {
    	
    	if (unavailable == null)
            setAvailable(Long.MAX_VALUE);
    	else if (unavailable.isPermanent())
            setAvailable(Long.MAX_VALUE);
    	 else {
    		 int unavailableSeconds = unavailable.getUnavailableSeconds();
             if (unavailableSeconds <= 0)
                 unavailableSeconds = 60;        // Arbitrary default
             setAvailable(System.currentTimeMillis() +
                          (unavailableSeconds * 1000L));
    	 }
    }
    
    
    
    /**
     * Load and initialize an instance of this servlet, if there is not already
     * at least one initialized instance.  This can be used, for example, to
     * load servlets that are marked in the deployment descriptor to be loaded
     * at server startup time.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  Servlets whose classnames begin with
     * <code>org.apache.catalina.</code> (so-called "container" servlets)
     * are loaded by the same classloader that loaded this class, rather than
     * the classloader for the current web application.
     * This gives such classes access to Catalina internals, which are
     * prevented for classes loaded for web applications.
     *
     * @exception ServletException if the servlet init() method threw
     *  an exception
     * @exception ServletException if some other loading problem occurs
     */
    public synchronized void load() throws ServletException {
        instance = loadServlet();
    }
    
    
    /**
     * Load and initialize an instance of this servlet, if there is not already
     * at least one initialized instance.  This can be used, for example, to
     * load servlets that are marked in the deployment descriptor to be loaded
     * at server startup time.
     */
    public synchronized Servlet loadServlet() throws ServletException {
    	
    	 // Nothing to do if we already have an instance or an instance pool
        if (!singleThreadModel && (instance != null))
            return instance;
        
        PrintStream out = System.out;
        
        Servlet servlet = null;
        try {
        	
        	String actualClass = servletClass;
        	
        	// handle JSP file here...
        	
        	// Complain if no servlet class has been specified
            if (actualClass == null) {
            	unavailable(null);
            	throw new ServletException("standardWrapper.notClass");
            }
            
            // Acquire an instance of the class loader to be used
            Loader loader = getLoader();
            if (loader == null) {
            	unavailable(null);
            	throw new ServletException("standardWrapper.missingLoader");
            }
            
            ClassLoader classLoader = loader.getClassLoader();
            
            // Special case class loader for a container provided servlet
            if (isContainerProvidedServlet(actualClass) && 
                    ! ((Context)getParent()).getPrivileged() ) {
            	
            	classLoader = this.getClass().getClassLoader();
            }
            
            // Load the specified servlet class from the appropriate class loader
            Class classClass = null;
            try {
            	if (classLoader != null) {
            		classClass = classLoader.loadClass(actualClass);
            	}else {
                    classClass = Class.forName(actualClass);
                }
            }catch (ClassNotFoundException e) {
            	unavailable(null);
                throw new ServletException("standardWrapper.missingClass");
            }
            
            
            // Instantiate and initialize an instance of the servlet class itself
            try {
            	servlet = (Servlet) classClass.newInstance();
            }
            catch (ClassCastException e) {
            	unavailable(null);
                // Restore the context ClassLoader
                throw new ServletException("standardWrapper.notServlet");
            }
            catch (Throwable e) {
            	
            }
            
            
            // Check if loading the servlet in this web application 
            // should be allowed
            if (!isServletAllowed(servlet)) {
            	throw new SecurityException("standardWrapper.privilegedServlet");
            }
            
            // Call the initialization method of this servlet
            try {
            	servlet.init(facade);
            }catch (UnavailableException f) {
            	
            }catch (ServletException f) {
            	
            }catch (Throwable f) {
            	
            }
            
        }finally {
        	
        }
        
        return servlet;
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
    
    
    /**
     * Return this previously allocated servlet to the pool of available
     * instances.  If this servlet class does not implement SingleThreadModel,
     * no action is actually required.
     *
     * @param servlet The servlet to be returned
     *
     * @exception ServletException if a deallocation error occurs
     */
    public void deallocate(Servlet servlet) throws ServletException {
    	
    	// If not SingleThreadModel, no action is required
        if (!singleThreadModel) {
            countAllocated.decrementAndGet();
            return;
        }
    	
    }
    
    
    
	public String getServletName() {
		return (getName());
	}
	

	public ServletContext getServletContext() {
		 if (parent == null)
	     	return (null);
	     else if (!(parent instanceof Context))
	     	return (null);
	     else
	     	return (((Context) parent).getServletContext());
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
	
	
	/**
     * Return <code>true</code> if the specified class name represents a
     * container provided servlet class that should be loaded by the
     * server class loader.
     *
     * @param classname Name of the class to be checked
     */
    protected boolean isContainerProvidedServlet(String classname) {
    	if (classname.startsWith("My.catalina.")) {
            return (true);
        }
    	return false;
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
