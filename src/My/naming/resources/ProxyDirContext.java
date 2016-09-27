package My.naming.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class ProxyDirContext implements DirContext {

	// -------------------- Constants --------------------


    public static final String CONTEXT = "context";
    public static final String HOST = "host";
    
    
    
    // --------------------- Constructors ---------------------
    
    /**
     * Builds a proxy directory context using the given environment.
     */
    public ProxyDirContext(Hashtable env, DirContext dirContext) {
    	
    	 this.env = env;
    	 this.dirContext = dirContext;
    	 
    	 if (dirContext instanceof BaseDirContext) {
    		// Initialize parameters based on the associated dir context, like
            // the caching policy.
    		 
    		 BaseDirContext baseDirContext = (BaseDirContext) dirContext;
    		 
    		 if (baseDirContext.isCached()) {
    			 try {
    				 cache = (ResourceCache)
    				 		Class.forName(cacheClassName).newInstance();
    			 }
    			 catch (Exception e) {
    				 
    			 }

    			 cache.setCacheMaxSize(baseDirContext.getCacheMaxSize());
    			 cacheTTL = baseDirContext.getCacheTTL();
    			 cacheObjectMaxSize = baseDirContext.getCacheObjectMaxSize();
    			 
    			 // cacheObjectMaxSize must be less than cacheMaxSize
                 // Set a sensible limit
                 if (cacheObjectMaxSize > baseDirContext.getCacheMaxSize()/20) {
                     cacheObjectMaxSize = baseDirContext.getCacheMaxSize()/20;
                 }
    		 }
    		 
    		 hostName = (String) env.get(HOST);
    		 contextName = (String) env.get(CONTEXT);
    		 
    	 }
    	 
    	 
    }
    
    
    
	// --------------------- Instance Variables ---------------------
    
    /**
     * Proxy DirContext (either this or the real proxy).
     */
    protected ProxyDirContext proxy = this;
    
    /**
     * Environment.
     */
    protected Hashtable env;
    
    /**
     * Associated DirContext.
     */
    protected DirContext dirContext;
    
    
    /**
     * Host name.
     */
    protected String hostName;
    
    /**
     * Context name.
     */
    protected String contextName;
    
    /**
     * Cache class.
     */
    protected String cacheClassName = 
        "My.naming.resources.ResourceCache";
    
    /**
     * Cache.
     */
    protected ResourceCache cache = null;
    
    /**
     * Cache TTL.
     */
    protected int cacheTTL = 5000; // 5s
    
    /**
     * Max size of resources which will have their content cached.
     */
    protected int cacheObjectMaxSize = 512; // 512 KB
    
    
    /**
     * Non cacheable resources.
     */
    protected String[] nonCacheable = { "/WEB-INF/lib/", "/WEB-INF/classes/" };

    
	// ------------------- Public Methods -------------------
    
    /**
     * Get the cache used for this context.
     */
    public ResourceCache getCache() {
        return cache;
    }
    
    
    /**
     * Return the actual directory context we are wrapping.
     */
    public DirContext getDirContext() {
        return this.dirContext;
    }


    /**
     * Return the document root for this component.
     */
    public String getDocBase() {
        if (dirContext instanceof BaseDirContext)
            return ((BaseDirContext) dirContext).getDocBase();
        else
            return "";
    }
    
    
    
    /**
     * Return the host name.
     */
    public String getHostName() {
        return this.hostName;
    }
    
    
    /**
     * Return the context name.
     */
    public String getContextName() {
        return this.contextName;
    }
    
    
    
	// ---------------------- Context Methods ----------------------
    
    /**
     * Retrieves the named object.
     * 
     * @param name the name of the object to look up
     * @return the object bound to name
     * @exception NamingException if a naming exception is encountered
     */
    public Object lookup(String name)
        throws NamingException {
    	
    	CacheEntry entry = cacheLookup(name);
    	
    	if (entry != null) {
    		if (!entry.exists) {
    			 //throw notFoundException;
    		}
    		if (entry.resource != null) {
    			return entry.resource;
    		}
    		else {
                return entry.context;
            }
    		
    	}
    	return null;
    }
    
    
    /**
     * Lookup in cache.
     */
    protected CacheEntry cacheLookup(String name) {
    	
    	if (cache == null)
            return (null);
    	
    	if (name == null)
            name = "";
    	
    	for (int i = 0; i < nonCacheable.length; i++) {
    		if (name.startsWith(nonCacheable[i])) {
                return (null);
            }
    	}
    	
    	CacheEntry cacheEntry = cache.lookup(name);
    	if (cacheEntry == null) {
    		cacheEntry = new CacheEntry();
    		cacheEntry.name = name;
    		// Load entry
            cacheLoad(cacheEntry);
    	}
    	else {
    		
    	}
    	return (cacheEntry);
    }
    
    
    
    
    /**
     * Retrieves the named object as a cache entry, without any exception.
     */
    public CacheEntry lookupCache(String name) {
    	
    	 CacheEntry entry = cacheLookup(name);
    	 return null;
    }
    

    /**
     * Retrieves the named object. If name is empty, returns a new instance 
     * of this context (which represents the same naming context as this 
     * context, but its environment may be modified independently and it may 
     * be accessed concurrently).
     * 
     * @param name the name of the object to look up
     * @return the object bound to name
     * @exception NamingException if a naming exception is encountered
     */
	public Object lookup(Name name) throws NamingException {
		
		return null;
	}
	

	/**
     * Load entry into cache.
     */
    protected void cacheLoad(CacheEntry entry) {
    	
    	String name = entry.name;
    	
    	boolean exists = true;
    	 
    	// Retrieving attributes
        if (entry.attributes == null) {
        	
        	try {
        		Attributes attributes = dirContext.getAttributes(entry.name);
        		if (!(attributes instanceof ResourceAttributes)) {
        			
        		}
        		else
        			entry.attributes = (ResourceAttributes) attributes;
        	}catch (NamingException e) {
        		
        	}
        }
        
        
        // Retriving object
        if ((exists) && (entry.resource == null) && (entry.context == null)) {
        	try {
        		Object object = dirContext.lookup(name);
        		
        		 if (object instanceof InputStream) {
                     entry.resource = new Resource((InputStream) object);
                 } else if (object instanceof DirContext) {
                     entry.context = (DirContext) object;
                 } else if (object instanceof Resource) {
                     entry.resource = (Resource) object;
                 } else {
                     entry.resource = new Resource(new ByteArrayInputStream
                         (object.toString().getBytes()));
                 }
        		 
        	}catch (NamingException e) {
        		exists = false;
        	}
        	
        	
        }
        
       
        
        
        // Set existence flag
        entry.exists = exists;
        
        // Set timestamp
        entry.timestamp = System.currentTimeMillis() + cacheTTL;
        
        // Add new entry to cache
        synchronized (cache) {
        	// Check cache size, and remove elements if too big
            if ((cache.lookup(name) == null) && cache.allocate(entry.size)) {
                cache.load(entry);
            }
        }
    }
	
	
	
	
	

	@Override
	public void bind(Name name, Object obj) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void bind(String name, Object obj) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void rebind(String name, Object obj) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void unbind(Name name) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void unbind(String name) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void rename(Name oldName, Name newName) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void rename(String oldName, String newName) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void destroySubcontext(Name name) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void destroySubcontext(String name) throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Context createSubcontext(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Context createSubcontext(String name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object lookupLink(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object lookupLink(String name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NameParser getNameParser(String name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Name composeName(Name name, Name prefix) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String composeName(String name, String prefix)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object removeFromEnvironment(String propName) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void close() throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getNameInNamespace() throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Attributes getAttributes(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Attributes getAttributes(String name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Attributes getAttributes(Name name, String[] attrIds)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Attributes getAttributes(String name, String[] attrIds)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void modifyAttributes(Name name, int mod_op, Attributes attrs)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void modifyAttributes(String name, int mod_op, Attributes attrs)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void modifyAttributes(Name name, ModificationItem[] mods)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void modifyAttributes(String name, ModificationItem[] mods)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void bind(Name name, Object obj, Attributes attrs)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void bind(String name, Object obj, Attributes attrs)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void rebind(Name name, Object obj, Attributes attrs)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void rebind(String name, Object obj, Attributes attrs)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public DirContext createSubcontext(Name name, Attributes attrs)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public DirContext createSubcontext(String name, Attributes attrs)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public DirContext getSchema(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public DirContext getSchema(String name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public DirContext getSchemaClassDefinition(Name name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public DirContext getSchemaClassDefinition(String name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<SearchResult> search(Name name,
			Attributes matchingAttributes, String[] attributesToReturn)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<SearchResult> search(String name,
			Attributes matchingAttributes, String[] attributesToReturn)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<SearchResult> search(Name name,
			Attributes matchingAttributes) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<SearchResult> search(String name,
			Attributes matchingAttributes) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<SearchResult> search(Name name, String filter,
			SearchControls cons) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<SearchResult> search(String name, String filter,
			SearchControls cons) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<SearchResult> search(Name name, String filterExpr,
			Object[] filterArgs, SearchControls cons) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NamingEnumeration<SearchResult> search(String name,
			String filterExpr, Object[] filterArgs, SearchControls cons)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}


    
    
    
    
    
}
