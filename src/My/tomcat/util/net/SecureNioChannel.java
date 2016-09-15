package My.tomcat.util.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 
 * Implementation of a secure socket channel
 */

public class SecureNioChannel extends NioChannel{

	
	 public SecureNioChannel(SocketChannel channel,
			ApplicationBufferHandler bufHandler)
			throws IOException {
		super(channel, bufHandler);
		// TODO Auto-generated constructor stub
	}

	/**
     * Callback interface to be able to expand buffers
     * when buffer overflow exceptions happen
     */
	 public static interface ApplicationBufferHandler {
		 public ByteBuffer expand(ByteBuffer buffer, int remaining);
	     public ByteBuffer getReadBuffer();
	     public ByteBuffer getWriteBuffer();
	 }
}
