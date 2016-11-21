package My.catalina.tribes.transport.nio;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import My.catalina.tribes.Channel;
import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;
import My.catalina.tribes.io.ChannelData;
import My.catalina.tribes.io.XByteBuffer;
import My.catalina.tribes.transport.AbstractSender;
import My.catalina.tribes.transport.MultiPointSender;
import My.catalina.tribes.transport.SenderState;

public class ParallelNioSender extends AbstractSender implements MultiPointSender{

	protected static My.juli.logging.Log log = 
		My.juli.logging.LogFactory.getLog(ParallelNioSender.class);
	
	
	protected long selectTimeout = 5000; //default 5 seconds
	protected Selector selector;
    protected HashMap nioSenders = new HashMap();
    
    public ParallelNioSender() throws IOException {
        selector = Selector.open();
        setConnected(true);
    }
    
    
	
	
	public synchronized void sendMessage(Member[] destination, ChannelMessage msg)
			throws ChannelException {

		long start = System.currentTimeMillis();
		byte[] data = XByteBuffer.createDataPackage((ChannelData)msg);
		
		NioSender[] senders = setupForSend(destination);
		
		connect(senders);
		
		setData(senders,data);
		
		int remaining = senders.length;
		ChannelException cx = null;
		
		try {
			//loop until complete, an error happens, or we timeout
            long delta = System.currentTimeMillis() - start;
            boolean waitForAck = (Channel.SEND_OPTIONS_USE_ACK & msg.getOptions()) == Channel.SEND_OPTIONS_USE_ACK;
            
            while ( (remaining>0) && (delta < getTimeout()) ) {
            	try {
            		remaining -= doLoop(selectTimeout, getMaxRetryAttempts(),waitForAck,msg);
            	}
            	catch (Exception x ) {
            		//....
            	}
            	
            	
            }
            
            if ( remaining > 0 ) {
            	//timeout has occured
            	ChannelException cxtimeout = 
            		new ChannelException("Operation has timed out("+getTimeout()+" ms.).");
            	
            	if ( cx==null ) cx = 
            		new ChannelException("Operation has timed out("+getTimeout()+" ms.).");
            	
            	for (int i=0; i<senders.length; i++ ) {
                    if (!senders[i].isComplete() ) 
                    	cx.addFaultyMember(senders[i].getDestination(),cxtimeout);
                }
            	throw cx;
            }
            else if ( cx != null ) {
                //there was an error
                throw cx;
            }
            
		}catch (Exception x ) {
			try { this.disconnect(); } catch (Exception ignore) {}
            if ( x instanceof ChannelException ) throw (ChannelException)x;
            else throw new ChannelException(x);
		}
		
		
	}
	
	
	
	private int doLoop(long selectTimeOut, int maxAttempts, boolean waitForAck, ChannelMessage msg) throws IOException, ChannelException {
		int completed = 0;
		int selectedKeys = selector.select(selectTimeOut);
		
		if (selectedKeys == 0) {
            return 0;
        }
		
		Iterator it = selector.selectedKeys().iterator();
		
		while (it.hasNext()) {
			SelectionKey sk = (SelectionKey) it.next();
			it.remove();
			
			int readyOps = sk.readyOps();
			sk.interestOps(sk.interestOps() & ~readyOps);
			
			NioSender sender = (NioSender) sk.attachment();
			
			try {
				if (sender.process(sk,waitForAck)) {
					completed++;
					sender.setComplete(true);
					
					SenderState.getSenderState(sender.getDestination()).setReady();
				}
			}
			catch (Exception x) {
				//....
			}
		}
		
		return completed;
	}
	
	
	
	private NioSender[] setupForSend(Member[] destination) throws ChannelException {
		ChannelException cx = null;
		NioSender[] result = new NioSender[destination.length];
		for ( int i=0; i<destination.length; i++ ) {
			NioSender sender = (NioSender)nioSenders.get(destination[i]);
			try {
				
				if (sender == null) {
					sender = new NioSender();
					sender.transferProperties(this, sender);
					nioSenders.put(destination[i], sender);
				}
				if (sender != null) {
					sender.reset();
					sender.setDestination(destination[i]);
					sender.setSelector(selector);
					result[i] = sender;
				}
			}
			catch ( UnknownHostException x ) {
				if (cx == null) 
					cx = new ChannelException("Unable to setup NioSender.", x);
			}
		}
		if ( cx != null ) 
			throw cx;
        else 
        	return result;
	}
	


	private void connect(NioSender[] senders) throws ChannelException {
		ChannelException x = null;
		
		for (int i=0; i<senders.length; i++ ) {
			try {
				if (!senders[i].isConnected()) 
					senders[i].connect();
			}
			catch ( IOException io ) {
				if ( x==null ) x = new ChannelException(io);
			}
		}
		if ( x != null ) throw x;
	}
	
	
	private void setData(NioSender[] senders, byte[] data) throws ChannelException {
		
		ChannelException x = null;
		
		for (int i=0; i<senders.length; i++ ) {
			try {
                senders[i].setMessage(data);
			}
			catch ( IOException io ) {
                if ( x==null ) 
                	x = new ChannelException(io);
			}
		}
		if ( x != null ) throw x;
	}

	public synchronized void disconnect() {
		setConnected(false);
		try {
			close(); 
		}catch (Exception x){}
		
	}
	
	
	private synchronized void close() throws ChannelException  {
		
		ChannelException x = null;
		
		Object[] members = nioSenders.keySet().toArray();
		for (int i=0; i<members.length; i++ ) {
			Member mbr = (Member)members[i];
			try {
				NioSender sender = (NioSender)nioSenders.get(mbr);
				sender.disconnect();
			}
			catch ( Exception e ) {
				if ( x == null ) 
					x = new ChannelException(e);
			}
			nioSenders.remove(mbr);
		}
		if ( x != null ) throw x;
	}


	public void connect() throws IOException {
		//do nothing, we connect on demand
        setConnected(true);
	}
	
	
	
	public boolean keepalive() {
		
		boolean result = false;
		
		for ( Iterator i = nioSenders.entrySet().iterator(); i.hasNext();  ) {
			Map.Entry entry = (Map.Entry)i.next();
            NioSender sender = (NioSender)entry.getValue();
            
            if ( sender.keepalive() ) {
            	i.remove();
                result = true;
            }
            else {
            	try{
            		sender.read(null);
            	}
            	catch ( IOException x ) {
            		//...
            	}
            }
            
            
		}
		
		//clean up any cancelled keys
        if ( result ) try { selector.selectNow(); }catch (Exception ignore){}
		
		
		return result;
		
	}

}
