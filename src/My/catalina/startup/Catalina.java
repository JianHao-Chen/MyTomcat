package My.catalina.startup;

/**
 * Should do the same thing as Embedded, but using a server.xml file.
 *
 */
public class Catalina extends Embedded{

	// --------------------- Instance Variables --------------------- 
	/**
     * Pathname to the server configuration file.
     */
    protected String configFile = "conf/server.xml";
    
    /**
     * The shared extensions class loader for this server.
     */
    protected ClassLoader parentClassLoader =
        Catalina.class.getClassLoader();

    
    /**
     * Are we starting a new server?
     */
    protected boolean starting = false;


    /**
     * Are we stopping an existing server?
     */
    protected boolean stopping = false;

    
    // ---------------------------- Properties ----------------------------
    
    public String getConfigFile() {
        return configFile;
    }

    
    /**
     * Set the shared extensions class loader.
     *
     * @param parentClassLoader The shared extensions class loader.
     */
    public void setParentClassLoader(ClassLoader parentClassLoader) {

        this.parentClassLoader = parentClassLoader;     
    }
    
    
    
    
 // ------------------------------------------------------ Protected Methods
    
    protected void initDirs() {
    	/* just make sure :
    		"catalina.home" and "catalina.base" is is absolute path.
    	*/
    }
    
    
    /**
     * Create and configure the Digester we will be using for startup.
     */
    protected Digester createStartDigester() {
    	
    }
    
    
    
    public void load() {
    	
    	 initDirs();
    }

    
}
