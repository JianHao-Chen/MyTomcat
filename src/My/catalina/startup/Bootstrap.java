package My.catalina.startup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.StringTokenizer;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public final class Bootstrap {

	private static Log log = LogFactory.getLog(Bootstrap.class);
	
	// -------------------------------------------------------------- Constants

    protected static final String CATALINA_HOME_TOKEN = "${catalina.home}";
    protected static final String CATALINA_BASE_TOKEN = "${catalina.base}";
    
    // ------------------------------------------------------- Static Variables
    
    /**
     * Daemon object used by main
     */
    private static Bootstrap daemon = null;
    
    
    
    // -------------------------------------------------------------- Variables
    
    /**
     * Daemon reference.
     */
    private Object catalinaDaemon = null;
    
    
    protected ClassLoader commonLoader = null;
    protected ClassLoader catalinaLoader = null;
    protected ClassLoader sharedLoader = null;
    
    
    // -------------------------------------------------------- Private Methods
    private void initClassLoaders() {
        try {
            commonLoader = createClassLoader("common", null);
            if( commonLoader == null ) {
                // no config file, default to this loader - we might be in a 'single' env.
                commonLoader=this.getClass().getClassLoader();
            }
            catalinaLoader = createClassLoader("server", commonLoader);
            sharedLoader = createClassLoader("shared", commonLoader);
        } catch (Throwable t) {
            log.error("Class loader creation threw exception", t);
            System.exit(1);
        }
    }
    
    
    private ClassLoader createClassLoader(String name, ClassLoader parent)
    throws Exception {
    	
    
    	String value = CatalinaProperties.getProperty(name + ".loader");
        if ((value == null) || (value.equals("")))
            return parent;

        ArrayList repositoryLocations = new ArrayList();
        ArrayList repositoryTypes = new ArrayList();
        int i;
 
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreElements()) {
            String repository = tokenizer.nextToken();

            // Local repository
            boolean replace = false;
            String before = repository;
            while ((i=repository.indexOf(CATALINA_HOME_TOKEN))>=0) {
                replace=true;
                if (i>0) {
                repository = repository.substring(0,i) + getCatalinaHome() 
                    + repository.substring(i+CATALINA_HOME_TOKEN.length());
                } else {
                    repository = getCatalinaHome() 
                        + repository.substring(CATALINA_HOME_TOKEN.length());
                }
            }
            while ((i=repository.indexOf(CATALINA_BASE_TOKEN))>=0) {
                replace=true;
                if (i>0) {
                repository = repository.substring(0,i) + getCatalinaBase() 
                    + repository.substring(i+CATALINA_BASE_TOKEN.length());
                } else {
                    repository = getCatalinaBase() 
                        + repository.substring(CATALINA_BASE_TOKEN.length());
                }
            }
            
            
            if (repository.endsWith("*.jar")) {
                repository = repository.substring
                    (0, repository.length() - "*.jar".length());
                repositoryLocations.add(repository);
                repositoryTypes.add(ClassLoaderFactory.IS_GLOB);
            } else if (repository.endsWith(".jar")) {
                repositoryLocations.add(repository);
                repositoryTypes.add(ClassLoaderFactory.IS_JAR);
            } else {
                repositoryLocations.add(repository);
                repositoryTypes.add(ClassLoaderFactory.IS_DIR);
            }  
        }
        
        String[] locations = (String[]) repositoryLocations.toArray(new String[0]);
        Integer[] types = (Integer[]) repositoryTypes.toArray(new Integer[0]);
 
        ClassLoader classLoader = ClassLoaderFactory.createClassLoader
            (locations, types, parent);
        
        return classLoader;
    }
    
    
    /**
     * Initialize daemon.
     */
    public void init()
        throws Exception
    {
    	 // Set Catalina path
        setCatalinaHome();
        setCatalinaBase();

        initClassLoaders();
        
        Thread.currentThread().setContextClassLoader(catalinaLoader);
        
        Class startupClass =
            catalinaLoader.loadClass
            ("My.catalina.startup.Catalina");
        
        Object startupInstance = startupClass.newInstance();
        
        
        // Set the shared extensions class loader
        String methodName = "setParentClassLoader";
        Class paramTypes[] = new Class[1];
        paramTypes[0] = Class.forName("java.lang.ClassLoader");
        Object paramValues[] = new Object[1];
        paramValues[0] = sharedLoader;
        
        Method method =
            startupInstance.getClass().getMethod(methodName, paramTypes);
        method.invoke(startupInstance, paramValues);

        catalinaDaemon = startupInstance;
    }
    
    
    
    public static void main(String args[]) {
    	
    	daemon = new Bootstrap();
    	
    	try {
            daemon.init();
            
            daemon.setAwait(true);
            daemon.load(args);
            daemon.start();
            
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
    }
    
    
    /**
     * Set flag.
     */
    public void setAwait(boolean await)
        throws Exception {
    	
    	 Class paramTypes[] = new Class[1];
         paramTypes[0] = Boolean.TYPE;
         Object paramValues[] = new Object[1];
         paramValues[0] = new Boolean(await);
         Method method = 
             catalinaDaemon.getClass().getMethod("setAwait", paramTypes);
         method.invoke(catalinaDaemon, paramValues);
    }
    
    
    /**
     * Load daemon.
     */
    private void load(String[] arguments)
        throws Exception {
    	
    	 // Call the load() method
        String methodName = "load";
        Object param[] = null;
        Class paramTypes[] = null;

        Method method = 
            catalinaDaemon.getClass().getMethod(methodName, paramTypes);
        
        method.invoke(catalinaDaemon, param);
    }
    
    
    /**
     * Start the Catalina daemon.
     */
    public void start()
        throws Exception {
    	
    	Method method = catalinaDaemon.getClass().
    						getMethod("start", (Class [] )null);
    	
        method.invoke(catalinaDaemon, (Object [])null);
    }
    
    
    /**
     * Set the <code>catalina.home</code> System property to the current
     * working directory if it has not been set.
     */
    private void setCatalinaHome() {
    	if (System.getProperty("catalina.home") != null)
            return;
    	
    }
    
    /**
     * Set the <code>catalina.base</code> System property to the current
     * working directory if it has not been set.
     */
    private void setCatalinaBase() {
    	if (System.getProperty("catalina.base") != null)
            return;
        if (System.getProperty("catalina.home") != null)
            System.setProperty("catalina.base",
                               System.getProperty("catalina.home"));
        else
            System.setProperty("catalina.base",
                               System.getProperty("user.dir"));
    }
    
    public void setCatalinaHome(String s) {
        System.setProperty( "catalina.home", s );
    }

    public void setCatalinaBase(String s) {
        System.setProperty( "catalina.base", s );
    }
    
    /**
     * Get the value of the catalina.home environment variable.
     */
    public static String getCatalinaHome() {
        return System.getProperty("catalina.home",
                                  System.getProperty("user.dir"));
    }

    /**
     * Get the value of the catalina.base environment variable.
     */
    public static String getCatalinaBase() {
        return System.getProperty("catalina.base", getCatalinaHome());
    }
    
}
