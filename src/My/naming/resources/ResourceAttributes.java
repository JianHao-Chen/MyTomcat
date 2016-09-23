package My.naming.resources;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class ResourceAttributes{

	// --------------------- Constructors ---------------------
	
	/**
     * Default constructor.
     */
    public ResourceAttributes() {
    }
    
    
    /**
     * Merges with another attribute set.
     */
    public ResourceAttributes(Attributes attributes) {
        this.attributes = attributes;
    }
    
    
	// ------------------- Instance Variables -------------------
    
    /**
     * External attributes.
     */
    protected Attributes attributes = null;


	
    
}
