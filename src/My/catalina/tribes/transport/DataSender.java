package My.catalina.tribes.transport;

import java.io.IOException;

public interface DataSender {

	public void connect() throws IOException;
	public void disconnect();
    public boolean isConnected();
}
