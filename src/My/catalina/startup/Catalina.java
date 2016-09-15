package My.catalina.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import My.catalina.Container;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.tomcat.util.digester.ConnectorCreateRule;
import My.tomcat.util.digester.Digester;
import My.tomcat.util.digester.Rule;

/**
 * Should do the same thing as Embedded, but using a server.xml file.
 *
 */
public class Catalina extends Embedded{

	
	private static My.juli.logging.Log log=
        My.juli.logging.LogFactory.getLog( Catalina.class );
	
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
     * Return a File object representing our configuration file.
     */
    protected File configFile() {

        File file = new File(configFile);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"), configFile);
        return (file);

    }

    
    
    
    /**
     * Create and configure the Digester we will be using for startup.
     */
    protected Digester createStartDigester() {
    	
    	// Initialize the digester
    	Digester digester = new Digester();
    	digester.setValidating(false);
        digester.setRulesValidation(true);
        
        HashMap<Class, List<String>> fakeAttributes = 
        	new HashMap<Class, List<String>>();
        
        ArrayList<String> attrs = new ArrayList<String>();
        
        
     // Configure the actions we will be using
        digester.addObjectCreate("Server",
                                 "My.catalina.core.StandardServer",
                                 "className");
        digester.addSetProperties("Server");
        digester.addSetNext("Server",
                            "setServer",
                            "My.catalina.Server");

        digester.addObjectCreate("Server/GlobalNamingResources",
                                 "My.catalina.deploy.NamingResources");
        digester.addSetProperties("Server/GlobalNamingResources");
        digester.addSetNext("Server/GlobalNamingResources",
                            "setGlobalNamingResources",
                            "My.catalina.deploy.NamingResources");

        digester.addObjectCreate("Server/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Listener");
        digester.addSetNext("Server/Listener",
                            "addLifecycleListener",
                            "My.catalina.LifecycleListener");

        digester.addObjectCreate("Server/Service",
                                 "My.catalina.core.StandardService",
                                 "className");
        digester.addSetProperties("Server/Service");
        digester.addSetNext("Server/Service",
                            "addService",
                            "My.catalina.Service");

        digester.addObjectCreate("Server/Service/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Service/Listener");
        digester.addSetNext("Server/Service/Listener",
                            "addLifecycleListener",
                            "My.catalina.LifecycleListener");

        //Executor
        digester.addObjectCreate("Server/Service/Executor",
                         "My.catalina.core.StandardThreadExecutor",
                         "className");
        digester.addSetProperties("Server/Service/Executor");

        digester.addSetNext("Server/Service/Executor",
                            "addExecutor",
                            "My.catalina.Executor");

        
        digester.addRule("Server/Service/Connector",
                         new ConnectorCreateRule());
        
        digester.addRule("Server/Service/Connector", 
                         new SetAllPropertiesRule(new String[]{"executor"}));
       
        digester.addSetNext("Server/Service/Connector",
                            "addConnector",
                            "My.catalina.connector.Connector");
        
        


        digester.addObjectCreate("Server/Service/Connector/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Service/Connector/Listener");
        digester.addSetNext("Server/Service/Connector/Listener",
                            "addLifecycleListener",
                            "My.catalina.LifecycleListener");
        
        
        // Add RuleSets for nested elements
        digester.addRuleSet(new NamingRuleSet("Server/GlobalNamingResources/"));
        digester.addRuleSet(new EngineRuleSet("Server/Service/"));
        digester.addRuleSet(new HostRuleSet("Server/Service/Engine/"));
        digester.addRuleSet(new ContextRuleSet("Server/Service/Engine/Host/"));
    //    digester.addRuleSet(ClusterRuleSetFactory.getClusterRuleSet("Server/Service/Engine/Host/Cluster/"));
        digester.addRuleSet(new NamingRuleSet("Server/Service/Engine/Host/Context/"));
        
        
        // When the 'engine' is found, set the parentClassLoader.
   //     digester.addRule("Server/Service/Engine",
   //                      new SetParentClassLoaderRule(parentClassLoader));
    //    digester.addRuleSet(ClusterRuleSetFactory.getClusterRuleSet("Server/Service/Engine/Cluster/"));
        
        return (digester);
    }
    
    
    /**
     * Await and shutdown.
     */
    public void await() {

        getServer().await();

    }
    
    /**
     * Stop an existing server instance.
     */
    public void stop() {
    	
    	 // Shut down the server
        if (getServer() instanceof Lifecycle) {
            try {
                ((Lifecycle) getServer()).stop();
            } catch (LifecycleException e) {
                log.error("Catalina.stop", e);
            }
        }
    }
    
    
    public void load() {
    	
    	 initDirs();
    	 
    	 
    	 // Create and execute our Digester
         Digester digester = createStartDigester();
    	 
    	 InputSource inputSource = null;
         InputStream inputStream = null;
         File file = null;
         
         try {
        	 
        	 file = configFile();
        	 inputStream = new FileInputStream(file);
             inputSource = new InputSource("file://" + file.getAbsolutePath());
         }catch (Exception e) {
             ;
         }
         
         
         
         try {
        	 inputSource.setByteStream(inputStream);
             digester.push(this);
             digester.parse(inputSource);
             inputStream.close();
         }catch (Exception e) {
             log.warn("Catalina.start using "
                     + getConfigFile() + ": " , e);
             return;
         }
         
         
         
         // Start the new server
         if (getServer() instanceof Lifecycle) {
        	 try {
                 getServer().initialize();
                 
        	 }catch (LifecycleException e) {
        		 
        		 log.error("Catalina.start", e);
        	 }
         }
         
    }
    
    
    
    
    /**
     * Start a new server instance.
     */
    public void start() {
    	
    	 if (getServer() == null) {
             load();
         }

         if (getServer() == null) {
             log.fatal("Cannot start server. Server instance is not configured.");
             return;
         }
         
         
      // Start the new server
         if (getServer() instanceof Lifecycle) {
             try {
                 ((Lifecycle) getServer()).start();
             } catch (LifecycleException e) {
                 log.error("Catalina.start: ", e);
             }
         }
         
         if (await) {
        	 await();
             stop();
         }
         
         
    }
    
    
    
}
    
    
    
    
 // ------------------------------------------------------------ Private Classes


    /**
     * Rule that sets the parent class loader for the top object on the stack,
     * which must be a <code>Container</code>.
     */

    final class SetParentClassLoaderRule extends Rule {

        public SetParentClassLoaderRule(ClassLoader parentClassLoader) {

            this.parentClassLoader = parentClassLoader;

        }

        ClassLoader parentClassLoader = null;

        public void begin(String namespace, String name, Attributes attributes)
            throws Exception {

         //   if (digester.getLogger().isDebugEnabled())
        //       digester.getLogger().debug("Setting parent class loader");

            Container top = (Container) digester.peek();
            top.setParentClassLoader(parentClassLoader);

        }

    }
