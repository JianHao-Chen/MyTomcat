package My.catalina.core;

import java.io.Serializable;

import My.catalina.Context;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class StandardContext 
	extends ContainerBase
	implements Context, Serializable{

	private static transient Log log = LogFactory.getLog(StandardContext.class);
	
	// --------------------- Constructors ---------------------
	
	
	
	
	// --------------------- Instance Variables ---------------------
	
	/**
     * Associated host name.
     */
    private String hostName;
    
    
    
    /**
     * The path to a file to save this Context information.
     */
    private String configFile = null;


    /**
     * The "correctly configured" flag for this Context.
     */
    private boolean configured = false;

    
    /**
     * The application available flag for this Context.
     */
    private boolean available = false;
    
    
    /**
     * The set of instantiated application lifecycle listener objects</code>.
     */
    private transient Object applicationLifecycleListenersObjects[] = 
        new Object[0];
    
    
    /**
     * The set of instantiated application event listener objects</code>.
     */
    private transient Object applicationEventListenersObjects[] = 
        new Object[0];
    
    
    
	// ------------------- Context Properties -------------------
    

    /**
     * Return the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationEventListeners() {
        return (applicationEventListenersObjects);
    }



    /**
     * Store the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationEventListeners(Object listeners[]) {
        applicationEventListenersObjects = listeners;
    }


	 /**
     * Return the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationLifecycleListeners() {
        return (applicationLifecycleListenersObjects);
    }


	/**
     * Store the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationLifecycleListeners(Object listeners[]) {
        applicationLifecycleListenersObjects = listeners;
    }


    /**
     * Return the application available flag for this Context.
     */
    public boolean getAvailable() {

        return (this.available);

    }


    /**
     * Set the application available flag for this Context.
     *
     * @param available The new application available flag
     */
    public void setAvailable(boolean available) {

        boolean oldAvailable = this.available;
        this.available = available;
    }

    /**
     * Return the path to a file to save this Context information.
     */
    public String getConfigFile() {

        return (this.configFile);

    }


    /**
     * Set the path to a file to save this Context information.
     *
     * @param configFile The path to a file to save this Context information.
     */
    public void setConfigFile(String configFile) {

        this.configFile = configFile;
    }
    
    
    
	
    
    
    
    
    
    
}