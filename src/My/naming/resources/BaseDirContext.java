package My.naming.resources;

import java.util.Hashtable;

import javax.naming.directory.DirContext;

public abstract class BaseDirContext implements DirContext{

	
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
	
}
