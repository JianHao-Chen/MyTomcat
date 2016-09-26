package My.catalina.core;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Facade for the <b>StandardWrapper</b> object.
 */

public class StandardWrapperFacade implements ServletConfig{

	// ----------------- Constructors -----------------
	
	 /**
     * Create a new facede around a StandardWrapper.
     */
    public StandardWrapperFacade(StandardWrapper config) {

        super();
        this.config = (ServletConfig) config;

    }
	
	// --------------- Instance Variables ---------------
    
    /**
     * Wrapped config.
     */
    private ServletConfig config = null;


    /**
     * Wrapped context (facade).
     */
    private ServletContext context = null;
    
    
	// ------------------ ServletConfig Methods ------------------
    
    public String getServletName() {
        return config.getServletName();
    }
    
    public ServletContext getServletContext() {
        if (context == null) {
            context = config.getServletContext();
            if ((context != null) && (context instanceof ApplicationContext))
                context = ((ApplicationContext) context).getFacade();
        }
        return (context);
    }
    
    public String getInitParameter(String name) {
        return config.getInitParameter(name);
    }


    public Enumeration getInitParameterNames() {
        return config.getInitParameterNames();
    }
    
}
