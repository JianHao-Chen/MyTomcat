package My.catalina.tribes.group;

import My.catalina.tribes.Channel;
import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelReceiver;
import My.catalina.tribes.ChannelSender;
import My.catalina.tribes.MembershipService;
import My.catalina.tribes.MessageListener;
import My.catalina.tribes.membership.McastService;
import My.catalina.tribes.transport.ReplicationTransmitter;
import My.catalina.tribes.transport.nio.NioReceiver;

public class ChannelCoordinator 
	extends ChannelInterceptorBase implements MessageListener{

	private ChannelReceiver clusterReceiver = new NioReceiver();
	private ChannelSender clusterSender = new ReplicationTransmitter();
	private MembershipService membershipService = new McastService();
	
	//override optionflag
    protected int optionFlag = Channel.SEND_OPTIONS_BYTE_MESSAGE|Channel.SEND_OPTIONS_USE_ACK|Channel.SEND_OPTIONS_SYNCHRONIZED_ACK;
    public int getOptionFlag() {return optionFlag;}
    public void setOptionFlag(int flag) {optionFlag=flag;}
    
	private int startLevel = 0;
	
	
	@Override
	public void heartbeat() {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
     * Starts up the channel. This can be called multiple times for individual services to start
     * The svc parameter can be the logical or value of any constants
     * @param svc int value of <BR>
     * DEFAULT - will start all services <BR>
     * MBR_RX_SEQ - starts the membership receiver <BR>
     * MBR_TX_SEQ - starts the membership broadcaster <BR>
     * SND_TX_SEQ - starts the replication transmitter<BR>
     * SND_RX_SEQ - starts the replication receiver<BR>
     * @throws ChannelException if a startup error occurs or the service is already started.
     */
	public void start(int svc) throws ChannelException {
		this.internalStart(svc);
	}
	
	
	
	 /**
     * Starts up the channel. This can be called multiple times for individual services to start
     * The svc parameter can be the logical or value of any constants
     * @param svc int value of <BR>
     * DEFAULT - will start all services <BR>
     * MBR_RX_SEQ - starts the membership receiver <BR>
     * MBR_TX_SEQ - starts the membership broadcaster <BR>
     * SND_TX_SEQ - starts the replication transmitter<BR>
     * SND_RX_SEQ - starts the replication receiver<BR>
     * @throws ChannelException if a startup error occurs or the service is already started.
     */
    protected synchronized void internalStart(int svc) throws ChannelException {
    	
    	try {
    		boolean valid = false;
    		
    		//make sure we don't pass down any flags that are unrelated to the bottom layer
            svc = svc & Channel.DEFAULT;
            
            if (startLevel == Channel.DEFAULT) //we have already started up all components
            	return; 
            if (svc == 0 ) return;//nothing to start
            
            if (svc == (svc & startLevel)) 
            	throw new ChannelException("Channel already started for level:"+svc);
            
            
            /*
              	must start the receiver first so that we can coordinate the port it
            	listens to with the local membership settings
            */
            //    SND_RX_SEQ - starts or stops the data receiver
            if ( Channel.SND_RX_SEQ==(svc & Channel.SND_RX_SEQ) ) {
            	clusterReceiver.setMessageListener(this);
            	clusterReceiver.start();
            	
            	membershipService.setLocalMemberProperties(
            			getClusterReceiver().getHost(), 
            			getClusterReceiver().getPort());
            	
            	valid = true;
            }
            /*	SND_TX_SEQ - starts or stops the data sender */
            if ( Channel.SND_TX_SEQ==(svc & Channel.SND_TX_SEQ) ) {
            	clusterSender.start();
                valid = true;
            }
            
            /*  MBR_RX_SEQ - starts or stops the membership listener */
            if ( Channel.MBR_RX_SEQ==(svc & Channel.MBR_RX_SEQ) ) {
            	membershipService.setMembershipListener(this);
            	membershipService.start(MembershipService.MBR_RX);
            	valid = true;
            }
            /*  MBR_TX_SEQ - starts or stops the membership broadcaster */
            if ( Channel.MBR_TX_SEQ==(svc & Channel.MBR_TX_SEQ) ) {
            	 membershipService.start(MembershipService.MBR_TX);
                 valid = true;
            }
            
    	}
    	catch ( ChannelException cx ) {
            throw cx;
        }catch ( Exception x ) {
            throw new ChannelException(x);
        }
    }
    
    
    
    public ChannelReceiver getClusterReceiver() {
        return clusterReceiver;
    }
    
    public void setClusterReceiver(ChannelReceiver clusterReceiver) {
    	
    }

}
