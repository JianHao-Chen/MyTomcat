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
	
	
	public abstract DataSender getNewDataSender();
	
	
	public DataSender getSender() {
        return queue.getSender(getTimeout());
    }
    
    public void returnSender(DataSender sender) {
        sender.keepalive();
        queue.returnSender(sender);
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
	
	
	public boolean keepalive() {
        //do nothing, the pool checks on every return
        return (queue==null)?false:queue.checkIdleKeepAlive();
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
        
        
        public synchronized DataSender getSender(long timeout) {
        	long start = System.currentTimeMillis();
        	while ( true ) {
        		if (!isOpen)
        			throw new IllegalStateException("Queue is closed");
        		DataSender sender = null;
        		if (notinuse.size() == 0 && inuse.size() < limit) {
        			sender = parent.getNewDataSender();
        		}
        		else if (notinuse.size() > 0) {
        			sender = (DataSender) notinuse.remove(0);
        		}
        		
        		if (sender != null) {
        			inuse.add(sender);
                    return sender;
        		}
        		
        		//...
        	}
        }
        
        
        public synchronized void returnSender(DataSender sender) {
        	if ( !isOpen) {
                sender.disconnect();
                return;
            }
        	
        	inuse.remove(sender);
        	if ( notinuse.size() < this.getLimit() ) 
        		notinuse.add(sender);
        	else 
        		try {sender.disconnect(); } catch ( Exception ignore){}
        		
        	notify();
        }
        
        
        public synchronized boolean checkIdleKeepAlive() {
        	DataSender[] list = new DataSender[notinuse.size()];
            notinuse.toArray(list);
            boolean result = false;
            for (int i=0; i<list.length; i++) {
                result = result | list[i].keepalive();
            }
            return result;
        	
        	
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
