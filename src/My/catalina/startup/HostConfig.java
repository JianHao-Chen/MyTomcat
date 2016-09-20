package My.catalina.startup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.management.ObjectName;

import My.catalina.Container;
import My.catalina.Engine;
import My.catalina.Host;
import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleListener;
import My.catalina.core.StandardHost;
import My.tomcat.util.digester.Digester;
import My.tomcat.util.modeler.Registry;

/**
 * Startup event listener for a <b>Host</b> that configures the properties
 * of that Host, and the associated defined contexts.
 */

public class HostConfig implements LifecycleListener {

	 protected static My.juli.logging.Log log=
		 My.juli.logging.LogFactory.getLog( HostConfig.class );
	 
	// -------------------- Instance Variables --------------------
	 
	 /**
	     * App base.
	     */
	    protected File appBase = null;


	    /**
	     * Config base.
	     */
	    protected File configBase = null;


	    /**
	     * The Java class name of the Context configuration class we should use.
	     */
	    protected String configClass = "org.apache.catalina.startup.ContextConfig";


	    /**
	     * The Java class name of the Context implementation we should use.
	     */
	    protected String contextClass = "org.apache.catalina.core.StandardContext";


	    /**
	     * The Host we are associated with.
	     */
	    protected Host host = null;
	    
	    /**
	     * Should we deploy XML Context config files?
	     */
	    protected boolean deployXML = false;


	    /**
	     * Should we unpack WAR files when auto-deploying applications in the
	     * <code>appBase</code> directory?
	     */
	    protected boolean unpackWARs = false;


	    /**
	     * Map of deployed applications.
	     */
	    protected HashMap deployed = new HashMap();

	    
	    /**
	     * The JMX ObjectName of this component.
	     */
	    protected ObjectName oname = null;
	    
	    
	    
	    /**
	     * List of applications which are being serviced, and shouldn't be 
	     * deployed/undeployed/redeployed at the moment.
	     */
	    protected ArrayList serviced = new ArrayList();
	    

	    /**
	     * Attribute value used to turn on/off XML validation
	     */
	    protected boolean xmlValidation = false;


	    /**
	     * Attribute value used to turn on/off XML namespace awarenes.
	     */
	    protected boolean xmlNamespaceAware = false;


	    /**
	     * The <code>Digester</code> instance used to parse context descriptors.
	     */
	    protected static Digester digester = createDigester();

	    /**
	     * The list of Wars in the appBase to be ignored because they are invalid
	     * (e.g. contain /../ sequences).
	     */
	    protected Set<String> invalidWars = new HashSet<String>();
	    
	    
	    
	// --------------------- Properties ---------------------
	    /**
	     * Return the Context configuration class name.
	     */
	    public String getConfigClass() {

	        return (this.configClass);

	    }


	    /**
	     * Set the Context configuration class name.
	     *
	     * @param configClass The new Context configuration class name.
	     */
	    public void setConfigClass(String configClass) {

	        this.configClass = configClass;

	    }


	    /**
	     * Return the Context implementation class name.
	     */
	    public String getContextClass() {

	        return (this.contextClass);

	    }


	    /**
	     * Set the Context implementation class name.
	     *
	     * @param contextClass The new Context implementation class name.
	     */
	    public void setContextClass(String contextClass) {

	        this.contextClass = contextClass;

	    }


	    /**
	     * Return the deploy XML config file flag for this component.
	     */
	    public boolean isDeployXML() {

	        return (this.deployXML);

	    }


	    /**
	     * Set the deploy XML config file flag for this component.
	     *
	     * @param deployXML The new deploy XML flag
	     */
	    public void setDeployXML(boolean deployXML) {

	        this.deployXML= deployXML;

	    }


	    /**
	     * Return the unpack WARs flag.
	     */
	    public boolean isUnpackWARs() {

	        return (this.unpackWARs);

	    }


	    /**
	     * Set the unpack WARs flag.
	     *
	     * @param unpackWARs The new unpack WARs flag
	     */
	    public void setUnpackWARs(boolean unpackWARs) {

	        this.unpackWARs = unpackWARs;

	    }
	    
	    
	     /**
	     * Set the validation feature of the XML parser used when
	     * parsing xml instances.
	     * @param xmlValidation true to enable xml instance validation
	     */
	    public void setXmlValidation(boolean xmlValidation){
	        this.xmlValidation = xmlValidation;
	    }

	    /**
	     * Get the server.xml &lt;host&gt; attribute's xmlValidation.
	     * @return true if validation is enabled.
	     *
	     */
	    public boolean getXmlValidation(){
	        return xmlValidation;
	    }

