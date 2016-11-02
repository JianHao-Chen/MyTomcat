package My.catalina.tribes.transport;

import My.catalina.tribes.ChannelSender;
import My.catalina.tribes.transport.nio.PooledParallelSender;

public class ReplicationTransmitter implements ChannelSender{

	private static My.juli.logging.Log log = 
		My.juli.logging.LogFactory.getLog(ReplicationTransmitter.class);

	public ReplicationTransmitter() {
    }
	
	private MultiPointSender transport = new PooledParallelSender();

	public MultiPointSender getTransport() {
        return transport;
    }

    public void setTransport(MultiPointSender transport) {
        this.transport = transport;
    }
	
	// ----------------------- public ------------------------------
	
	/**
     * start the sender and register transmitter mbean
     */
	public void start() throws java.io.IOException {
		getTransport().connect();
	}
	
	
	@Override
	public void heartbeat() {
		// TODO Auto-generated method stub
		
	}
}
