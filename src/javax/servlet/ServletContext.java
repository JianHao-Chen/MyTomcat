package javax.servlet;



/**
 * 
 * Defines a set of methods that a servlet uses to communicate with its
 * servlet container, for example, to get the MIME type of a file, dispatch
 * requests, or write to a log file.
 *
 * <p>There is one context per "web application" per Java Virtual Machine.  (A
 * "web application" is a collection of servlets and content installed under a
 * specific subset of the server's URL namespace such as <code>/catalog</code>
 * and possibly installed via a <code>.war</code> file.) 
 *
 * <p>In the case of a web
 * application marked "distributed" in its deployment descriptor, there will
 * be one context instance for each virtual machine.  In this situation, the 
 * context cannot be used as a location to share global information (because
 * the information won't be truly global).  Use an external resource like 
 * a database instead.
 *
 * <p>The <code>ServletContext</code> object is contained within 
 * the {@link ServletConfig} object, which the Web server provides the
 * servlet when the servlet is initialized.
 */

public interface ServletContext {

}
