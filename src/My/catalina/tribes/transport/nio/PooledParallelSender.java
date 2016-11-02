package My.catalina.tribes.transport.nio;

import java.io.IOException;

import My.catalina.tribes.transport.MultiPointSender;
import My.catalina.tribes.transport.PooledSender;

public class PooledParallelSender 
	extends PooledSender implements MultiPointSender{

	protected boolean connected = true;
	
	
	public PooledParallelSender() {
        super();
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
