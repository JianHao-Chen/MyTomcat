package My.catalina.tribes.membership;

import java.io.IOException;
import java.util.Properties;

import My.catalina.tribes.MembershipListener;
import My.catalina.tribes.MembershipService;
import My.catalina.tribes.util.UUIDGenerator;

/**
 * A <b>membership</b> implementation using simple multicast.
 * This is the representation of a multicast membership service.
 * This class is responsible for maintaining a list of active cluster nodes in the cluster.
 * If a node fails to send out a heartbeat, the node will be dismissed.
 */

public class McastService implements MembershipService,MembershipListener{

	private static My.juli.logging.Log log =
        My.juli.logging.LogFactory.getLog( McastService.class );
	
	/**
     * The implementation specific properties
     */
    protected Properties properties = new Properties();
    
    /**
     * A handle to the actual low level implementation
     */
    protected McastServiceImpl impl;
    
    
    /**
     * The local member
     */
    protected MemberImpl localMember ;
    
    
    
    
    
    /**
     * Create a membership service.
     */
    public McastService() {
    	//default values
        properties.setProperty("mcastPort","45564");
        properties.setProperty("mcastAddress","228.0.0.4");
        properties.setProperty("memberDropTime","3000");
        properties.setProperty("mcastFrequency","500");
    }
    
    
    /**
     * Sets the local member properties for broadcasting
     */
    public void setLocalMemberProperties(String listenHost, int listenPort) {
    	properties.setProperty("tcpListenHost",listenHost);
        properties.setProperty("tcpListenPort",String.valueOf(listenPort));
        
        try {
        	if (localMember != null) {
        		
        	}
        	else {
        		localMember = new MemberImpl(listenHost, listenPort, 0);
        		
        		localMember.setUniqueId(UUIDGenerator.randomUUID(true));
        		
        		localMember.setPayload(getPayload());
        		
        		localMember.setDomain(getDomain());
        	}
        	localMember.getData(true, true);
        	
        }catch ( IOException x ) {
            throw new IllegalArgumentException(x);
        }
    }
    
    
    
    protected byte[] payload;
    
    public byte[] getPayload() {
        return payload;
    }
    
    public void setPayload(byte[] payload) {
    	this.payload = payload;
    	if ( localMember != null ) {
    		localMember.setPayload(payload);
    	}
    }
    
    
    protected byte[] domain;
    
    public byte[] getDomain() {
        return domain;
    }
    public void setDomain(byte[] domain) {
    	this.domain = domain;
        if ( localMember != null ) {
            localMember.setDomain(domain);
            localMember.getData(true,true);
            try {
                if (impl != null) 
                	;//impl.send(false);
            }catch ( Exception x ) {
                log.error("Unable to send domain update.",x);
            }
        }
    }
    
    
    
    /**
     * A membership listener delegate (should be the cluster :)
     */
    protected MembershipListener listener;
    
    
    /**
     * Add a membership listener, this version only supports one listener per service,
     * so calling this method twice will result in only the second listener being active.
     * @param listener The listener
     */
    public void setMembershipListener(MembershipListener listener) {
        this.listener = listener;
    }
    
    
    
    /**
    *
    * @param properties
    * <BR/>All are required<BR />
    * 1. mcastPort - the port to listen to<BR>
    * 2. mcastAddress - the mcast group address<BR>
    * 4. bindAddress - the bind address if any - only one that can be null<BR>
    * 5. memberDropTime - the time a member is gone before it is considered gone.<BR>
    * 6. mcastFrequency - the frequency of sending messages<BR>
    * 7. tcpListenPort - the port this member listens to<BR>
    * 8. tcpListenHost - the bind address of this member<BR>
    * @exception java.lang.IllegalArgumentException if a property is missing.
    */
   public void setProperties(Properties properties) {
       hasProperty(properties,"mcastPort");
       hasProperty(properties,"mcastAddress");
       hasProperty(properties,"memberDropTime");
       hasProperty(properties,"mcastFrequency");
       hasProperty(properties,"tcpListenPort");
       hasProperty(properties,"tcpListenHost");
       this.properties = properties;
   }

   /**
    * Return the properties, see setProperties
    */
   public Properties getProperties() {
       return properties;
   }
    
    /**
     * Check if a required property is available.
     * @param properties The set of properties
     * @param name The property to check for
     */
    protected void hasProperty(Properties properties, String name){
        if ( properties.getProperty(name)==null) 
        	throw new IllegalArgumentException("McastService:Required property \""+name+"\" is missing.");
    }
    
    
    /**
     * Start broadcasting and listening to membership pings
     * @throws java.lang.Exception if a IO error occurs
     */
    public void start() throws java.lang.Exception {
    	
    }
    
    
    public void start(int level) throws java.lang.Exception {
    	hasProperty(properties,"mcastPort");
        hasProperty(properties,"mcastAddress");
        hasProperty(properties,"memberDropTime");
        hasProperty(properties,"mcastFrequency");
        hasProperty(properties,"tcpListenPort");
        hasProperty(properties,"tcpListenHost");
        
        if ( impl != null ) {
         //   impl.start(level);
            return;
        }
        
        
        String host = getProperties().getProperty("tcpListenHost");
        int port = Integer.parseInt(
        		getProperties().getProperty("tcpListenPort"));
        
        if ( localMember == null ) {
        	//...
        }
        else{
        	localMember.setHostname(host);
        	localMember.setPort(port);
        	localMember.setMemberAliveTime(100);
        }
        
        if ( this.payload != null ) 
        	localMember.setPayload(payload);
        
        if ( this.domain != null ) 
        	localMember.setDomain(domain);
        
        localMember.setServiceStartTime(System.currentTimeMillis());
        
        java.net.InetAddress bind = null;
        
        int ttl = -1;
        int soTimeout = -1;
        
        impl = new McastServiceImpl(
        		(MemberImpl)localMember,
        		Long.parseLong(properties.getProperty("mcastFrequency")),
                Long.parseLong(properties.getProperty("memberDropTime")),
                Integer.parseInt(properties.getProperty("mcastPort")),
                bind,
                java.net.InetAddress.getByName(properties.getProperty("mcastAddress")),
                ttl,
                soTimeout,
                this);
        
    }
    
    
    
    
    
	
}
