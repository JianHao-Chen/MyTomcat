package My.catalina.tribes.group.interceptors;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;

import My.catalina.tribes.Channel;
import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelException.FaultyMember;
import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;
import My.catalina.tribes.RemoteProcessException;
import My.catalina.tribes.group.ChannelInterceptorBase;
import My.catalina.tribes.group.InterceptorPayload;
import My.catalina.tribes.io.ChannelData;
import My.catalina.tribes.io.XByteBuffer;
import My.catalina.tribes.membership.MemberImpl;
import My.catalina.tribes.membership.Membership;

/**
 * <p>Title: A perfect failure detector </p>
 *
 * <p>Description: The TcpFailureDetector is a useful interceptor
 * that adds reliability to the membership layer.</p>
 * <p>
 * If the network is busy, or the system is busy so that the membership receiver thread
 * is not getting enough time to update its table, members can be "timed out";
 * This failure detector will intercept the memberDisappeared message(unless its a true shutdown message)
 * and connect to the member using TCP.
 * </p>
 * <p>
 * The TcpFailureDetector works in two ways. <br>
 * 1. It intercepts memberDisappeared events
 * 2. It catches send errors 
 * </p>
 */

public class TcpFailureDetector extends ChannelInterceptorBase{

	private static My.juli.logging.Log log = My.juli.logging.LogFactory.getLog( TcpFailureDetector.class );
	
	protected static byte[] TCP_FAIL_DETECT = new byte[] {
        79, -89, 115, 72, 121, -126, 67, -55, -97, 111, -119, -128, -95, 91, 7, 20,
        125, -39, 82, 91, -21, -15, 67, -102, -73, 126, -66, -113, -127, 103, 30, -74,
        55, 21, -66, -121, 69, 126, 76, -88, -65, 10, 77, 19, 83, 56, 21, 50,
        85, -10, -108, -73, 58, -6, 64, 120, -111, 4, 125, -41, 114, -124, -64, -43};
	
	protected boolean performConnectTest = true;

    protected long connectTimeout = 1000;//1 second default
    
    protected boolean performSendTest = true;

    protected boolean performReadTest = false;
    
    protected long readTestTimeout = 5000;//5 seconds
	
	protected Membership membership = null;
	
	protected HashMap removeSuspects = new HashMap();
    
    protected HashMap addSuspects = new HashMap();
    
	
	 public void memberAdded(Member member) {
		 if ( membership == null ) 
			 setupMembership();
		 
		 boolean notify = false;
	     synchronized (membership) {
	    	 if (removeSuspects.containsKey(member)) {
	         	//previously marked suspect, system below picked up the member again
	         	removeSuspects.remove(member);
	         } else if (membership.getMember( (MemberImpl) member) == null){
	        	 //if we add it here, then add it upwards too
	        	 //check to see if it is alive
	        	 if (memberAlive(member)) {
	        		 membership.memberAlive( (MemberImpl) member);
	        		 notify = true;
	        	 }
	        	 else {
	                 addSuspects.put(member, new Long(System.currentTimeMillis()));
	             }
	         }
	     }
	     if ( notify ) 
	    	 super.memberAdded(member);
	 }
	 
	 
	 public void memberDisappeared(Member member) {
		 if ( membership == null ) 
			 setupMembership();
		 
		 boolean notify = false;
		 
		 boolean shutdown = Arrays.equals(member.getCommand(),Member.SHUTDOWN_PAYLOAD);
	     if ( !shutdown ) 
	     	if(log.isInfoEnabled())
	        	log.info("Received memberDisappeared["+member+"] message. Will verify.");
	        
	     synchronized (membership) {
	    	 if (!membership.contains(member)) {
	    		 if(log.isInfoEnabled())
	                    log.info("Verification complete. Member already disappeared["+member+"]");
	                return;
	    	 }
	    	 
	    	//check to see if the member really is gone
	        //if the payload is not a shutdown message
	        if (shutdown || !memberAlive(member)) {
	            //not correct, we need to maintain the map
                membership.removeMember( (MemberImpl) member);
                removeSuspects.remove(member);
                notify = true;
	        }
	        else {
                //add the member as suspect
                removeSuspects.put(member, new Long(System.currentTimeMillis()));
            }

	     }
	     
	     if ( notify ) {
	    	 if(log.isInfoEnabled())
	                log.info("Verification complete. Member disappeared["+member+"]");
	         super.memberDisappeared(member);
	     }
	     else {
	            if(log.isInfoEnabled())
	                log.info("Verification complete. Member still alive["+member+"]");
	        }
	 }
	 
	 
	 public boolean hasMembers() {
	        if ( membership == null ) 
	        	setupMembership();
	        return membership.hasMembers();
	    }

