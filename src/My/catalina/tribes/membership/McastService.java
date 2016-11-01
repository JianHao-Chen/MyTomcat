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
	
}
