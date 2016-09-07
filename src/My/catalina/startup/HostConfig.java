package My.catalina.startup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import My.catalina.Container;
import My.catalina.Engine;
import My.catalina.Host;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleListener;
import My.tomcat.util.digester.Digester;

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
	    public void lifecycleEvent(LifecycleEvent event) {
	    	
	    	/*
	    	 *  do this latter.
	    	 */
	    	
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
}