	    /**
	     * Get the server.xml &lt;host&gt; attribute's xmlNamespaceAware.
	     * @return true if namespace awarenes is enabled.
	     *
	     */
	    public boolean getXmlNamespaceAware(){
	        return xmlNamespaceAware;
	    }


	    /**
	     * Set the namespace aware feature of the XML parser used when
	     * parsing xml instances.
	     * @param xmlNamespaceAware true to enable namespace awareness
	     */
	    public void setXmlNamespaceAware(boolean xmlNamespaceAware){
	        this.xmlNamespaceAware=xmlNamespaceAware;
	    }    

	    
	 // ------------------------ Public Methods ------------------------
	   
	    /**
	     * Check status of all webapps.
	     */
	    protected void check() {
	    	
	    }
	    
	    /**
	     * Process the START event for an associated Host.
	     */
	    public void lifecycleEvent(LifecycleEvent event) {
	    	
	    	if (event.getType().equals(Lifecycle.PERIODIC_EVENT))
	    		check();
	    	
	    	// Identify the host we are associated with
	    	try {
	    		host = (Host) event.getLifecycle();
	    		if (host instanceof StandardHost) {
	    			setDeployXML(((StandardHost)host).isDeployXML());
	    			setUnpackWARs(((StandardHost) host).isUnpackWARs());
	    			setXmlNamespaceAware(((StandardHost) host).getXmlNamespaceAware());
	                setXmlValidation(((StandardHost) host).getXmlValidation());
	    		}
	    	}catch (ClassCastException e) {
	    		 return;
	    	}
	    	
	    	
	    	// Process the event that has occurred
	    	if (event.getType().equals(Lifecycle.START_EVENT))
	    		start();
	    	else if (event.getType().equals(Lifecycle.STOP_EVENT))
	            stop();
	    	
	    	
	    }

	    
	    
	    /**
	     * Add a serviced application to the list.
	     */
	    public synchronized void addServiced(String name) {
	        serviced.add(name);
	    }
	    
	    
	    /**
	     * Is application serviced ?
	     * @return state of the application
	     */
	    public synchronized boolean isServiced(String name) {
	        return (serviced.contains(name));
	    }
	    
	    
	    /**
	     * Removed a serviced application from the list.
	     */
	    public synchronized void removeServiced(String name) {
	        serviced.remove(name);
	    }
	    
	    
	    
	 // ---------------- Protected Methods ----------------
	    /**
	     * Create the digester which will be used to parse context config files.
	     */
	    protected static Digester createDigester() {
	    	
	    	 Digester digester = new Digester();
	         digester.setValidating(false);
	         // Add object creation rule
	         digester.addObjectCreate("Context", "My.catalina.core.StandardContext",
	             "className");
	         // Set the properties on that object (it doesn't matter if extra 
	         // properties are set)
	         digester.addSetProperties("Context");
	         return (digester);
	    }
	    
	    
	    /**
	     * Return a File object representing the "application root" directory
	     * for our associated Host.
	     */
	    protected File appBase() {

	        if (appBase != null) {
	            return appBase;
	        }

	        File file = new File(host.getAppBase());
	        if (!file.isAbsolute())
	            file = new File(System.getProperty("catalina.base"),
	                            host.getAppBase());
	        try {
	            appBase = file.getCanonicalFile();
	        } catch (IOException e) {
	            appBase = file;
	        }
	        return (appBase);

	    }
	    
	    
	    
	    /**
	     * Return a File object representing the "configuration root" directory
	     * for our associated Host.
	     */
	    protected File configBase() {

	        if (configBase != null) {
	            return configBase;
	        }

	        File file = new File(System.getProperty("catalina.base"), "conf");
	        Container parent = host.getParent();
	        if ((parent != null) && (parent instanceof Engine)) {
	            file = new File(file, parent.getName());
	        }
	        file = new File(file, host.getName());
	        try {
	            configBase = file.getCanonicalFile();
	        } catch (IOException e) {
	            configBase = file;
	        }
	        return (configBase);

	    }
	    
	    
	    /**
	     * Filter the list of application file paths to remove those that match
	     * the regular expression defined by {@link Host#getDeployIgnore()}.
	     *  
	     * @param unfilteredAppPaths    The list of application paths to filtert
	     * 
	     * @return  The filtered list of application paths
	     */
	    protected String[] filterAppPaths(String[] unfilteredAppPaths) {
	    	
	    	// currently, just return;
	    	return unfilteredAppPaths;
	    }
	    
	    
	    /**
	     * Deploy XML context descriptors.
	     */
	    protected void deployDescriptors(File configBase, String[] files) {
	    	 if (files == null)
	             return;
	    	 
	    	 // implements latter.
	    }
	    
	    
	    /**
	     * Deploy WAR files.
	     */
	    protected void deployWARs(File appBase, String[] files) {
	    	if (files == null)
	            return;
	    	
	    	// implements latter.
	    	
	    }
	    
	    
	    /**
	     * Check if a webapp is already deployed in this host.
	     */
	    protected boolean deploymentExists(String contextPath) {
	    	
	    	return (deployed.containsKey(contextPath) || (host.findChild(contextPath) != null));
	    }
	    
	    
	    
	    
	    
