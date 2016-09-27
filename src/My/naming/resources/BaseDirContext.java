package My.naming.resources;

import java.util.Hashtable;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

public abstract class BaseDirContext implements DirContext{

	// ------------------ Constructors --------------------
	
	/**
     * Builds a base directory context.
     */
    public BaseDirContext() {
        this.env = new Hashtable();
    }


    /**
     * Builds a base directory context using the given environment.
     */
    public BaseDirContext(Hashtable env) {
        this.env = env;
    }
	
	// ----------------- Instance Variables -----------------
	
	 /**
     * The document base path.
     */
    protected String docBase = null;
    
    /**
     * Environment.
     */
    protected Hashtable env;
    
    
    /**
     * Cached.
     */
    protected boolean cached = true;
	
    
    /**
     * Cache TTL.
     */
    protected int cacheTTL = 5000; // 5s
	
    
    /**
     * Max size of cache for resources.
     */
    protected int cacheMaxSize = 10240; // 10 MB
    
    
    /**
     * Max size of resources that will be content cached.
     */
    protected int cacheObjectMaxSize = 512; // 512 K
    
	
	// --------------------- Properties ---------------------
	
	 /**
     * Return the document root for this component.
     */
    public String getDocBase() {
        return (this.docBase);
    }


    /**
     * Set the document root for this component.
     *
     * @param docBase The new document root
     *
     * @exception IllegalArgumentException if the specified value is not
     *  supported by this implementation
     * @exception IllegalArgumentException if this would create a
     *  malformed URL
     */
    public void setDocBase(String docBase) {

        // Validate the format of the proposed document root
        if (docBase == null)
            throw new IllegalArgumentException
                ("resources.null");

        // Change the document root property
        this.docBase = docBase;

    }
    
    
	
	/**
     * Set cached.
     */
    public void setCached(boolean cached) {
        this.cached = cached;
    }


    /**
     * Is cached ?
     */
    public boolean isCached() {
        return cached;
    }
    
    
    /**
     * Set cache TTL.
     */
    public void setCacheTTL(int cacheTTL) {
        this.cacheTTL = cacheTTL;
    }


    /**
     * Get cache TTL.
     */
    public int getCacheTTL() {
        return cacheTTL;
    }
    
    
    /**
     * Return the maximum size of the cache in KB.
     */
    public int getCacheMaxSize() {
        return cacheMaxSize;
    }


    /**
     * Set the maximum size of the cache in KB.
     */
    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }
    
    
    /**
     * Return the maximum size of objects to be cached in KB.
     */
    public int getCacheObjectMaxSize() {
        return cacheObjectMaxSize;
    }


    /**
     * Set the maximum size of objects to be placed the cache in KB.
     */
    public void setCacheObjectMaxSize(int cacheObjectMaxSize) {
        this.cacheObjectMaxSize = cacheObjectMaxSize;
    }
    
    
	// -------------------- Public Methods --------------------
    /**
     * Allocate resources for this directory context.
     */
    public void allocate() {
        ; // No action taken by the default implementation
    }


    /**
     * Release any resources allocated for this directory context.
     */
    public void release() {
        ; // No action taken by the default implementation
    }
    
    
    
	// ------------------ DirContext Methods ------------------
    /**
     * Retrieves all of the attributes associated with a named object. 
     * 
     * @return the set of attributes associated with name. 
     * Returns an empty attribute set if name has no attributes; never null.
     * @param name the name of the object from which to retrieve attributes
     * @exception NamingException if a naming exception is encountered
     */
    public Attributes getAttributes(Name name)
        throws NamingException {
        return getAttributes(name.toString());
    }


    /**
     * Retrieves all of the attributes associated with a named object.
     * 
     * @return the set of attributes associated with name
     * @param name the name of the object from which to retrieve attributes
     * @exception NamingException if a naming exception is encountered
     */
    public Attributes getAttributes(String name)
        throws NamingException {
        return getAttributes(name, null);
    }


    /**
     * Retrieves selected attributes associated with a named object. 
     * See the class description regarding attribute models, attribute type 
     * names, and operational attributes.
     * 
     * @return the requested attributes; never null
     * @param name the name of the object from which to retrieve attributes
     * @param attrIds the identifiers of the attributes to retrieve. null 
     * indicates that all attributes should be retrieved; an empty array 
     * indicates that none should be retrieved
     * @exception NamingException if a naming exception is encountered
     */
    public Attributes getAttributes(Name name, String[] attrIds)
        throws NamingException {
        return getAttributes(name.toString(), attrIds);
    }
    
    
    /**
     * Retrieves selected attributes associated with a named object.
     * 
     * @return the requested attributes; never null
     * @param name the name of the object from which to retrieve attributes
     * @param attrIds the identifiers of the attributes to retrieve. null 
     * indicates that all attributes should be retrieved; an empty array 
     * indicates that none should be retrieved
     * @exception NamingException if a naming exception is encountered
     */
    public abstract Attributes getAttributes(String name, String[] attrIds)
        throws NamingException;
	
}
