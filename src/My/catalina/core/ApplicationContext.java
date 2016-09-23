package My.catalina.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;


/**
 * Standard implementation of <code>ServletContext</code> that represents
 * a web application's execution environment.  An instance of this class is
 * associated with each instance of <code>StandardContext</code>.
 */

public class ApplicationContext implements ServletContext{

	
	// ----------------------- Constructors -----------------------
	/**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContext(String basePath, StandardContext context) {
        super();
        this.context = context;
        this.basePath = basePath;
    }
	
    
    //---------------------- Instance Variables ----------------------
	
    /**
     * The context attributes for this context.
     */
    protected Map attributes = new ConcurrentHashMap();

    
    /**
     * List of read only attributes for this context.
     */
    private Map readOnlyAttributes = new ConcurrentHashMap();
    
    
    /**
     * The Context instance with which we are associated.
     */
    private StandardContext context = null;
    
    /**
     * The facade around this object.
     */
    private ServletContext facade = new ApplicationContextFacade(this);
    
    
    /**
     * Base path.
     */
    private String basePath = null;
    
    
    
    
    
    
    
	// ------------------------ Package Methods ------------------------
    
    protected StandardContext getContext() {
        return this.context;
    }
    
    protected Map getReadonlyAttributes() {
        return this.readOnlyAttributes;
    }
    

    /**
     * Return the facade associated with this ApplicationContext.
     */
    protected ServletContext getFacade() {

        return (this.facade);

    }
    
    
    
    
	// ------------------ ServletContext Methods -------------------
    
    /**
     * Return the value of the specified context attribute, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the context attribute to return
     */
    public Object getAttribute(String name) {

        return (attributes.get(name));

    }
    
    /**
     * Bind the specified value with the specified context attribute name,
     * replacing any existing value for that name.
     *
     * @param name Attribute name to be bound
     * @param value New attribute value to be bound
     */
    public void setAttribute(String name, Object value) {
    	// Name cannot be null
        if (name == null)
            throw new IllegalArgumentException
                ("applicationContext.setAttribute.namenull");

        // Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }
        
        
        Object oldValue = null;
        boolean replaced = false;
        
        // Add or replace the specified attribute
        // Check for read only attribute
        if (readOnlyAttributes.containsKey(name))
            return;
        
        oldValue = attributes.get(name);
        if (oldValue != null)
            replaced = true;
        
        attributes.put(name, value);
        
        // Notify interested application event listeners
        Object listeners[] = context.getApplicationEventListeners();
        if ((listeners == null) || (listeners.length == 0))
            return;
        
        /*
         *  below is the code for :
         *  	notify listeners
         *  
         *   implements latter.
         */
        
    }
    
    
    /**
     * Set an attribute as read only.
     */
    void setAttributeReadOnly(String name) {

        if (attributes.containsKey(name))
            readOnlyAttributes.put(name, name);
    }
    
    
    /**
     * Remove the context attribute with the specified name, if any.
     *
     * @param name Name of the context attribute to be removed
     */
    public void removeAttribute(String name) {
    	 Object value = null;
         boolean found = false;
         
         // Remove the specified attribute
         // Check for read only attribute
         if (readOnlyAttributes.containsKey(name))
             return;
         
         found = attributes.containsKey(name);

         if (found) {
             value = attributes.get(name);
             attributes.remove(name);
         } else {
             return;
         }
         
         // Notify interested application event listeners
         
         /*
          *  implement latter.
          */
    }
    
    
}
