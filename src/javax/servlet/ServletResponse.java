package javax.servlet;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Defines an object to assist a servlet in sending a response to the client.
 * The servlet container creates a <code>ServletResponse</code> object and
 * passes it as an argument to the servlet's <code>service</code> method.
 *
 * <p>To send binary data in a MIME body response, use
 * the {@link ServletOutputStream} returned by {@link #getOutputStream}.
 * To send character data, use the <code>PrintWriter</code> object 
 * returned by {@link #getWriter}. To mix binary and text data,
 * for example, to create a multipart response, use a
 * <code>ServletOutputStream</code> and manage the character sections
 * manually.
 *
 * <p>The charset for the MIME body response can be specified
 * explicitly using the {@link #setCharacterEncoding} and
 * {@link #setContentType} methods, or implicitly
 * using the {@link #setLocale} method.
 * Explicit specifications take precedence over
 * implicit specifications. If no charset is specified, ISO-8859-1 will be
 * used. The <code>setCharacterEncoding</code>,
 * <code>setContentType</code>, or <code>setLocale</code> method must
 * be called before <code>getWriter</code> and before committing
 * the response for the character encoding to be used.
 * 
 * <p>See the Internet RFCs such as 
 * <a href="http://www.ietf.org/rfc/rfc2045.txt">
 * RFC 2045</a> for more information on MIME. Protocols such as SMTP
 * and HTTP define profiles of MIME, and those standards
 * are still evolving.
 */

public interface ServletResponse {

	/**
     * Returns a {@link ServletOutputStream} suitable for writing binary 
     * data in the response. The servlet container does not encode the
     * binary data.  
     
     * <p> Calling flush() on the ServletOutputStream commits the response.
     
     * Either this method or {@link #getWriter} may 
     * be called to write the body, not both.
     *
     * @return				a {@link ServletOutputStream} for writing binary data	
     *
     * @exception IllegalStateException if the <code>getWriter</code> method
     * 					has been called on this response
     *
     * @exception IOException 		if an input or output exception occurred
     *
     * @see 				#getWriter
     *
     */

    public ServletOutputStream getOutputStream() throws IOException;
    
    
    
    /**
     * Returns a <code>PrintWriter</code> object that
     * can send character text to the client.
     * The <code>PrintWriter</code> uses the character
     * encoding returned by {@link #getCharacterEncoding}.
     * If the response's character encoding has not been
     * specified as described in <code>getCharacterEncoding</code>
     * (i.e., the method just returns the default value 
     * <code>ISO-8859-1</code>), <code>getWriter</code>
     * updates it to <code>ISO-8859-1</code>.
     * <p>Calling flush() on the <code>PrintWriter</code>
     * commits the response.
     * <p>Either this method or {@link #getOutputStream} may be called
     * to write the body, not both.
     *
     * 
     * @return 		a <code>PrintWriter</code> object that 
     *			can return character data to the client 
     *
     * @exception UnsupportedEncodingException
     *			if the character encoding returned
     *			by <code>getCharacterEncoding</code> cannot be used
     *
     * @exception IllegalStateException
     *			if the <code>getOutputStream</code>
     * 			method has already been called for this 
     *			response object
     *
     * @exception IOException
     *			if an input or output exception occurred
     *
     * @see 		#getOutputStream
     * @see 		#setCharacterEncoding
     *
     */

    public PrintWriter getWriter() throws IOException;
    
    
    
    /**
     * Sets the content type of the response being sent to
     * the client, if the response has not been committed yet.
     * The given content type may include a character encoding
     * specification, for example, <code>text/html;charset=UTF-8</code>.
     * The response's character encoding is only set from the given
     * content type if this method is called before <code>getWriter</code>
     * is called.
     * <p>This method may be called repeatedly to change content type and
     * character encoding.
     * This method has no effect if called after the response
     * has been committed. It does not set the response's character
     * encoding if it is called after <code>getWriter</code>
     * has been called or after the response has been committed.
     * <p>Containers must communicate the content type and the character
     * encoding used for the servlet response's writer to the client if
     * the protocol provides a way for doing so. In the case of HTTP,
     * the <code>Content-Type</code> header is used.
     *
     * @param type 	a <code>String</code> specifying the MIME 
     *			type of the content
     *
     * @see 		#setLocale
     * @see 		#setCharacterEncoding
     * @see 		#getOutputStream
     * @see 		#getWriter
     *
     */

    public void setContentType(String type);
    
    
    /**
     * Sets the length of the content body in the response
     * In HTTP servlets, this method sets the HTTP Content-Length header.
     *
     *
     * @param len 	an integer specifying the length of the 
     * 			content being returned to the client; sets
     *			the Content-Length header
     *
     */

    public void setContentLength(int len);
    
    
    
    /**
     * Sets the preferred buffer size for the body of the response.  
     * The servlet container will use a buffer at least as large as 
     * the size requested.  The actual buffer size used can be found
     * using <code>getBufferSize</code>.
     *
     * <p>A larger buffer allows more content to be written before anything is
     * actually sent, thus providing the servlet with more time to set
     * appropriate status codes and headers.  A smaller buffer decreases 
     * server memory load and allows the client to start receiving data more
     * quickly.
     *
     * <p>This method must be called before any response body content is
     * written; if content has been written or the response object has
     * been committed, this method throws an 
     * <code>IllegalStateException</code>.
     *
     * @param size 	the preferred buffer size
     *
     * @exception  IllegalStateException  	if this method is called after
     *						content has been written
     *
     * @see 		#getBufferSize
     * @see 		#flushBuffer
     * @see 		#isCommitted
     * @see 		#reset
     *
     */

    public void setBufferSize(int size);
    
    
    /**
     * Returns a boolean indicating if the response has been
     * committed.  A committed response has already had its status 
     * code and headers written.
     *
     * @return		a boolean indicating if the response has been
     *  		committed
     *
     * @see 		#setBufferSize
     * @see 		#getBufferSize
     * @see 		#flushBuffer
     * @see 		#reset
     *
     */

    public boolean isCommitted();
}
