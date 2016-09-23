package My.naming.resources;

public class ResourceCache {

	// -------------------- Instance Variables -------------------
	
	/**
     * Max size of resources which will have their content cached.
     */
    protected int cacheMaxSize = 10240; // 10 MB
	
	
	
	// -------------------- Properties --------------------
	
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
    
    
}
