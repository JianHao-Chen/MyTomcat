package My.catalina.connector;

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import My.catalina.ContainerEvent;
import My.catalina.ContainerListener;
import My.catalina.Host;
import My.catalina.core.StandardContext;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.mapper.Mapper;
import My.tomcat.util.http.mapper.MappingData;
import My.tomcat.util.modeler.Registry;

public class MapperListener 
	implements NotificationListener, ContainerListener{

	private static Log log = LogFactory.getLog(MapperListener.class);
	
	// --------------------- Instance Variables ---------------------
	
	/**
     * Associated mapper.
     */
    protected Mapper mapper = null;
	
    
    /**
     * Associated connector.
     */
    protected Connector connector = null;
    
    /**
     * MBean server.
     */
    protected MBeanServer mBeanServer = null;
    
    private String domain="*";
    
    // --------------------- Constructors ---------------------
    
    /**
     * Create mapper listener.
     */
    public MapperListener(Mapper mapper, Connector connector) {
        this.mapper = mapper;
        this.connector = connector;
    }
    
    
	// ------------------ Public Methods ------------------
    
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    
    /**
     * Initialize associated mapper.
     */
    public void init() {
    	
    	 try {
    		 mBeanServer = Registry.getRegistry(null, null).getMBeanServer();
    		
    		 registerEngine();
    		 
    		// Query hosts
             String onStr = domain + ":type=Host,*";
             ObjectName objectName = new ObjectName(onStr);
             Set set = mBeanServer.queryMBeans(objectName, null);
             Iterator iterator = set.iterator();
             while (iterator.hasNext()) {
                 ObjectInstance oi = (ObjectInstance) iterator.next();
                 registerHost(oi.getObjectName());
             }
             
          // Query contexts
             onStr = "*:j2eeType=WebModule,*";
             objectName = new ObjectName(onStr);
             set = mBeanServer.queryMBeans(objectName, null);
             iterator = set.iterator();
             while (iterator.hasNext()) {
                 ObjectInstance oi = (ObjectInstance) iterator.next();
                 registerContext(oi.getObjectName());
             }

             // Query wrappers
             onStr = "*:j2eeType=Servlet,*";
             objectName = new ObjectName(onStr);
             set = mBeanServer.queryMBeans(objectName, null);
             iterator = set.iterator();
             while (iterator.hasNext()) {
                 ObjectInstance oi = (ObjectInstance) iterator.next();
                 registerWrapper(oi.getObjectName());
             }

             onStr = "JMImplementation:type=MBeanServerDelegate";
             objectName = new ObjectName(onStr);
             mBeanServer.addNotificationListener(objectName, this, null, null);

         } catch (Exception e) {
             log.warn("Error registering contexts",e);
         }
    	
    }


	@Override
	public void containerEvent(ContainerEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void handleNotification(Notification notification, Object handback) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	// ------------------- Protected Methods -------------------
	
	private void registerEngine()
    	throws Exception{
		
		ObjectName engineName = new ObjectName
        (domain + ":type=Engine");
		
	    if ( ! mBeanServer.isRegistered(engineName)) return;
	    String defaultHost = 
	        (String) mBeanServer.getAttribute(engineName, "defaultHost");
	    ObjectName hostName = new ObjectName
	        (domain + ":type=Host," + "host=" + defaultHost);
	    if (!mBeanServer.isRegistered(hostName)) {
	
	        // Get the hosts' list
	        String onStr = domain + ":type=Host,*";
	        ObjectName objectName = new ObjectName(onStr);
	        Set set = mBeanServer.queryMBeans(objectName, null);
	        Iterator iterator = set.iterator();
	        String[] aliases;
	        boolean isRegisteredWithAlias = false;
	        
	        while (iterator.hasNext()) {
	
	            if (isRegisteredWithAlias) break;
	        
	            ObjectInstance oi = (ObjectInstance) iterator.next();
	            hostName = oi.getObjectName();
	            aliases = (String[])
	                mBeanServer.invoke(hostName, "findAliases", null, null);
	
	            for (int i=0; i < aliases.length; i++){
	                if (aliases[i].equalsIgnoreCase(defaultHost)){
	                    isRegisteredWithAlias = true;
	                    break;
	                }
	            }
	        }
	        
	        if (!isRegisteredWithAlias && log.isWarnEnabled())
	            log.warn("mapperListener.unknownDefaultHost");
	    }
	    // This should probably be called later 
	    if( defaultHost != null ) {
	        mapper.setDefaultHostName(defaultHost);
	    }
		
	}
	
	
	/**
     * Register host.
     */
    private void registerHost(ObjectName objectName)
        throws Exception {
        String name=objectName.getKeyProperty("host");
        if( name != null ) {        

            Host host =
                (Host) connector.getService().getContainer().findChild(name);

            String[] aliases = new String[0];
            mapper.addHost(name, aliases, objectName);
            host.addContainerListener(this);
            if(log.isDebugEnabled())
                log.debug("mapperListener.registerHost");
        }
    }


    /**
     * Unregister host.
     */
    private void unregisterHost(ObjectName objectName)
        throws Exception {
        String name=objectName.getKeyProperty("host");
        if( name != null ) { 
            Host host =
                (Host) connector.getService().getContainer().findChild(name);
        
            mapper.removeHost(name);
            if (host != null) {
                host.removeContainerListener(this);
            }
            if(log.isDebugEnabled())
                log.debug("mapperListener.unregisterHost");
        }
    }


    /**
     * Register context.
     */
    private void registerContext(ObjectName objectName)
        throws Exception {

        String name = objectName.getKeyProperty("name");
        
        // If the domain is the same with ours or the engine 
        // name attribute is the same... - then it's ours
        String targetDomain=objectName.getDomain();
        if( ! domain.equals( targetDomain )) {
            try {
                targetDomain = (String) mBeanServer.getAttribute
                    (objectName, "engineName");
            } catch (Exception e) {
                // Ignore
            }
            if( ! domain.equals( targetDomain )) {
                // not ours
                return;
            }
        }

        String hostName = null;
        String contextName = null;
        if (name.startsWith("//")) {
            name = name.substring(2);
        }
        int slash = name.indexOf("/");
        if (slash != -1) {
            hostName = name.substring(0, slash);
            contextName = name.substring(slash);
        } else {
            return;
        }
        // Special case for the root context
        if (contextName.equals("/")) {
            contextName = "";
        }

        if(log.isDebugEnabled())
             log.debug("mapperListener.registerContext");

        Object context = 
            mBeanServer.invoke(objectName, "findMappingObject", null, null);
            //mBeanServer.getAttribute(objectName, "mappingObject");
        javax.naming.Context resources = (javax.naming.Context)
            mBeanServer.invoke(objectName, "findStaticResources", null, null);
            //mBeanServer.getAttribute(objectName, "staticResources");
        String[] welcomeFiles = (String[])
            mBeanServer.getAttribute(objectName, "welcomeFiles");

        mapper.addContext(hostName, contextName, context, 
                          welcomeFiles, resources);

    }


    /**
     * Unregister context.
     */
    private void unregisterContext(ObjectName objectName)
        throws Exception {

        String name = objectName.getKeyProperty("name");

        // If the domain is the same with ours or the engine 
        // name attribute is the same... - then it's ours
        String targetDomain=objectName.getDomain();
        if( ! domain.equals( targetDomain )) {
            try {
                targetDomain = (String) mBeanServer.getAttribute
                    (objectName, "engineName");
            } catch (Exception e) {
                // Ignore
            }
            if( ! domain.equals( targetDomain )) {
                // not ours
                return;
            }
        }

        String hostName = null;
        String contextName = null;
        if (name.startsWith("//")) {
            name = name.substring(2);
        }
        int slash = name.indexOf("/");
        if (slash != -1) {
            hostName = name.substring(0, slash);
            contextName = name.substring(slash);
        } else {
            return;
        }
        // Special case for the root context
        if (contextName.equals("/")) {
            contextName = "";
        }

        // Don't un-map a context that is paused
        MessageBytes hostMB = MessageBytes.newInstance();
        hostMB.setString(hostName);
        MessageBytes contextMB = MessageBytes.newInstance();
        contextMB.setString(contextName);
        MappingData mappingData = new MappingData();
        mapper.map(hostMB, contextMB, mappingData);
        if (mappingData.context instanceof StandardContext &&
                ((StandardContext)mappingData.context).getPaused()) {
            return;
        } 

        if(log.isDebugEnabled())
            log.debug("mapperListener.unregisterContext");

        mapper.removeContext(hostName, contextName);

    }


    /**
     * Register wrapper.
     */
    private void registerWrapper(ObjectName objectName)
        throws Exception {
    
        // If the domain is the same with ours or the engine 
        // name attribute is the same... - then it's ours
        String targetDomain=objectName.getDomain();
        if( ! domain.equals( targetDomain )) {
            try {
                targetDomain=(String) mBeanServer.getAttribute(objectName, "engineName");
            } catch (Exception e) {
                // Ignore
            }
            if( ! domain.equals( targetDomain )) {
                // not ours
                return;
            }
            
        }

        String wrapperName = objectName.getKeyProperty("name");
        String name = objectName.getKeyProperty("WebModule");

        String hostName = null;
        String contextName = null;
        if (name.startsWith("//")) {
            name = name.substring(2);
        }
        int slash = name.indexOf("/");
        if (slash != -1) {
            hostName = name.substring(0, slash);
            contextName = name.substring(slash);
        } else {
            return;
        }
        // Special case for the root context
        if (contextName.equals("/")) {
            contextName = "";
        }
        if(log.isDebugEnabled())
            log.debug("mapperListener.registerWrapper");

        String[] mappings = (String[])
            mBeanServer.invoke(objectName, "findMappings", null, null);
        Object wrapper = 
            mBeanServer.invoke(objectName, "findMappingObject", null, null);

        for (int i = 0; i < mappings.length; i++) {
            boolean jspWildCard = (wrapperName.equals("jsp")
                                   && mappings[i].endsWith("/*"));
            mapper.addWrapper(hostName, contextName, mappings[i], wrapper,
                              jspWildCard);
        }

    }
	
    
	
}
