package javax.servlet;

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

}