	    public Member[] getMembers() {
	        if ( membership == null ) 
	        	setupMembership();
	        return membership.getMembers();
	    }
	 
	 
	 
	 protected synchronized void setupMembership() {
		 if ( membership == null ) {
			 membership = new Membership(
					 (MemberImpl)super.getLocalMember(true));
		 }
	 }
	 
	 public Member getLocalMember(boolean incAlive) {
		 return super.getLocalMember(incAlive);
	 }
	 
	 
	 protected boolean memberAlive(Member mbr) {
	 	return memberAlive(
	 			mbr,
	 			TCP_FAIL_DETECT,
	 			performSendTest,
	 			performReadTest,
	 			readTestTimeout,
	 			connectTimeout,
	 			getOptionFlag()
	 	);
	 }
	 
	protected static boolean memberAlive(Member mbr, byte[] msgData, 
             boolean sendTest, boolean readTest,
             long readTimeout, long conTimeout,
             int optionFlag) {
		 
		//could be a shutdown notification
		if ( Arrays.equals(mbr.getCommand(),Member.SHUTDOWN_PAYLOAD) )
			return false;
	        
		Socket socket = new Socket(); 
		
		try {
			InetAddress ia = InetAddress.getByAddress(mbr.getHost());
			InetSocketAddress addr = new InetSocketAddress(ia, mbr.getPort());
			socket.setSoTimeout((int)readTimeout);
			
			socket.connect(addr, (int) conTimeout);
			
			 if ( sendTest ) {
				 ChannelData data = new ChannelData(true);
				 data.setAddress(mbr);
				 data.setMessage(new XByteBuffer(msgData,false));
				 data.setTimestamp(System.currentTimeMillis());
				 int options = optionFlag | Channel.SEND_OPTIONS_BYTE_MESSAGE;
				 
				 if ( readTest ) 
					 options = (options | Channel.SEND_OPTIONS_USE_ACK);
				 else 
					 options = (options & (~Channel.SEND_OPTIONS_USE_ACK));
				 
				 data.setOptions(options);
				 
				 byte[] message = XByteBuffer.createDataPackage(data);
				 
				 socket.getOutputStream().write(message);
				 if ( readTest ) {
	                    int length = socket.getInputStream().read(message);
	                    return length > 0;
	             }
			 }//end if
			 return true;
		}
		catch ( SocketTimeoutException sx) {
            //do nothing, we couldn't connect
        } catch ( ConnectException cx) {
            //do nothing, we couldn't connect
        }catch (Exception x ) {
            log.error("Unable to perform failure detection check, assuming member down.",x);
        } finally {
            try {socket.close(); } catch ( Exception ignore ){}
        }
        return false;
		
	 }
	 
	 
	 
	public void sendMessage(Member[] destination, ChannelMessage msg, InterceptorPayload payload) throws ChannelException {
		try {
			super.sendMessage(destination, msg, payload);
		}
		catch ( ChannelException cx ) {
			FaultyMember[] mbrs = cx.getFaultyMembers();
			for ( int i=0; i<mbrs.length; i++ ) {
                if ( mbrs[i].getCause()!=null &&  
                     (!(mbrs[i].getCause() instanceof RemoteProcessException)) ) {//RemoteProcessException's are ok
                    this.memberDisappeared(mbrs[i].getMember());
                }//end if
            }//for
            throw cx;
		}
	}
	
	
	public void messageReceived(ChannelMessage msg) {
		boolean process = true;
		if ( okToProcess(msg.getOptions()) ) {
			//check to see if it is a testMessage, if so, process = false
			process = ( (msg.getMessage().getLength() != TCP_FAIL_DETECT.length) ||
                    (!Arrays.equals(TCP_FAIL_DETECT,msg.getMessage().getBytes()) ) );
		}
		//ignore the message, it doesnt have the flag set
        if ( process ) 
        	super.messageReceived(msg);
        else if ( log.isDebugEnabled() ) 
        	log.debug("Received a failure detector packet:"+msg);
	}
	 
	 
}
