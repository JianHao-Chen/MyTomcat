package My.catalina.deploy;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Holds and manages the naming resources defined in the J2EE Enterprise 
 * Naming Context and their associated JNDI context.
 */

public class NamingResources implements Serializable{

	// ----------------------------------------------------------- Constructors


    /**
     * Create a new NamingResources instance.
     */
    public NamingResources() {
    }


    // ----------------------------------------------------- Instance Variables
    
    /**
     * Associated container object.
     */
    private Object container = null;
    
    
    /**
     * List of naming entries, keyed by name. The value is the entry type, as
     * declared by the user.
     */
    private Hashtable entries = new Hashtable();
    
    
    
    
    
 // ------------------------------------------------------------- Properties


    /**
     * Get the container with which the naming resources are associated.
     */
    public Object getContainer() {
        return container;
    }


    /**
     * Set the container with which the naming resources are associated.
     */
    public void setContainer(Object container) {
        this.container = container;
    }
    
}
