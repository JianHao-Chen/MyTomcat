package My.catalina.tribes.transport;

import My.catalina.tribes.ChannelSender;

public class ReplicationTransmitter implements ChannelSender{

	private static My.juli.logging.Log log = 
		My.juli.logging.LogFactory.getLog(ReplicationTransmitter.class);

	public ReplicationTransmitter() {
    }
	
	private MultiPointSender transport = new PooledParallelSender();

	@Override
	public void heartbeat() {
		// TODO Auto-generated method stub
		
	}
}
