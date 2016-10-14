package My.catalina.session;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Random;

import My.catalina.Container;
import My.catalina.Manager;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

/**
 * Minimal implementation of the <b>Manager</b> interface that supports
 * no session persistence or distributable capabilities.  This class may
 * be subclassed to create more sophisticated Manager implementations.
 */

public class ManagerBase implements Manager{

	protected Log log = LogFactory.getLog(ManagerBase.class);
	
	// ------------------- Instance Variables -------------------
	
	protected DataInputStream randomIS = null;
    protected String devRandomSource = "/dev/urandom";
	
	/**
     * The Container with which this Manager is associated.
     */
    protected Container container;
    
    
    
    protected boolean initialized=false;
    
    
    
    /**
     * A random number generator to use when generating session identifiers.
     */
    protected Random random = null;
    
    /**
     * The Java class name of the random number generator class to be used
     * when generating session identifiers.
     */
    protected String randomClass = "java.security.SecureRandom";
    
    /**
     * A String initialization parameter used to increase the entropy of
     * the initialization of our random number generator.
     */
    protected String entropy = null;
    
    
	// ----------------------- Properties -----------------------
	
	/**
     * Return the Container with which this Manager is associated.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the Container with which this Manager is associated.
     *
     * @param container The newly associated Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
    }
    
    
    
    
    
    
    
    
	// ------------------ Public Methods ------------------
    
    public void init() {
    	if( initialized ) 
    		return;
    	initialized=true;
    	
    	
    	
    	
    	
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	// -----------------Protected Methods -------------------------
    
   
    
}