	    protected void deployDirectory(String contextPath, File dir, String file) {
	    	
	    	DeployedApplication deployedApp = new DeployedApplication(contextPath);
	    	
	    	if (deploymentExists(contextPath))
	    		return;
	    	
	    	
	    }
	    
	    
	    /**
	     * Deploy directories.
	     */
	    protected void deployDirectories(File appBase, String[] files) {
	    	 if (files == null)
	             return;
	    	 
	    	 for (int i = 0; i < files.length; i++) {
	    		 
	    		 if (files[i].equalsIgnoreCase("META-INF"))
	                 continue;
	             if (files[i].equalsIgnoreCase("WEB-INF"))
	                 continue;
	             
	             File dir = new File(appBase, files[i]);
	             
	             if (dir.isDirectory()) {
	            	 
	            	// Calculate the context path and make sure it is unique
	            	 String contextPath = "/" + files[i].replace('#','/');
	                 if (files[i].equals("ROOT"))
	                     contextPath = "";

	                 if (isServiced(contextPath))
	                     continue;
	                 
	                 deployDirectory(contextPath, dir, files[i]);
	             }
	             
	    	 }
	    }
	    
	    
	    /**
	     * Deploy applications for any directories or WAR files that are found
	     * in our "application root" directory.
	     */
	    protected void deployApps() {
	    	
	    	File appBase = appBase();
	    	File configBase = configBase();
	    	String[] filteredAppPaths = filterAppPaths(appBase.list());
	    
	    	// Deploy XML descriptors from configBase
	    	deployDescriptors(configBase, configBase.list());
	    
	    	// Deploy WARs, and loop if additional descriptors are found
	    	deployWARs(appBase, filteredAppPaths);
	    	
	    	// Deploy expanded folders
	        deployDirectories(appBase, filteredAppPaths);
	    }
	    
	    
	    
	    /**
	     * Process a "start" event for this Host.
	     */
	    public void start() {
	    	
	    	try {
	    		ObjectName hostON = new ObjectName(host.getObjectName());
	    		
	    		oname = new ObjectName
                	(hostON.getDomain() + ":type=Deployer,host=" + host.getName());
	    		
	    		Registry.getRegistry(null, null).registerComponent
                		(this, oname, this.getClass().getName());
	    		
	    	}catch (Exception e) {
	    		
	    	}
	    	
	    	
	    	if (host.getDeployOnStartup())
	    		deployApps();
	    }
	    
	    
	    /**
	     * Process a "stop" event for this Host.
	     */
	    public void stop() {
	    	
	    }
	    
	    
	    
	    // ----------------------- DeployedApplication inner class --------------
	    
	    /**
	     * This class represents the state of a deployed application, as well as 
	     * the monitored resources.
	     */
	    protected class DeployedApplication {
	    	
	    	public DeployedApplication(String name) {
	    		this.name = name;
	    	}
	    	
	    	
	    	/**
	    	 * Application context path. The assertion is that 
	    	 * (host.getChild(name) != null).
	    	 */
	    	public String name;
	    	
	    	
	    	/**
	    	 * Any modification of the specified (static) resources will cause a 
	    	 * redeployment of the application. If any of the specified resources is
	    	 * removed, the application will be undeployed. Typically, this will
	    	 * contain resources like the context.xml file, a compressed WAR path.
	         * The value is the last modification time.
	    	 */
	    	public LinkedHashMap redeployResources = new LinkedHashMap();
	    	
	    	
	    	
	    	/**
	    	 * Any modification of the specified (static) resources will cause a 
	    	 * reload of the application. This will typically contain resources
	    	 * such as the web.xml of a webapp, but can be configured to contain
	    	 * additional descriptors.
	         * The value is the last modification time.
	    	 */
	    	public HashMap reloadResources = new HashMap();
	    	
	    	
	    	/**
	    	 * Instant where the application was last put in service.
	    	 */
	    	public long timestamp = System.currentTimeMillis();
	    }
}
