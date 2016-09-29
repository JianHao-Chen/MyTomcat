package My.tomcat.util.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.MutableInteger;
import My.tomcat.util.net.NioEndpoint.KeyAttachment;

public class NioBlockingSelector {

	protected static Log log = LogFactory.getLog(NioBlockingSelector.class);
	
	private static int threadCounter = 0;
	
	protected Selector sharedSelector;

	
	
	 public void open(Selector selector) {
		 
	 }
	
	
	
	 /**
     * Performs a blocking write using the bytebuffer for data to be written
     * If the <code>selector</code> parameter is null, then it will perform a busy write that could
     * take up a lot of CPU cycles.
     * @param buf ByteBuffer - the buffer containing the data, we will write as long as <code>(buf.hasRemaining()==true)</code>
     * @param socket SocketChannel - the socket to write data to
     * @param writeTimeout long - the timeout for this write operation in milliseconds, -1 means no timeout
     * @return int - returns the number of bytes written
     * @throws EOFException if write returns -1
     * @throws SocketTimeoutException if the write times out
     * @throws IOException if an IO Exception occurs in the underlying socket logic
     */
	public int write(ByteBuffer buf, NioChannel socket, long writeTimeout,MutableInteger lastWrite) throws IOException {
		
		SelectionKey key = socket.getIOChannel().keyFor(
							socket.getPoller().getSelector());
		
		if ( key == null ) 
			throw new IOException("Key no longer registered");
		
		KeyReference reference = new KeyReference();
		
		KeyAttachment att = (KeyAttachment) key.attachment();
		
		int written = 0;
		boolean timedout = false;
		
		int keycount = 1; //assume we can write
		long time = System.currentTimeMillis(); //start the timeout timer
		
		try {
			 while ( (!timedout) && buf.hasRemaining()) {
				 if (keycount > 0) { //only write if we were registered for a write
					 int cnt = socket.write(buf); //write the data
					 lastWrite.set(cnt);
					 
					 if (cnt == -1)
	                        throw new EOFException();
					 
					 written += cnt;
					 
					 if (cnt > 0) {
	                        time = System.currentTimeMillis(); //reset our timeout timer
	                        continue; //we successfully wrote, try again without a selector
	                    }
				 }

				 
			 } // while
			 
			 if (timedout) 
				 throw new SocketTimeoutException();
			 
			 
		}finally {
			// ...
		}
		return written;
	}
	
	
	
	
	public class KeyReference {
        SelectionKey key = null;
        
        @Override
        public void finalize() {
            if (key!=null && key.isValid()) {
                log.warn("Possible key leak, cancelling key in the finalizer.");
                try {
                	key.cancel();
                }
                catch (Exception ignore){}
            }
            key = null;
        }
    }
}
