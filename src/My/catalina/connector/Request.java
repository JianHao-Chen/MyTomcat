package My.catalina.connector;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import My.catalina.Context;
import My.catalina.Globals;
import My.catalina.Host;
import My.catalina.Manager;
import My.catalina.Session;
import My.catalina.Wrapper;
import My.catalina.core.ApplicationFilterFactory;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.Cookies;
import My.tomcat.util.http.FastHttpDateFormat;
import My.tomcat.util.http.Parameters;
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
     * Return all of the values of the specified header, if any; otherwise,
     * return an empty enumeration.
     *
     * @param name Name of the requested header
     */
    public Enumeration getHeaders(String name) {
        return coyoteRequest.getMimeHeaders().values(name);
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
    
    /**
     * ServletInputStream.
     */
    protected CoyoteInputStream inputStream = 
        new CoyoteInputStream(inputBuffer);
    
    
    /**
     * Using stream flag.
     */
    protected boolean usingInputStream = false;


    /**
     * Using writer flag.
     */
    protected boolean usingReader = false;
    
    
    
    /**
     * Post data buffer.
     */
    protected static int CACHED_POST_LEN = 8192;
    protected byte[] postData = null;
    
	
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
	 * Return the Context within which this Request is being processed.
	 */
	public Context getContext() {
	    return (this.context);
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
     * Return the character encoding for this Request.
     */
    public String getCharacterEncoding() {
      return (coyoteRequest.getCharacterEncoding());
    }
    
    
    
    /**
     * Return the content length for this Request.
     */
    public int getContentLength() {
        return (coyoteRequest.getContentLength());
    }


    /**
     * Return the content type for this Request.
     */
    public String getContentType() {
        return (coyoteRequest.getContentType());
    }
    
    
    
    
    /**
     * Path parameters
     */
    protected Map<String,String> pathParameters = new HashMap<String, String>();
    
    protected void addPathParameter(String name, String value) {
        pathParameters.put(name, value);
    }

    protected String getPathParameter(String name) {
        return pathParameters.get(name);
    }
    
    
    
    
    /**
     * The requested session ID (if any) for this request.
     */
    protected String requestedSessionId = null;
    
    
    /**
     * Set the requested session ID for this request.  This is normally called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param id The new session id
     */
    public void setRequestedSessionId(String id) {

        this.requestedSessionId = id;

    }

    
    
    /**
     * Was the requested session ID received in a URL?
     */
    protected boolean requestedSessionURL = false;

    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a URL.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionURL(boolean flag) {

        this.requestedSessionURL = flag;

    }
    
    
    
    /**
     * Request parameters parsed flag.
     */
    protected boolean parametersParsed = false;
    
    /**
     * Return the value of the specified request parameter, if any; otherwise,
     * return <code>null</code>.  If there is more than one value defined,
     * return only the first one.
     *
     * @param name Name of the desired request parameter
     */
    public String getParameter(String name) {

        if (!parametersParsed)
            parseParameters();

        return coyoteRequest.getParameters().getParameter(name);

    }
    
    
    /**
     * Parse request parameters.
     */
    protected void parseParameters() {
    	parametersParsed = true;
    	
    	Parameters parameters = coyoteRequest.getParameters();
    	
    	
    	// getCharacterEncoding() may have been overridden to search for
        // hidden form field containing request encoding
        String enc = getCharacterEncoding();
    	
    	parameters.setEncoding
        	(My.coyote.Constants.DEFAULT_CHARACTER_ENCODING);
    	
    	
    	parameters.handleQueryParameters();
    	
    	if (usingInputStream || usingReader)
            return;
    	
    	if (!getMethod().equalsIgnoreCase("POST"))
            return;
    	
    	String contentType = getContentType();
    	if (contentType == null)
            contentType = "";
    	
    	int semicolon = contentType.indexOf(';');
    	if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }
    	
    	if (!("application/x-www-form-urlencoded".equals(contentType)))
            return;
    	 
    	 
    	int len = getContentLength();
    	
    	if (len > 0) {
    		int maxPostSize = connector.getMaxPostSize();
    		if ((maxPostSize > 0) && (len > maxPostSize)) {
    			// log error
    			return;
    		}
    		
    		byte[] formData = null;
    		if (len < CACHED_POST_LEN) {
                if (postData == null)
                    postData = new byte[CACHED_POST_LEN];
                formData = postData;
            } else {
                formData = new byte[len];
            }
    		
    		
    		try {
    			if (readPostBody(formData, len) != len) {
    				return;
    			}
    		}catch (IOException e) {
    			return;
    		}
    		
    		parameters.processParameters(formData, 0, len);
    		
    	}
    }
    
    
    /**
     * Read post body in an array.
     */
    protected int readPostBody(byte body[], int len)
        throws IOException {
    	
    	int offset = 0;
    	do {
    		int inputLen = getStream().read(body, offset, len - offset);
    		if (inputLen <= 0) {
                return offset;
            }
    		offset += inputLen;
    	}
    	while ((len - offset) > 0);
    	
    	return len;
    }
    
    
    
    
    /**
     * Return the input stream associated with this Request.
     */
    public InputStream getStream() {
        if (inputStream == null) {
            inputStream = new CoyoteInputStream(inputBuffer);
        }
        return inputStream;
    }
    
    
    
    /**
     * Return the protocol and version used to make this Request.
     */
    public String getProtocol() {
        return coyoteRequest.protocol().toString();
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
     * The currently active session for this request.
     */
    protected Session session = null;
    
    
    /**
     * Return the session associated with this Request, creating one
     * if necessary.
     */
    public HttpSession getSession() {
    	
    	return null;
    }
    
    
    /**
     * Return the session associated with this Request, creating one
     * if necessary and requested.
     *
     * @param create Create a new session if one does not exist
     */
    public HttpSession getSession(boolean create) {
    	Session session = doGetSession(create);
    	
    	
    	return null;
    }
    
    
    protected Session doGetSession(boolean create) {
    	
    	// There cannot be a session if no context has been assigned yet
        if (context == null)
            return (null);
        
        // Return the current session if it exists and is valid
        if ((session != null) && !session.isValid())
        	session = null;
        if (session != null)
            return (session);
        
        // Return the requested session if it exists and is valid
        Manager manager = null;
        
        if (context != null)
            manager = context.getManager();
        
        
        return null;
    }
    
    
    
    /**
     * Parse locales.
     */
    protected boolean localesParsed = false;
    
    /**
     * The preferred Locales assocaited with this Request.
     */
    protected ArrayList locales = new ArrayList();
    
    /**
     * The default Locale if none are specified.
     */
    protected static Locale defaultLocale = Locale.getDefault();
    
    
    /**
     * Return the preferred Locale that the client will accept content in,
     * based on the value for the first <code>Accept-Language</code> header
     * that was encountered.  If the request did not specify a preferred
     * language, the server's default Locale is returned.
     */
    public Locale getLocale() {
    	
    	 if (!localesParsed)
             parseLocales();
    	 
    	 if (locales.size() > 0) {
             return ((Locale) locales.get(0));
         } else {
             return (defaultLocale);
         }
    }
    
    
    /**
     * Parse request locales.
     */
    protected void parseLocales() {

        localesParsed = true;

        Enumeration values = getHeaders("accept-language");

        while (values.hasMoreElements()) {
            String value = values.nextElement().toString();
          //  parseLocalesHeader(value);
        }

    }
    
    
    
    /**
     * Cookies parsed flag.
     */
    protected boolean cookiesParsed = false;
    
    
    /**
     * The set of cookies associated with this Request.
     */
    protected Cookie[] cookies = null;
    
    /**
     * Return the set of Cookies received with this Request.
     */
    public Cookie[] getCookies() {

        if (!cookiesParsed)
            parseCookies();

        return cookies;

    }


    /**
     * Set the set of cookies recieved with this Request.
     */
    public void setCookies(Cookie[] cookies) {

        this.cookies = cookies;

    }
    
    
    /**
     * Parse cookies.
     */
    protected void parseCookies() {
    	
    	cookiesParsed = true;
    	
    	Cookies serverCookies = coyoteRequest.getCookies();
    	int count = serverCookies.getCookieCount();
        if (count <= 0)
            return;
        
        
        
    }
    
    
    
    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
    	
    	context = null;
        wrapper = null;
        

        parametersParsed = false;
        cookiesParsed = false;
        
        inputBuffer.recycle();

        
        attributes.clear();
        
        
        mappingData.recycle();
    }
    
    
}
