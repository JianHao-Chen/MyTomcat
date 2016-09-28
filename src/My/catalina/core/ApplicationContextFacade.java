package My.catalina.core;

import java.util.HashMap;

import javax.servlet.ServletContext;

/**
 * Facade object which masks the internal <code>ApplicationContext</code>
 * object from the web application.
 */

public class ApplicationContextFacade implements ServletContext {

	// ------------------- Attributes -------------------
	/**
     * Cache Class object used for reflection.
     */
    private HashMap classCache;
    
    
    /**
     * Cache method object.
     */
    private HashMap objectCache;
	
	
	// ---------------------- Constructors ----------------------
	
	 /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContextFacade(ApplicationContext context) {
    	super();
        this.context = context;
        
        classCache = new HashMap();
        objectCache = new HashMap();
        initClassCache();
    }
    
    
    private void initClassCache(){
        Class[] clazz = new Class[]{String.class};
        classCache.put("getContext", clazz);
        classCache.put("getMimeType", clazz);
        classCache.put("getResourcePaths", clazz);
        classCache.put("getResource", clazz);
        classCache.put("getResourceAsStream", clazz);
        classCache.put("getRequestDispatcher", clazz);
        classCache.put("getNamedDispatcher", clazz);
        classCache.put("getServlet", clazz);
        classCache.put("getInitParameter", clazz);
        classCache.put("setAttribute", new Class[]{String.class, Object.class});
        classCache.put("removeAttribute", clazz);
        classCache.put("getRealPath", clazz);
        classCache.put("getAttribute", clazz);
        classCache.put("log", clazz);
    }
    
    
	// ------------------- Instance Variables -------------------
    
    /**
     * Wrapped application context.
     */
    private ApplicationContext context = null;
    
    
	// ------------------ ServletContext Methods ------------------
    
    public Object getAttribute(String name) {
      
    	return context.getAttribute(name);
        
     }
    
    
    public void setAttribute(String name, Object object) {
        
    	context.setAttribute(name, object);
        
    }

    
    public String getRealPath(String path) {
       
    	return context.getRealPath(path);
        
    }
    
    
    
    public String getMimeType(String file) {
        
       return context.getMimeType(file);
        
    }

	
}
