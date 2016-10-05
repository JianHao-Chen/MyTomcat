package My.catalina.connector;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;

import My.catalina.Context;
import My.catalina.Globals;
import My.catalina.Host;
import My.catalina.Wrapper;
import My.catalina.core.ApplicationFilterFactory;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.FastHttpDateFormat;
import My.tomcat.util.http.mapper.MappingData;

/**
 * Wrapper object for the Coyote request.
 */

public class Request implements HttpServletRequest{

	
	/**
     * The set of SimpleDateFormat formats to use in getDateHeader().
     *
     * Notice that because SimpleDateFormat is not thread-safe, we can't
     * declare formats[] as a static variable.
     */
    protected SimpleDateFormat formats[] = {
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    };
	
	
	 // -------------------- Properties -------------------- 


    /**
     * Coyote request.
     */
    protected My.coyote.Request coyoteRequest;

    /**
     * Set the Coyote request.
     * 
     * @param coyoteRequest The Coyote request
     */
    public void setCoyoteRequest(My.coyote.Request coyoteRequest) {
        this.coyoteRequest = coyoteRequest;
        inputBuffer.setRequest(coyoteRequest);
    }

    /**
     * Get the Coyote request.
     */
    public My.coyote.Request getCoyoteRequest() {
        return (this.coyoteRequest);
    }
    
    
    /**
     * Return the HTTP request method used in this Request.
     */
    public String getMethod() {
        return coyoteRequest.method().toString();
    }
    
    
    
    /**
     * Return the first value of the specified header, if any; otherwise,
     * return <code>null</code>
     *
     * @param name Name of the requested header
     */
    public String getHeader(String name) {
        return coyoteRequest.getHeader(name);
    }
    
    /**
     * Return the value of the specified date header, if any; otherwise
     * return -1.
     *
     * @param name Name of the requested date header
     *
     * @exception IllegalArgumentException if the specified header value
     *  cannot be converted to a date
     */
    public long getDateHeader(String name) {

        String value = getHeader(name);
        if (value == null)
            return (-1L);

        // Attempt to convert the date header in a variety of formats
        long result = FastHttpDateFormat.parseDate(value, formats);
        if (result != (-1L)) {
            return result;
        }
        throw new IllegalArgumentException(value);

    }
    
    
    /**
     * Return the path information associated with this Request.
     */
    public String getPathInfo() {
        return (mappingData.pathInfo.toString());
    }
    
    /**
     * Return the portion of the request URI used to select the servlet
     * that will process this request.
     */
    public String getServletPath() {
        return (mappingData.wrapperPath.toString());
    }
    
    
    
	// ---------------------- Variables ----------------------------
    
    /**
     * The associated input buffer.
     */
    protected InputBuffer inputBuffer = new InputBuffer();
    
    
	
	// --------------- Request Methods ---------------
	 /**
     * Associated Catalina connector.
     */
    protected Connector connector;

