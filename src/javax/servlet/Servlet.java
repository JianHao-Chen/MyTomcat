package javax.servlet;

/**
 * Defines methods that all servlets must implement.
 *
 * <p>A servlet is a small Java program that runs within a Web server.
 * Servlets receive and respond to requests from Web clients,
 * usually across HTTP, the HyperText Transfer Protocol. 
 *
 * <p>To implement this interface, you can write a generic servlet
 * that extends
 * <code>javax.servlet.GenericServlet</code> or an HTTP servlet that
 * extends <code>javax.servlet.http.HttpServlet</code>.
 *
 * <p>This interface defines methods to initialize a servlet,
 * to service requests, and to remove a servlet from the server.
 * These are known as life-cycle methods and are called in the
 * following sequence:
 * <ol>
 * <li>The servlet is constructed, then initialized with the <code>init</code> method.
 * <li>Any calls from clients to the <code>service</code> method are handled.
 * <li>The servlet is taken out of service, then destroyed with the 
 * <code>destroy</code> method, then garbage collected and finalized.
 * </ol>
 *
 * <p>In addition to the life-cycle methods, this interface
 * provides the <code>getServletConfig</code> method, which the servlet 
 * can use to get any startup information, and the <code>getServletInfo</code>
 * method, which allows the servlet to return basic information about itself,
 * such as author, version, and copyright.
 */

public interface Servlet {

}
