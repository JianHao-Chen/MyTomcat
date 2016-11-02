package My.catalina.tribes.transport;

import java.io.IOException;

public abstract class AbstractSender implements DataSender {

	private boolean connected = false;
	
	public AbstractSender() {
        
    }

	/**
     * connect
     *
     * @throws IOException
     * @todo Implement this org.apache.catalina.tribes.transport.DataSender method
     */
    public abstract void connect() throws IOException;

    /**
     * disconnect
     *
     * @todo Implement this org.apache.catalina.tribes.transport.DataSender method
     */
    public abstract void disconnect();
    
    
    public boolean isConnected() {
        return connected;
    }
    protected void setConnected(boolean connected){
        this.connected = connected;
    }

}
