package My.catalina.tribes.group;

import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelInterceptor;

public abstract class ChannelInterceptorBase 
	implements ChannelInterceptor{

	
	protected static My.juli.logging.Log log = My.juli.logging.LogFactory.getLog(
	        ChannelInterceptorBase.class);
	
	private ChannelInterceptor next;
    private ChannelInterceptor previous;
    
    //default value, always process
    protected int optionFlag = 0;
    
    public ChannelInterceptorBase() {
    }
    
    
    public void setOptionFlag(int optionFlag) {
        this.optionFlag = optionFlag;
    }
    
    public int getOptionFlag() {
        return optionFlag;
    }
    
    
    public final void setNext(ChannelInterceptor next) {
        this.next = next;
    }

    public final ChannelInterceptor getNext() {
        return next;
    }

    public final void setPrevious(ChannelInterceptor previous) {
        this.previous = previous;
    }
    
    public final ChannelInterceptor getPrevious() {
        return previous;
    }
    
    
    public void heartbeat() {
        if (getNext() != null) 
        	getNext().heartbeat();
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
        if ( getNext()!=null ) getNext().start(svc);
    }
    
    
    /**
     * Shuts down the channel. This can be called multiple times for individual services to shutdown
     * The svc parameter can be the logical or value of any constants
     * @param svc int value of <BR>
     * DEFAULT - will shutdown all services <BR>
     * MBR_RX_SEQ - stops the membership receiver <BR>
     * MBR_TX_SEQ - stops the membership broadcaster <BR>
     * SND_TX_SEQ - stops the replication transmitter<BR>
     * SND_RX_SEQ - stops the replication receiver<BR>
     * @throws ChannelException if a startup error occurs or the service is already started.
     */
    public void stop(int svc) throws ChannelException {
        if (getNext() != null) getNext().stop(svc);
    }
}
