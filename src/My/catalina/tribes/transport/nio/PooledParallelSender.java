package My.catalina.tribes.transport.nio;

import java.io.IOException;

import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;
import My.catalina.tribes.transport.DataSender;
import My.catalina.tribes.transport.MultiPointSender;
import My.catalina.tribes.transport.PooledSender;

public class PooledParallelSender 
	extends PooledSender implements MultiPointSender{

	protected boolean connected = true;
	
	
	public PooledParallelSender() {
        super();
    }
	
	
	public void sendMessage(Member[] destination, ChannelMessage message) throws ChannelException {
		if ( !connected ) 
			throw new ChannelException("Sender not connected.");
		
		ParallelNioSender sender = (ParallelNioSender)getSender();
		
		if (sender == null) {
			ChannelException cx = new ChannelException("Unable to retrieve a data sender, time out error.");
			
			throw cx;
		}
		else {
			try {
				sender.sendMessage(destination, message);
				/*sender.keepalive();*/
			}
			catch (ChannelException x) {
				sender.disconnect();
				throw x;
			}
			finally {
				returnSender(sender);
				if (!connected) 
					disconnect();
			}
		}
		
	}
	
	public DataSender getNewDataSender() {
		try {
			ParallelNioSender sender = new ParallelNioSender();
			sender.transferProperties(this,sender);
			return sender;
		}
		catch ( IOException x ) {
            throw new RuntimeException("Unable to open NIO selector.",x);
        }
	}
	
	
	
	public synchronized void connect() throws IOException {
		this.connected = true;
        super.connect();
	}
	
	
	public synchronized void disconnect() {
		this.connected = false;
        super.disconnect();
	}
}
