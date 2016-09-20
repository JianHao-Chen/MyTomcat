package My.tomcat.util.http.mapper;

import org.omg.CORBA.Context;

import My.catalina.Host;

public final class Mapper {

	// ------------------- Instance Variables -------------------
	
	/**
     * Array containing the virtual hosts definitions.
     */
    protected Host[] hosts = new Host[0];
    
    /**
     * Default host name.
     */
    protected String defaultHostName = null;
    
    
    /**
     * Context associated with this wrapper, used for wrapper mapping.
     */
    protected Context context = new Context();
    
	// --------------------- Public Methods ---------------------
    
    /**
     * Get default host.
     *
     * @return Default host name
     */
    public String getDefaultHostName() {
        return defaultHostName;
    }


    /**
     * Set default host.
     *
     * @param defaultHostName Default host name
     */
    public void setDefaultHostName(String defaultHostName) {
        this.defaultHostName = defaultHostName;
    }
    
    
    
    /**
     * Add a new host to the mapper.
     *
     * @param name Virtual host name
     * @param host Host object
     */
    public synchronized void addHost(String name, String[] aliases,
                                     Object host) {
    	
    }
    
    
    
	// ---------------------- MapElement Inner Class -----------
    
    protected static abstract class MapElement {
    	public String name = null;
        public Object object = null;
    }
    
    
	// ------------------ Host Inner Class ------------------
    
    protected static final class Host
    extends MapElement {
    	public ContextList contextList = null;
    }
    
    
    
    
	// ---------------- ContextList Inner Class ----------------
    
    protected static final class ContextList {

        public Context[] contexts = new Context[0];
        public int nesting = 0;

    }
    
    
	// ----------------- Context Inner Class -----------------
    
    protected static final class Context
    extends MapElement {
    	
    	 public String path = null;
         public String[] welcomeResources = new String[0];
         public javax.naming.Context resources = null;
         public Wrapper defaultWrapper = null;
         public Wrapper[] exactWrappers = new Wrapper[0];
         public Wrapper[] wildcardWrappers = new Wrapper[0];
         public Wrapper[] extensionWrappers = new Wrapper[0];
         public int nesting = 0;
    }
    
    
	// ---------------- Wrapper Inner Class ----------------
    
    protected static class Wrapper
    extends MapElement {

    	public String path = null;
    	public boolean jspWildCard = false;
    }
    
	
}