    /**
     * Return the Connector through which this Request was received.
     */
    public Connector getConnector() {
        return (this.connector);
    }

    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
    }
	
	
	
    /**
     * The response with which this request is associated.
     */
    protected My.catalina.connector.Response response = null;

    /**
     * Return the Response with which this Request is associated.
     */
    public My.catalina.connector.Response getResponse() {
        return (this.response);
    }

    /**
     * Set the Response with which this Request is associated.
     *
     * @param response The new associated response
     */
    public void setResponse(My.catalina.connector.Response response) {
        this.response = response;
    }
    
    /**
     * Associated context.
     */
    protected Context context = null;

    /**
     * Return the Context within which this Request is being processed.
     */
    public Context getContext() {
        return (this.context);
    }


    /**
     * Set the Context within which this Request is being processed.  This
     * must be called as soon as the appropriate Context is identified, because
     * it identifies the value to be returned by <code>getContextPath()</code>,
     * and thus enables parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    
    
    
    /**
     * Return the Host within which this Request is being processed.
     */
    public Host getHost() {
    	 if (getContext() == null)
             return null;
    	 return (Host)getContext().getParent();
    }
    
    
    
    /**
     * Mapping data.
     */
    protected MappingData mappingData = new MappingData();

    /**
     * Return mapping data.
     */
    public MappingData getMappingData() {
        return (mappingData);
    }
    
    
    /**
     * Get the request path.
     * 
     * @return the request path
     */
    public MessageBytes getRequestPathMB() {
        return (mappingData.requestPath);
    }
    
    
    /**
     * Associated wrapper.
     */
    protected Wrapper wrapper = null;

    /**
     * Return the Wrapper within which this Request is being processed.
     */
    public Wrapper getWrapper() {
        return (this.wrapper);
    }


    /**
     * Set the Wrapper within which this Request is being processed.  This
     * must be called as soon as the appropriate Wrapper is identified, and
     * before the Request is ultimately passed to an application servlet.
     * @param wrapper The newly associated Wrapper
     */
    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }
    
    
    
    /**
     * The current dispatcher type.
     */
    protected Object dispatcherType = null;
    
    /**
     * The current request dispatcher path.
     */
    protected Object requestDispatcherPath = null;
    
    
    /**
     * List of read only attributes for this Request.
     */
    private HashMap readOnlyAttributes = new HashMap();
    
    
    /**
     * The attributes associated with this Request, keyed by attribute name.
     */
    protected HashMap attributes = new HashMap();
    
    
    
    /**
     * Set the specified request attribute to the specified value.
     */
    public void setAttribute(String name, Object value) {
    	
    	// Name cannot be null
        if (name == null)
            throw new IllegalArgumentException("coyoteRequest.setAttribute.namenull");
        
        // Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }
        
        if (name.equals(Globals.DISPATCHER_TYPE_ATTR)) {
            dispatcherType = value;
            return;
        } else if (name.equals(Globals.DISPATCHER_REQUEST_PATH_ATTR)) {
            requestDispatcherPath = value;
            return;
        }
        
        Object oldValue = null;
        boolean replaced = false;
        
        // Add or replace the specified attribute
        // Check for read only attribute
        // requests are per thread so synchronization unnecessary
        if (readOnlyAttributes.containsKey(name)) {
            return;
        }
        
        oldValue = attributes.put(name, value);
        if (oldValue != null) {
            replaced = true;
        }
        
        
        
        
    }
    
    
    /**
     * Remove the specified request attribute if it exists.
     */
    public void removeAttribute(String name) {
    	 Object value = null;
         boolean found = false;

         // Remove the specified attribute
         // Check for read only attribute
         // requests are per thread so synchronization unnecessary
         if (readOnlyAttributes.containsKey(name)) {
             return;
         }
         
         
         found = attributes.containsKey(name);
         if (found) {
             value = attributes.get(name);
             attributes.remove(name);
         } else {
             return;
         }
    }
    
    
    
    /**
     * Return the specified request attribute if it exists; otherwise, return
     * <code>null</code>.
     *
     * @param name Name of the request attribute to return
     */
    public Object getAttribute(String name) {
    	
    	if (name.equals(Globals.DISPATCHER_TYPE_ATTR)) {
            return (dispatcherType == null) 
                ? ApplicationFilterFactory.REQUEST_INTEGER
                : dispatcherType;
        } else if (name.equals(Globals.DISPATCHER_REQUEST_PATH_ATTR)) {
            return (requestDispatcherPath == null) 
                ? getRequestPathMB().toString()
                : requestDispatcherPath.toString();
        }
    	
    	Object attr=attributes.get(name);
    	
    	return(attr);
    }
    
    
    
    
    /**
     * Filter chain associated with the request.
     */
    protected FilterChain filterChain = null;

    /**
     * Get filter chain associated with the request.
     */
    public FilterChain getFilterChain() {
        return (this.filterChain);
    }

    /**
     * Set filter chain associated with the request.
     * 
     * @param filterChain new filter chain
     */
    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }
    
    
    
    /**
     * The facade associated with this request.
     */
    protected RequestFacade facade = null;

    /**
     * Return the <code>ServletRequest</code> for which this object
     * is the facade.  This method must be implemented by a subclass.
     */
    public HttpServletRequest getRequest() {
        if (facade == null) {
            facade = new RequestFacade(this);
        } 
        return (facade);
    }
    
    
    
    
    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
    	
    	context = null;
        wrapper = null;
        
        inputBuffer.recycle();

        
        attributes.clear();
        
        
        mappingData.recycle();
    }
    
    
}
