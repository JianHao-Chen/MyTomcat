package My.catalina.connector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import My.catalina.Context;
import My.catalina.Globals;
import My.catalina.Session;
import My.catalina.Wrapper;
import My.tomcat.util.buf.CharChunk;
import My.tomcat.util.buf.UEncoder;
import My.tomcat.util.http.MimeHeaders;
import My.tomcat.util.http.ServerCookie;
import My.tomcat.util.net.URL;

public class Response implements HttpServletResponse{

	// ----------------------- Constructors ---------------------
	
	public Response() {
        urlEncoder.addSafeCharacter('/');
    }
	
	
	
	// -------------------------- Properties --------------------------


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
        
        //connector.getProtocol() is 
        //"My.coyote.http11.Http11NioProtocol"
     
        outputBuffer = new OutputBuffer();
     
        outputStream = new CoyoteOutputStream(outputBuffer);
        writer = new CoyoteWriter(outputBuffer);
    }
    
    
    
    
    /**
     * The associated output buffer.
     */
    protected OutputBuffer outputBuffer;


    /**
     * The associated output stream.
     */
    protected CoyoteOutputStream outputStream;
    
    /**
     * The associated writer.
     */
    protected CoyoteWriter writer;
    
    
    /**
     * The application commit flag.
     */
    protected boolean appCommitted = false;
    
    
    /**
     * The set of Cookies associated with this Response.
     */
    protected ArrayList<Cookie> cookies = new ArrayList<Cookie>();
    
    
    /**
     * The error flag.
     */
    protected boolean error = false;
    
    
    /**
     * The included flag.
     */
    protected boolean included = false;
    
    
    /**
     * The characterEncoding flag
     */
    private boolean isCharacterEncodingSet = false;
    
    
    /**
     * Recyclable buffer to hold the redirect URL.
     */
    protected CharChunk redirectURLCC = new CharChunk();
    
    
    
    /**
     * Using output stream flag.
     */
    protected boolean usingOutputStream = false;


    /**
     * Using writer flag.
     */
    protected boolean usingWriter = false;
    
    
    /**
     * URL encoder.
     */
    protected UEncoder urlEncoder = new UEncoder();
    
    /**
     * Coyote response.
     */
    protected My.coyote.Response coyoteResponse;

    /**
     * Set the Coyote response.
     * 
     * @param coyoteResponse The Coyote response
     */
    public void setCoyoteResponse(My.coyote.Response coyoteResponse) {
        this.coyoteResponse = coyoteResponse;
        outputBuffer.setResponse(coyoteResponse);
    }

    /**
     * Get the Coyote response.
     */
    public My.coyote.Response getCoyoteResponse() {
        return (coyoteResponse);
    }
    
    
    
    
    /**
     * The request with which this response is associated.
     */
    protected Request request = null;

    /**
     * Return the Request with which this Response is associated.
     */
    public My.catalina.connector.Request getRequest() {
        return (this.request);
    }

    /**
     * Set the Request with which this Response is associated.
     *
     * @param request The new associated request
     */
    public void setRequest(My.catalina.connector.Request request) {
        this.request = (Request) request;
    }
    
    
    
    /**
     * The facade associated with this response.
     */
    protected ResponseFacade facade = null;

    /**
     * Return the <code>ServletResponse</code> for which this object
     * is the facade.
     */
    public HttpServletResponse getResponse() {
        if (facade == null) {
            facade = new ResponseFacade(this);
        }
        return (facade);
    }
    
    
    
    
    
	// ---------------------- Response Methods----------------------
    
    /**
     * Application commit flag accessor.
     */
    public boolean isAppCommitted() {
        return (this.appCommitted || isCommitted() || isSuspended()
                || ((getContentLength() > 0) 
                    && (getContentCount() >= getContentLength())));
    }
    
    
    
    /**
     * Set the suspended flag.
     * 
     * @param suspended The new suspended flag value
     */
    public void setSuspended(boolean suspended) {
        outputBuffer.setSuspended(suspended);
    }


    /**
     * Suspended flag accessor.
     */
    public boolean isSuspended() {
        return outputBuffer.isSuspended();
    }
    
    
    /**
     * Set the error flag.
     */
    public void setError() {
        error = true;
    }


    /**
     * Error flag accessor.
     */
    public boolean isError() {
        return error;
    }
    
    
    
    /**
     * Set the buffer size to be used for this Response.
     *
     * @param size The new buffer size
     *
     * @exception IllegalStateException if this method is called after
     *  output has been committed for this response
     */
    public void setBufferSize(int size) {

        if (isCommitted() || !outputBuffer.isNew())
            throw new IllegalStateException("coyoteResponse.setBufferSize.ise");

        outputBuffer.setBufferSize(size);

    }
    
    
    /**
     * Perform whatever actions are required to flush and close the output
     * stream or writer, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishResponse() 
        throws IOException {
        // Writing leftover bytes
        outputBuffer.close();
    }
    
    
    
    /**
     * Set the content length (in bytes) for this Response.
     *
     * @param length The new content length
     */
    public void setContentLength(int length) {

        if (isCommitted())
            return;

        // Ignore any call from an included servlet
        if (included)
            return;
        
        if (usingWriter)
            return;
        
        coyoteResponse.setContentLength(length);

    }
    
    
    /**
     * Return the content length that was set or calculated for this Response.
     */
    public int getContentLength() {
        return (coyoteResponse.getContentLength());
    }
    
    
    /**
     * Return the number of bytes actually written to the output stream.
     */
    public int getContentCount() {
        return outputBuffer.getContentWritten();
    }
    
    
    /**
     * Set the content type for this Response.
     *
     * @param type The new content type
     */
    public void setContentType(String type) {
    	
    	 if (isCommitted())
             return;
    	 
    	// Ignore any call from an included servlet
         if (included)
             return;
         
         // Ignore charset if getWriter() has already been called
         if (usingWriter) {
        	 if (type != null) {
                 int index = type.indexOf(";");
                 if (index != -1) {
                     type = type.substring(0, index);
                 }
             }
         }
         
         coyoteResponse.setContentType(type);
         
         
      // Check to see if content type contains charset
         if (type != null) {
             int index = type.indexOf(";");
             if (index != -1) {
                 int len = type.length();
                 index++;
                 while (index < len && Character.isSpace(type.charAt(index))) {
                     index++;
                 }
                 if (index+7 < len
                         && type.charAt(index) == 'c'
                         && type.charAt(index+1) == 'h'
                         && type.charAt(index+2) == 'a'
                         && type.charAt(index+3) == 'r'
                         && type.charAt(index+4) == 's'
                         && type.charAt(index+5) == 'e'
                         && type.charAt(index+6) == 't'
                         && type.charAt(index+7) == '=') {
                     isCharacterEncodingSet = true;
                 }
             }
         }
    	
    }
    
    
    /**
     * Set the application commit flag.
     * 
     * @param appCommitted The new application committed flag value
     */
    public void setAppCommitted(boolean appCommitted) {
        this.appCommitted = appCommitted;
    }
    
    
    /**
     * Send an error response with the specified status and a
     * default message.
     *
     * @param status HTTP status code to send
     *
     * @exception IllegalStateException if this response has
     *  already been committed
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int status) 
        throws IOException {
        sendError(status, null);
    }
    
    
    
    /**
     * Send an error response with the specified status and message.
     *
     * @param status HTTP status code to send
     * @param message Corresponding message to send
     *
     * @exception IllegalStateException if this response has
     *  already been committed
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int status, String message) 
        throws IOException {
    	
    	if (isCommitted())
            throw new IllegalStateException
                ("coyoteResponse.sendError.ise");
    	
    	Wrapper wrapper = getRequest().getWrapper();
    	if (wrapper != null) {
            //wrapper.incrementErrorCount();
        } 
    	
    	setError();
    	
    	coyoteResponse.setStatus(status);
    	coyoteResponse.setMessage(message);
    	
    	// Clear any data content that has been buffered
        resetBuffer();
        
        // Cause the response to be finished (from the application perspective)
        setSuspended(true);
        
    }
    
    
    
    /**
     * Reset the data buffer but not any status or header information.
     *
     * @exception IllegalStateException if the response has already
     *  been committed
     */
    public void resetBuffer() {
        resetBuffer(false);
    }
    
    
    
    /**
     * Reset the data buffer and the using Writer/Stream flags but not any
     * status or header information.
     *
     * @param resetWriterStreamFlags <code>true</code> if the internal
     *        <code>usingWriter</code>, <code>usingOutputStream</code>,
     *        <code>isCharacterEncodingSet</code> flags should also be reset
     * 
     * @exception IllegalStateException if the response has already
     *  been committed
     */
    public void resetBuffer(boolean resetWriterStreamFlags) {
    	
    	if (isCommitted())
            throw new IllegalStateException
                ("coyoteResponse.resetBuffer.ise");
    	
    	outputBuffer.reset();
    	
    	if(resetWriterStreamFlags) {
            usingOutputStream = false;
            usingWriter = false;
            isCharacterEncodingSet = false;
        }
    	
    }
    
    
    
    
	// -------------- ServletResponse Methods --------------
    
    /**
     * Return the servlet output stream associated with this Response.
     *
     * @exception IllegalStateException if <code>getWriter</code> has
     *  already been called for this response
     * @exception IOException if an input/output error occurs
     */
    public ServletOutputStream getOutputStream() 
        throws IOException {
    	
    	if (usingWriter)
    		throw new IllegalStateException("coyoteResponse.getOutputStream.ise");
    	
    	usingOutputStream = true;
    	if (outputStream == null) {
    		outputStream = new CoyoteOutputStream(outputBuffer);
    	}
    	return outputStream;
    }
    
    
    /**
     * Return the writer associated with this Response.
     *
     * @exception IllegalStateException if <code>getOutputStream</code> has
     *  already been called for this response
     * @exception IOException if an input/output error occurs
     */
    public PrintWriter getWriter() 
        throws IOException {
    	
    	if (usingOutputStream)
            throw new IllegalStateException("coyoteResponse.getWriter.ise");
    	
    	usingWriter = true;
    	
    	outputBuffer.checkConverter();
    	
    	if (writer == null) {
            writer = new CoyoteWriter(outputBuffer);
        }
        return writer;
    }
    
    
    
    /**
     * Has the output of this response already been committed?
     */
    public boolean isCommitted() {
        return (coyoteResponse.isCommitted());
    }

    
    
    
	// ---------------- HttpServletResponse Methods ----------------
    
    
    /**
     * Add the specified Cookie to those that will be included with
     * this Response.
     *
     * @param cookie Cookie to be added
     */
    public void addCookie(final Cookie cookie) {
    	
    	addCookieInternal(cookie);
    }
    
    
    /**
     * Add the specified Cookie to those that will be included with
     * this Response.
     *
     * @param cookie Cookie to be added
     */
    public void addCookieInternal(final Cookie cookie) {
        addCookieInternal(cookie, false);
    }
    
    
    /**
     * Add the specified Cookie to those that will be included with
     * this Response.
     *
     * @param cookie    Cookie to be added
     * @param httpOnly  Should the httpOnly falg be set on this cookie
     */
    public void addCookieInternal(final Cookie cookie, final boolean httpOnly) {
    	
    	if (isCommitted())
            return;
    	
    	final StringBuffer sb = generateCookieString(cookie, httpOnly);
    	
    	//if we reached here, no exception, cookie is valid
        // the header name is Set-Cookie for both "old" and v.1 ( RFC2109 )
        // RFC2965 is not supported by browsers and the Servlet spec
        // asks for 2109.
        addHeader("Set-Cookie", sb.toString());
    }
    
    
    public StringBuffer generateCookieString(final Cookie cookie, 
            final boolean httpOnly) {
    	
    	final StringBuffer sb = new StringBuffer();
    	
    	ServerCookie.appendCookieValue
        (sb, cookie.getVersion(), cookie.getName(), cookie.getValue(),
             cookie.getPath(), cookie.getDomain(), cookie.getComment(), 
             cookie.getMaxAge(), cookie.getSecure(), httpOnly);
    	
    	return sb;
    	
    }
    
    
    
    
    /**
     * Special method for adding a session cookie as we should be overriding 
     * any previous 
     * @param cookie
     */
    public void addSessionCookieInternal(final Cookie cookie,
            boolean httpOnly) {
    	
    	if (isCommitted())
            return;
    	
    	String name = cookie.getName();
        final String headername = "Set-Cookie";
        final String startsWith = name + "=";
        
        final StringBuffer sb = generateCookieString(cookie, httpOnly);
        
        boolean set = false;
        
        MimeHeaders headers = coyoteResponse.getMimeHeaders();
        
        int n = headers.size();
        for (int i = 0; i < n; i++) {
        	
        	if (headers.getName(i).toString().equals(headername)) {
                if (headers.getValue(i).toString().startsWith(startsWith)) {
                    headers.getValue(i).setString(sb.toString());
                    set = true;
                }
            }
        }
        
        if (set) {
        	Iterator<Cookie> iter = cookies.iterator();
            while (iter.hasNext()) {
            	Cookie c = iter.next();
                if (name.equals(c.getName())) {
                    iter.remove();
                    break;
                }
            }
        }
        else {
            addHeader(headername, sb.toString());
        }
        cookies.add(cookie);
    }
    
    
    
    
    
    
    
    
    
    /**
     * Send an acknowledgment of a request.
     * 
     * @exception IOException if an input/output error occurs
     */
    public void sendAcknowledgement()
        throws IOException {
    	
    	coyoteResponse.acknowledge();
    }
    
    
    /**
     * Add the specified header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Value to be set
     */
    public void addHeader(String name, String value) {
    	
    	if (name == null || name.length() == 0 || value == null) {
            return;
        }

        if (isCommitted())
            return;
        
        coyoteResponse.addHeader(name, value);
    }

    
    /**
     * Return the Context within which this Request is being processed.
     */
    public Context getContext() {
        return (request.getContext());
    }
    
    
    /**
     * Return the HTTP status code associated with this Response.
     */
    public int getStatus() {
        return coyoteResponse.getStatus();
    }
    
    /**
     * Set the HTTP status to be returned with this response.
     *
     * @param status The new HTTP status
     */
    public void setStatus(int status) {
        setStatus(status, null);
    }
    
    
    
    /**
     * Set the HTTP status and message to be returned with this response.
     *
     * @param status The new HTTP status
     * @param message The associated text message
     *
     * @deprecated As of Version 2.1 of the Java Servlet API, this method
     *  has been deprecated due to the ambiguous meaning of the message
     *  parameter.
     */
    public void setStatus(int status, String message) {

        if (isCommitted())
            return;

        // Ignore any call from an included servlet
        if (included)
            return;

        coyoteResponse.setStatus(status);
        coyoteResponse.setMessage(message);

    }
    
    

    /**
     * Set the specified header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Value to be set
     */
	public void setHeader(String name, String value) {
		if (name == null || name.length() == 0 || value == null) {
            return;
        }

        if (isCommitted())
            return;
        
        // Ignore any call from an included servlet
        if (included)
            return;

        coyoteResponse.setHeader(name, value);
		
	}
	
	
	
	/**
     * Encode the session identifier associated with this response
     * into the specified URL, if necessary.
     *
     * @param url URL to be encoded
     */
    public String encodeURL(String url) {
    	
    	String absolute = toAbsolute(url);
    	if (isEncodeable(absolute)) {
    		// W3c spec clearly said 
            if (url.equalsIgnoreCase("")){
                url = absolute;
            }
            
            return (toEncoded(url, request.getSessionInternal().getIdInternal()));
    	}
    	else 
    		return url;
    }
    
    
    /**
     * Return the specified URL with the specified session identifier
     * suitably encoded.
     *
     * @param url URL to be encoded with the session id
     * @param sessionId Session id to be included in the encoded URL
     */
    protected String toEncoded(String url, String sessionId) {
    	
    	if ((url == null) || (sessionId == null))
            return (url);
    	
    	String path = url;
        String query = "";
        String anchor = "";
        
        int question = url.indexOf('?');
        if (question >= 0) {
            path = url.substring(0, question);
            query = url.substring(question);
        }
        
        int pound = path.indexOf('#');
        if (pound >= 0) {
            anchor = path.substring(pound);
            path = path.substring(0, pound);
        }
        
        StringBuffer sb = new StringBuffer(path);
        if( sb.length() > 0 ) { // jsessionid can't be first.
            sb.append(";");
            sb.append(Globals.SESSION_PARAMETER_NAME);
            sb.append("=");
            sb.append(sessionId);
        }
        
        sb.append(anchor);
        sb.append(query);
        return (sb.toString());
        
    }
	
	
	// ---------------------- Public Methods ----------------------
	
	/**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
    	
    	outputBuffer.recycle();
    	usingOutputStream = false;
    	usingWriter = false;
    	
    }
    
    
    
    
    /**
     * Convert (if necessary) and return the absolute URL that represents the
     * resource referenced by this possibly relative URL.  If this URL is
     * already absolute, return it unchanged.
     *
     * @param location URL to be (possibly) converted and then returned
     *
     * @exception IllegalArgumentException if a MalformedURLException is
     *  thrown when converting the relative URL to an absolute one
     */
    private String toAbsolute(String location) {
    	if (location == null)
            return (location);

        boolean leadingSlash = location.startsWith("/");

        if (leadingSlash || !hasScheme(location)) {
        	redirectURLCC.recycle();
        	
        	String scheme = request.getScheme();
            String name = request.getServerName();
            int port = request.getServerPort();
            
            try {
            	redirectURLCC.append(scheme, 0, scheme.length());
                redirectURLCC.append("://", 0, 3);
                redirectURLCC.append(name, 0, name.length());
                
                if ((scheme.equals("http") && port != 80)
                        || (scheme.equals("https") && port != 443)) {
                	redirectURLCC.append(':');
                    String portS = port + "";
                    redirectURLCC.append(portS, 0, portS.length());
                }
                
                if (!leadingSlash) {
                	String relativePath = request.getDecodedRequestURI();
                    int pos = relativePath.lastIndexOf('/');
                    relativePath = relativePath.substring(0, pos);
                    
                    String encodedURI = null;
                    final String frelativePath = relativePath;
                    
                    encodedURI = urlEncoder.encodeURL(relativePath);
                    
                    redirectURLCC.append(encodedURI, 0, encodedURI.length());
                    redirectURLCC.append('/');
                }
                
                redirectURLCC.append(location, 0, location.length());
            }catch (IOException e) {
            	
            }
            
            return redirectURLCC.toString();
        }
        else 
        	return (location);

    }
    
    
    /**
     * Return <code>true</code> if the specified URL should be encoded with
     * a session identifier.  This will be true if all of the following
     * conditions are met:
     * <ul>
     * <li>The request we are responding to asked for a valid session
     * <li>The requested session ID was not received via a cookie
     * <li>The specified URL points back to somewhere within the web
     *     application that is responding to this request
     * <li>If URL rewriting hasn't been disabled for this context
     * </ul>
     *
     * @param location Absolute URL to be validated
     */
    protected boolean isEncodeable(final String location) {
    	
    	if (getContext().isDisableURLRewriting())
            return (false);
    	
    	if (location == null)
            return (false);
    	
    	// Is this an intra-document reference?
        if (location.startsWith("#"))
            return (false);

        // Are we in a valid session that is not using cookies?
        final Request hreq = request;
        final Session session = hreq.getSessionInternal(false);
        if (session == null)
            return (false);
        
        if (hreq.isRequestedSessionIdFromCookie())
            return (false);
        
        return doIsEncodeable(hreq, session, location);
    }
    
    
    private boolean doIsEncodeable(Request hreq, Session session, 
            String location) {
    	
    	// Is this a valid absolute URL?
        URL url = null;
        try {
            url = new URL(location);
        } catch (MalformedURLException e) {
            return (false);
        }
        
        // Does this URL match down to (and including) the context path?
        if (!hreq.getScheme().equalsIgnoreCase(url.getProtocol()))
            return (false);
        
        if (!hreq.getServerName().equalsIgnoreCase(url.getHost()))
            return (false);
        
        int serverPort = hreq.getServerPort();
        if (serverPort == -1) {
            if ("https".equals(hreq.getScheme()))
                serverPort = 443;
            else
                serverPort = 80;
        }
        
        int urlPort = url.getPort();
        
        if (urlPort == -1) {
            if ("https".equals(url.getProtocol()))
                urlPort = 443;
            else
                urlPort = 80;
        }
        
        if (serverPort != urlPort)
            return (false);
        
        String contextPath = getContext().getPath();
        if (contextPath != null) {
            String file = url.getFile();
            if ((file == null) || !file.startsWith(contextPath))
                return (false);
            String tok = ";" + Globals.SESSION_PARAMETER_NAME + "=" + session.getIdInternal();
            if( file.indexOf(tok, contextPath.length()) >= 0 )
                return (false);
        }
        
        // This URL belongs to our web application, so it is encodeable
        return (true);
    }
    
    
    /**
     * Determine if a URI string has a <code>scheme</code> component.
     */
    private boolean hasScheme(String uri) {
        int len = uri.length();
        for(int i=0; i < len ; i++) {
            char c = uri.charAt(i);
            if(c == ':') {
                return i > 0;
            } else if(!URL.isSchemeChar(c)) {
                return false;
            }
        }
        return false;
    }
    	
    
}
