package My.tomcat.util.net;

import java.io.IOException;
import java.nio.channels.Selector;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;

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
			// NioBlockingSelector implements latter.
		//	blockingSelector = new NioBlockingSelector();
			
		}
	}
	
}
