package My.catalina.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Host;
import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleListener;
import My.catalina.core.StandardContext;
import My.catalina.core.StandardEngine;
import My.tomcat.util.digester.Digester;
import My.tomcat.util.digester.RuleSet;

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
     * The <code>Digester</code> we will use to process web application
     * context files.
     */
    protected static Digester contextDigester = null;
    
    
	
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
    
    
    /**
     * Track any fatal errors during startup configuration processing.
     */
    protected boolean ok = false;

    
    /**
     * Any parse error which occurred while parsing XML descriptors.
     */
    protected SAXParseException parseException = null;
    
    
    /**
     * The default web application's context file location.
     */
    protected String defaultContextXml = null;
	
    
    /**
     * The default web application's deployment descriptor location.
     */
    protected String defaultWebXml = null;
    
    
	
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
        	start();
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
     * Process the default configuration file, if it exists.
     */
    protected void contextConfig() {
    	
    	// Open the default context.xml file, if it exists
    	if( defaultContextXml==null && context instanceof StandardContext ) {
            defaultContextXml = ((StandardContext)context).getDefaultContextXml();
        }
    	
    	// set the default if we don't have any overrides
    	if( defaultContextXml==null ) 
    		getDefaultContextXml();
    	
    	 if (!context.getOverride()) {
    		 File defaultContextFile = new File(defaultContextXml);
             if (!defaultContextFile.isAbsolute()) {
                 defaultContextFile =new File(getBaseDir(), defaultContextXml);
             }
    		 
             processContextConfig(defaultContextFile.getParentFile(), defaultContextFile.getName());
    	 }
    }
    
    
    
    
    /**
     * Process a "init" event for this Context.
     */
    protected void init() {
    	
    	
    	if (webDigester == null){
    		webDigester = createWebDigester();
    		webDigester.getParser();
    	}
    	
    	if (contextDigester == null){
    		contextDigester = createContextDigester();
    		contextDigester.getParser();
    	}
    	
    	context.setConfigured(false);
    	
    	ok = true;
    	 
    	contextConfig();
    }
    
    
    
    /**
     * Process a "start" event for this Context.
     */
    protected synchronized void start() {
    	
    	// Set properties based on DefaultContext
    	Container container = context.getParent();
    	if( !context.getOverride() ) {
    		
    		if( container instanceof Host ) {
    			
    		}
    	}
    	
    	
    	if("/examples".equals(this.context.getName())){
    		System.out.println("/examples");
    	}
    	
    	// Process the default and application web.xml files
        defaultWebConfig();
        applicationWebConfig();
        
        applicationAnnotationsConfig();
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
     * context configuration descriptor for an application.
     */
    protected Digester createContextDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
        RuleSet contextRuleSet = new ContextRuleSet("", false);
        digester.addRuleSet(contextRuleSet);
        RuleSet namingRuleSet = new NamingRuleSet("Context/");
        digester.addRuleSet(namingRuleSet);
        return digester;
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
    
    
    
    /**
     * Return the location of the default deployment descriptor
     */
    public String getDefaultWebXml() {
        if( defaultWebXml == null ) {
            defaultWebXml=Constants.DefaultWebXml;
        }

        return (this.defaultWebXml);

    }


    /**
     * Set the location of the default deployment descriptor
     *
     * @param path Absolute/relative path to the default web.xml
     */
    public void setDefaultWebXml(String path) {

        this.defaultWebXml = path;

    }
    
    
    /**
     * Return the location of the default context file
     */
    public String getDefaultContextXml() {
        if( defaultContextXml == null ) {
            defaultContextXml=Constants.DefaultContextXml;
        }

        return (this.defaultContextXml);

    }


    /**
     * Set the location of the default context file
     *
     * @param path Absolute/relative path to the default context.xml
     */
    public void setDefaultContextXml(String path) {

        this.defaultContextXml = path;

    }
    
    
    
    /**
     * Process the application classes annotations, if it exists.
     */
    protected void applicationAnnotationsConfig() {
    	
    	WebAnnotationSet.loadApplicationAnnotations(context);
    	
    }
    
    
    /**
     * Process a context.xml.
     */
    protected void processContextConfig(File baseDir, String resourceName) {
    	InputSource source = null;
        InputStream stream = null;

        File file = baseDir;
        if (resourceName != null) {
        	file = new File(baseDir, resourceName);
        }

        try {
        	if ( !file.exists() ) {
        		// just assume the context.xml is in path : 
        		// conf/
        	}else{
        		source =
                    new InputSource("file://" + file.getAbsolutePath());
        		stream = new FileInputStream(file);
        		
        		// Add as watched resource so that cascade reload occurs if a default
                // config file is modified/added/removed
                context.addWatchedResource(file.getAbsolutePath());
        		
        	}
        }catch (Exception e) {
        	log.error("contextConfig.contextMissing");
        }
        
        if (source == null)
            return;
        
        
        synchronized (contextDigester) {
        	
        	try {
        		source.setByteStream(stream);
        		contextDigester.setClassLoader(this.getClass().getClassLoader());
                contextDigester.setUseContextClassLoader(false);
                contextDigester.push(context.getParent());
                contextDigester.push(context);
                contextDigester.setErrorHandler(new ContextErrorHandler());
                contextDigester.parse(source);
                
                if (parseException != null) {
                    ok = false;
                }
                
                
                
        	}catch (SAXParseException e) {
        		
        	}catch (Exception e) {
        		
        	}finally {
        		contextDigester.reset();
                parseException = null;
                
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error("contextConfig.contextClose");
                }
        	}
        	
        	
        }
        
    }
    
    
    
    /**
     * Process a default web.xml.
     */
    protected void processDefaultWebConfig(Digester digester, InputStream stream, 
            InputSource source) {
    	
    	// Process the default web.xml file
        synchronized (digester) {
        	
        	try {
        		source.setByteStream(stream);

                digester.setClassLoader(this.getClass().getClassLoader());
                digester.setUseContextClassLoader(false);
                digester.push(context);
                digester.setErrorHandler(new ContextErrorHandler());
                digester.parse(source);
                if (parseException != null) {
                    ok = false;
                }
        	}catch (SAXParseException e) {
        		ok = false;
        	}catch (Exception e) {
        		ok = false;
        		
        	}finally {
        		digester.reset();
                parseException = null;
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error("contextConfig.defaultClose");
                }
        	}
        	
        }
    }
    
    
    /**
     * Process the application configuration file, if it exists.
     */
    protected void applicationWebConfig() {
    	
    	String altDDName = null;
    	// Open the application web.xml file, if it exists
        InputStream stream = null;
        
        ServletContext servletContext = context.getServletContext();
        
        if (servletContext != null) {
        	
        	stream = servletContext.getResourceAsStream
            				(Constants.ApplicationWebXml);
        }
        
        if (stream == null) {
        	// log ...
        	return;
        }
        
        URL url=null;
        
        // Process the application web.xml file
        
        synchronized (webDigester) {
        	
        	try {
        		url = servletContext.getResource(
                        Constants.ApplicationWebXml);
        		
        		if( url!=null ) {
        			InputSource is = new InputSource(url.toExternalForm());
        			is.setByteStream(stream);
        			
        			webDigester.push(context);
        			webDigester.setErrorHandler(new ContextErrorHandler());
        			
        			webDigester.parse(is);
        			
        			if (parseException != null) {
                        ok = false;
                    }
        		}
        		else {
                    log.info("No web.xml, using defaults " + context );
                }
        		
        	}
        	catch (SAXParseException e) {
        		// log ...
        		ok = false;
        	}
        	catch (Exception e) {
        		ok = false;
        	}
        	finally {
        		webDigester.reset();
                parseException = null;
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error("contextConfig.applicationClose");
                }
        	}
        }
        
        
    }
    
    
    
    /**
     * Process the default configuration file, if it exists.
     * The default config must be read with the container loader - so
     * container servlets can be loaded
     */
    protected void defaultWebConfig() {
    	
    	// set the default if we don't have any overrides
        if( defaultWebXml==null ) 
        	getDefaultWebXml();
        
        File file = new File(this.defaultWebXml);
        if (!file.isAbsolute()) {
            file = new File(getBaseDir(),
                            this.defaultWebXml);
        }

        InputStream stream = null;
        InputSource source = null;

        
        try {
        	if ( ! file.exists() ) {
        		// assume the file is exists
        	}else{
        		source =
                    new InputSource("file://" + file.getAbsolutePath());
                stream = new FileInputStream(file);
                context.addWatchedResource(file.getAbsolutePath());
        	}	
        }catch (Exception e) {
        	
        }
        
        
        
        if (stream != null) {
        	processDefaultWebConfig(webDigester, stream, source);
            webRuleSet.recycle();
        }
        
    }
    
    
    protected String getBaseDir() {
        Container engineC=context.getParent().getParent();
        if( engineC instanceof StandardEngine ) {
            return ((StandardEngine)engineC).getBaseDir();
        }
        return System.getProperty("catalina.base");
    }
    
    
    
    
    
    
    protected class ContextErrorHandler
    implements ErrorHandler {

    public void error(SAXParseException exception) {
        parseException = exception;
    }

    public void fatalError(SAXParseException exception) {
        parseException = exception;
    }

    public void warning(SAXParseException exception) {
        parseException = exception;
    }

}

    
    
    
}
