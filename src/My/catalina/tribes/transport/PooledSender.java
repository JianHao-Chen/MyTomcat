package My.catalina.tribes.transport;

import java.io.IOException;
import java.util.List;

public abstract class PooledSender 
	extends AbstractSender implements MultiPointSender{

	private SenderQueue queue = null;
	private int poolSize = 25;
	
	public PooledSender() {
        queue = new SenderQueue(this,poolSize);
    }
	
	
	public synchronized void connect() throws IOException {
		//do nothing, happens in the socket sender itself
        queue.open();
        setConnected(true);
	}
	
	public synchronized void disconnect() {
        queue.close();
        setConnected(false);
    }
	
	// ---------------------- Inner Class ----------------------
	
	private class SenderQueue {
		private int limit = 25;

        PooledSender parent = null;

        private List notinuse = null;

        private List inuse = null;

        private boolean isOpen = true;
        
        public SenderQueue(PooledSender parent, int limit) {
            this.limit = limit;
            this.parent = parent;
            notinuse = new java.util.LinkedList();
            inuse = new java.util.LinkedList();
        }
        
        /**
         * @return Returns the limit.
         */
        public int getLimit() {
            return limit;
        }
        /**
         * @param limit The limit to set.
         */
        public void setLimit(int limit) {
            this.limit = limit;
        }
        /**
         * @return
         */
        public int getInUsePoolSize() {
            return inuse.size();
        }

        /**
         * @return
         */
        public int getInPoolSize() {
            return notinuse.size();
        }
        
        
        
        public synchronized void open() {
        	isOpen = true;
        	notify();
        }
        
        public synchronized void close() {
        	//...
        	
        }
        
	}
	
}
