package My.tomcat.util.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.MutableInteger;

/**
*
* Thread safe non blocking selector pool
*/

public class NioSelectorPool {

	protected static Log log = LogFactory.getLog(NioSelectorPool.class);
	
	
	// -------------   static variables ----------------------
	
	protected final static boolean SHARED = true;
	
	 
	 
	// -------------   instance variables ----------------------
	
	protected boolean enabled = true;
	
	protected NioBlockingSelector blockingSelector;
	
	protected Selector SHARED_SELECTOR;
	
	// ------------- protected methods ----------------------
	
	protected Selector getSharedSelector() throws IOException {
		
		if (SHARED && SHARED_SELECTOR == null) {
			
			synchronized ( NioSelectorPool.class ) {
				
				if ( SHARED_SELECTOR == null )  {
					 SHARED_SELECTOR = Selector.open();
	                 log.info("Using a shared selector for servlet write/read");
				}
			}
		}
		 return  SHARED_SELECTOR;
	}
	
	public void open() throws IOException {
		
		enabled = true;
		getSharedSelector();
		if (SHARED) {
			
			blockingSelector = new NioBlockingSelector();
			blockingSelector.open(getSharedSelector());
		}
	}
	
	
	public Selector get() throws IOException{
		if ( SHARED ) {
            return getSharedSelector();
        }
		
		return null;
	}
	
	
	/**
     * Performs a blocking write using the bytebuffer for data to be written and a selector to block.
     * If the <code>selector</code> parameter is null, then it will perform a busy write that could
     * take up a lot of CPU cycles.
     * @param buf ByteBuffer - the buffer containing the data, we will write as long as <code>(buf.hasRemaining()==true)</code>
     * @param socket SocketChannel - the socket to write data to
     * @param selector Selector - the selector to use for blocking, if null then a busy write will be initiated
     * @param writeTimeout long - the timeout for this write operation in milliseconds, -1 means no timeout
     * @return int - returns the number of bytes written
     * @throws EOFException if write returns -1
     * @throws SocketTimeoutException if the write times out
     * @throws IOException if an IO Exception occurs in the underlying socket logic
     */
	
	public int write(ByteBuffer buf, NioChannel socket, Selector selector, long writeTimeout) throws IOException {
        return write(buf,socket,selector,writeTimeout,true,null);
    }
	
	public int write(ByteBuffer buf, NioChannel socket, Selector selector, 
            long writeTimeout, boolean block, MutableInteger lastWrite) throws IOException {
		
		if ( SHARED && block ) {
			return blockingSelector.write(buf,socket,writeTimeout,lastWrite);
		}
		
		
		// ..
		return -1;
		
		
	}
	
}
