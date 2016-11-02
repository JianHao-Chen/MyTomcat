package My.catalina.tribes.membership;

import java.io.IOException;
import java.net.InetAddress;

import My.catalina.tribes.MembershipListener;

public class McastServiceImpl {

	private static My.juli.logging.Log log =
        My.juli.logging.LogFactory.getLog( McastService.class );
	
	protected static int MAX_PACKET_SIZE = 65535;
	
	/**
     * Internal flag used for the listen thread that listens to the multicasting socket.
     */
    protected boolean doRunSender = false;
    protected boolean doRunReceiver = false;
	
    protected int startLevel = 0;
    /**
     * Socket that we intend to listen to
     */
    protected MulticastSocket socket;
	
	/**
     * The local member that we intend to broad cast over and over again
     */
    protected MemberImpl member;
	
    
    /**
     * The multicast address
     */
    protected InetAddress address;
    /**
     * The multicast port
     */
    protected int port;
    
    /**
     * The time it takes for a member to expire.
     */
    protected long timeToExpiration;
    /**
     * How often to we send out a broadcast saying we are alive, must be smaller than timeToExpiration
     */
    protected long sendFrequency;
    /**
     * Reuse the sendPacket, no need to create a new one everytime
     */
    protected DatagramPacket sendPacket;
    /**
     * Reuse the receivePacket, no need to create a new one everytime
     */
    protected DatagramPacket receivePacket;
    /**
     * The membership, used so that we calculate memberships when they arrive or don't arrive
     */
    protected Membership membership;
    
    /**
     * The actual listener, for callback when shits goes down
     */
    protected MembershipListener service;
    /**
     * Thread to listen for pings
     */
    protected ReceiverThread receiver;
    /**
     * Thread to send pings
     */
    protected SenderThread sender;

    /**
     * When was the service started
     */
    protected long serviceStartTime = System.currentTimeMillis();
    
    
    /**
     * Time to live for the multicast packets that are being sent out
     */
    protected int mcastTTL = -1;
    /**
     * Read timeout on the mcast socket
     */
    protected int mcastSoTimeout = -1;
    
    /**
     * bind address
     */
    protected InetAddress mcastBindAddress = null;
	
	
	/**
     * Create a new mcast service impl
     * @param member - the local member
     * @param sendFrequency - the time (ms) in between pings sent out
     * @param expireTime - the time (ms) for a member to expire
     * @param port - the mcast port
     * @param bind - the bind address (not sure this is used yet)
     * @param mcastAddress - the mcast address
     * @param service - the callback service
     * @throws IOException
     */
	public McastServiceImpl(
	        MemberImpl member,
	        long sendFrequency,
	        long expireTime,
	        int port,
	        InetAddress bind,
	        InetAddress mcastAddress,
	        int ttl,
	        int soTimeout,
	        MembershipListener service)
	    throws IOException {
		
		 this.member = member;
	     this.address = mcastAddress;
	     this.port = port;
	     this.mcastSoTimeout = soTimeout;
	     this.mcastTTL = ttl;
	     this.mcastBindAddress = bind;
	     this.timeToExpiration = expireTime;
	     this.service = service;
	     this.sendFrequency = sendFrequency;
		
	     init();
	}
	
	 public void init() throws IOException {
		 
	 }
}
