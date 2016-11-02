package My.catalina.tribes.membership;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import My.catalina.tribes.Channel;
import My.catalina.tribes.Member;
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
		 setupSocket();
		 sendPacket = new DatagramPacket(new byte[MAX_PACKET_SIZE],MAX_PACKET_SIZE);
		 sendPacket.setAddress(address);
		 sendPacket.setPort(port);
		 
		 receivePacket = new DatagramPacket(new byte[MAX_PACKET_SIZE],MAX_PACKET_SIZE);
	     receivePacket.setAddress(address);
	     receivePacket.setPort(port);
	     
	     member.setCommand(new byte[0]);
	     member.getData(true, true);
	     if ( membership == null ) 
	    	 membership = new Membership(member);
	 }
	 
	 protected void setupSocket() throws IOException {
		 
		 if (mcastBindAddress != null) {
			 //...
		 }
		 else {
	     	socket = new MulticastSocket(port);
	     }
		 socket.setLoopbackMode(false);
		 
		 if (mcastBindAddress != null) {
			 //...
		 }
		 
		//force a so timeout so that we don't block forever
	    if ( mcastSoTimeout <= 0 ) 
	    	mcastSoTimeout = (int)sendFrequency;
	    
	    if(log.isInfoEnabled())
            log.info("Setting cluster mcast soTimeout to "+mcastSoTimeout);
	    
	    socket.setSoTimeout(mcastSoTimeout);
	 }
	 
	 
	 
	 /**
	 * Start the service
	 * @param level 1 starts the receiver, level 2 starts the sender
	 * @throws IOException if the service fails to start
	 * @throws IllegalStateException if the service is already started
	 */
	 public synchronized void start(int level) throws IOException {
		 boolean valid = false;
		 
		 if ( (level & Channel.MBR_RX_SEQ)==Channel.MBR_RX_SEQ ) {
			 if ( receiver != null ) 
				 throw new IllegalStateException("McastService.receive already running.");
			 
			 if ( sender == null ) 
				 socket.joinGroup(address);
			 
			 doRunReceiver = true;
			 
			 receiver = new ReceiverThread();
			 receiver.setDaemon(true);
	         receiver.start();
	         valid = true;
		 }
		 if ( (level & Channel.MBR_TX_SEQ)==Channel.MBR_TX_SEQ ) {
			 if ( sender != null ) 
				 throw new IllegalStateException("McastService.send already running.");
			 if ( receiver == null ) 
				 socket.joinGroup(address);
			 //make sure at least one packet gets out there
	         send(false);
			 
			 
		 }
		 if (!valid) {
			 throw new IllegalArgumentException("Invalid start level. Only acceptable levels are Channel.MBR_RX_SEQ and Channel.MBR_TX_SEQ");
	     }
		 
		 //pause, once or twice
		 waitForMembers(level);
		 startLevel = (startLevel | level);
	 }
	 
	 private void waitForMembers(int level) {
		 long memberwait = sendFrequency*2;
		 if(log.isInfoEnabled())
	     	log.info("Sleeping for "+memberwait+" milliseconds to establish cluster membership, start level:"+level);
		 
		 try {
			 Thread.sleep(memberwait);
		 }
		 catch (InterruptedException ignore){}
		 
		 if(log.isInfoEnabled())
	     	log.info("Done sleeping, membership established, start level:"+level);
	 }
	 
	 
	 
	 /**
	  * Receive a datagram packet, locking wait
	  * @throws IOException
	  */
	public void receive() throws IOException {
		try {
			socket.receive(receivePacket);
			if(receivePacket.getLength() > MAX_PACKET_SIZE) {
				log.error("Multicast packet received was too long, dropping package:"+receivePacket.getLength());
			}
			else {
				byte[] data = new byte[receivePacket.getLength()];
				System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, data.length);
				
				final MemberImpl m = MemberImpl.getMember(data);
				
				Thread t = null;
				if (Arrays.equals(m.getCommand(), Member.SHUTDOWN_PAYLOAD)) {
					//...
				}
				else if (membership.memberAlive(m)) {
					
				}
				
				if ( t != null ) 
					t.start();
				
			}
		}
		catch (SocketTimeoutException x ) { 
			
			//do nothing, this is normal, we don't want to block forever
            //since the receive thread is the same thread
            //that does membership expiration
		}
		checkExpired();
	}
	
	protected Object expiredMutex = new Object();
    protected void checkExpired() {
    	synchronized (expiredMutex) {
    		MemberImpl[] expired = membership.expire(timeToExpiration);
    		
    	}
    }
    
    
    
    /**
     * Send a ping
     * @throws Exception
     */ 
    public void send(boolean checkexpired) throws IOException{
    	//ignore if we haven't started the sender
    	member.inc();
    	
    	byte[] data = member.getData();
    	DatagramPacket p = new DatagramPacket(data,data.length);
    	
    	p.setAddress(address);
    	p.setPort(port);
    	socket.send(p);
    	if ( checkexpired ) 
    		checkExpired();
    }
	 

	 
	public class ReceiverThread extends Thread {
		int errorCounter = 0;
		public ReceiverThread() {
			super();
			setName("Tribes-MembershipReceiver");
		}
		
		public void run() {
			while ( doRunReceiver ) {
				try {
					receive();
                    errorCounter=0;
				}
				catch ( Exception x ) {
					
				}
			}
		}
	 }
	 
	 public class SenderThread extends Thread {
		 
	 }
	 
	 
}
