package My.catalina.startup;

import My.catalina.Context;
import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleListener;
import My.catalina.core.StandardContext;
import My.tomcat.util.digester.Digester;

public class ContextConfig implements LifecycleListener {

	protected static My.juli.logging.Log log=
        My.juli.logging.LogFactory.getLog( ContextConfig.class );
	
	
	// -------------------------- Static Variables ------------------------
	
	/**
     * The <code>Digester</code> we will use to process web application
     * deployment descriptor files.
     */
    protected static Digester webDigester = null;
	
	
    /**
     * Attribute value used to turn on/off XML validation
     */
     protected static boolean xmlValidation = false;


    /**
     * Attribute value used to turn on/off XML namespace awarenes.
     */
    protected static boolean xmlNamespaceAware = false;

    
    /**
     * The <code>Rule</code> used to parse the web.xml
     */
    protected static WebRuleSet webRuleSet = new WebRuleSet();
    
    
	
	// ---------------------- Instance Variables ----------------------
	
	
	/**
     * The Context we are associated with.
     */
    protected Context context = null;
	
	
	// --------------------------------------------------------- Public Methods


    /**
     * Process events for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {
    	
    	// Identify the context we are associated with
    	try {
    		context = (Context) event.getLifecycle();
    	}catch (ClassCastException e) {
    		return;
    	}
    	
    	
    	 // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) {
     
        }else if (event.getType().equals(StandardContext.BEFORE_START_EVENT)) {
       
        }
        else if (event.getType().equals(StandardContext.AFTER_START_EVENT)) {
        	
        }else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
        	
        }else if (event.getType().equals(Lifecycle.INIT_EVENT)) {
        	init();
        }else if (event.getType().equals(Lifecycle.DESTROY_EVENT)) {
        	
        }
    	
    }
    
    
    
    
    /**
     * Process a "init" event for this Context.
     */
    protected void init() {
    	
    	// use to parse web.xml, can't use right now.
    	/*if (webDigester == null){
    		webDigester = createWebDigester();
    	}*/
    }
    
    
    //------------------------- protected method ---------------------
    
    
    /**
     * Create (if necessary) and return a Digester configured to process the
     * web application deployment descriptor (web.xml).
     */
    protected static Digester createWebDigester() {
        Digester webDigester =
            createWebXmlDigester(xmlNamespaceAware, xmlValidation);
        return webDigester;
    }
    
    
    
    /**
     * Create (if necessary) and return a Digester configured to process the
     * web application deployment descriptor (web.xml).
     */
    public static Digester createWebXmlDigester(boolean namespaceAware,
                                                boolean validation) {
        
        Digester webDigester =  DigesterFactory.newDigester(xmlValidation,
                                                            xmlNamespaceAware,
                                                            webRuleSet);
        return webDigester;
    }
    
    
    
    
    
    
}
