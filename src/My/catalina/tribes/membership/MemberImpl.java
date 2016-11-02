package My.catalina.tribes.membership;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import My.catalina.tribes.Member;
import My.catalina.tribes.io.XByteBuffer;

public class MemberImpl implements Member, java.io.Externalizable{

	
	public static final transient byte[] TRIBES_MBR_BEGIN = new byte[] {84, 82, 73, 66, 69, 83, 45, 66};
    public static final transient byte[] TRIBES_MBR_END   = new byte[] {84, 82, 73, 66, 69, 83, 45, 69};
    
    
    /**
     * The listen host for this member
     */
    protected byte[] host;
    
    protected transient String hostname;
    
    /**
     * The tcp listen port for this member
     */
    protected int port;
    
    
    /**
     * Counter for how many broadcast messages have been sent from this member
     */
    protected int msgCount = 0;
    
    /**
     * The number of milliseconds since this members was
     * created, is kept track of using the start time
     */
    protected long memberAliveTime = 0;
    
    /**
     * For the local member only
     */
    protected transient long serviceStartTime;
    
    
    /**
     * To avoid serialization over and over again, once the local dataPkg
     * has been set, we use that to transmit data
     */
    protected transient byte[] dataPkg = null;
    
    
    
    /**
     * Unique session Id for this member
     */
    protected byte[] uniqueId = new byte[16];
    
    
    /**
     * Custom payload that an app framework can broadcast
     * Also used to transport stop command.
     */
    protected byte[] payload = new byte[0];
    
    /**
     * Command, so that the custom payload doesn't have to be used
     * This is for internal tribes use, such as SHUTDOWN_COMMAND
     */
    protected byte[] command = new byte[0];
    
    /**
     * Domain if we want to filter based on domain.
     */
    protected byte[] domain = new byte[0];
    
    
    
	
	/**
     * Empty constructor for serialization
     */
    public MemberImpl() {
        
    }
    
    
    /**
     * Construct a new member object
     * @param name - the name of this member, cluster unique
     * @param domain - the cluster domain name of this member
     * @param host - the tcp listen host
     * @param port - the tcp listen port
     */
    public MemberImpl(String host,
                      int port,
                      long aliveTime) throws IOException {
    	
    	setHostname(host);
        this.port = port;
        this.memberAliveTime=aliveTime;
    }
	
    
    
    public void setHost(byte[] host) {
        this.host = host;
    }
    
    public void setHostname(String host) throws IOException {
        hostname = host;
        this.host = java.net.InetAddress.getByName(host).getAddress();
    }
    
    
    
    /**
     * Return the listen port of this member
     * @return - tcp listen port
     */
    public int getPort()  {
        return this.port;
    }
    
    public void setPort(int port) {
        this.port = port;
        this.dataPkg = null;
    }
    
    
    
    /**
     * Contains information on how long this member has been online.
     * The result is the number of milli seconds this member has been
     * broadcasting its membership to the cluster.
     * @return nr of milliseconds since this member started.
     */
    public long getMemberAliveTime() {
       return memberAliveTime;
    }
    
    public void setMemberAliveTime(long time) {
        memberAliveTime=time;
    }
    
    
    public long getServiceStartTime() {
        return serviceStartTime;
    }
    
    public void setServiceStartTime(long serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }
    
    
    
    public void setPayload(byte[] payload) {
    	byte[] oldpayload = this.payload;
    	this.payload = payload!=null?payload:new byte[0];
    	if ( this.getData(true,true).length > McastServiceImpl.MAX_PACKET_SIZE ) {
    		 this.payload = oldpayload;
             throw new IllegalArgumentException("Payload is too large for tribes to handle.");
    	}
    }
    
    
    public byte[] getUniqueId() {
        return uniqueId;
    }
    
    public void setUniqueId(byte[] uniqueId) {
        this.uniqueId = uniqueId!=null?uniqueId:new byte[16];
        getData(true,true);
    }

    
    public byte[] getDomain() {
        return domain;
    }
    
    public void setDomain(byte[] domain) {
        this.domain = domain!=null?domain:new byte[0];
        getData(true,true);
    }
    
    
    
    public int getDataLength() {
    	return TRIBES_MBR_BEGIN.length+ //start pkg
		        4+ //data length
		        8+ //alive time
		        4+ //port
		        1+ //host length
		        host.length+ //host
		        4+ //command length
		        command.length+ //command
		        4+ //domain length
		        domain.length+ //domain
		        16+ //unique id
		        4+ //payload length
		        payload.length+ //payload
		        TRIBES_MBR_END.length; //end pkg
    }
    
    
    
    
    /**
     * 
     * @param getalive boolean - calculate memberAlive time
     * @param reset boolean - reset the cached data package, and create a new one
     * @return byte[]
     */
    public byte[] getData(boolean getalive, boolean reset)  {
    	if ( reset ) 
    		dataPkg = null;
    	
    	//look in cache first
    	if ( dataPkg!=null ) {
    		//...
    	}
    	
    	/*
    	package looks like:
        	start package TRIBES_MBR_BEGIN.length
        	package length - 4 bytes
        	alive - 8 bytes
        	port - 4 bytes
        	host length - 1 byte
        	host - host length bytes
        	command len - 4 bytes
        	command - command len bytes
        	domain len - 4 bytes
        	domain - domain len bytes
        	uniqueId - 16 bytes
        	payload length - 4 bytes
        	payload -  payload len bytes
        	end package TRIBES_MBR_END.length
    	*/
    	byte[] addr = host;
    	long alive=System.currentTimeMillis()-getServiceStartTime();
    	byte hostlength = (byte)addr.length;
    	
    	byte[] data = new byte[getDataLength()];
    	
    	int bodylength = (getDataLength() - TRIBES_MBR_BEGIN.length - TRIBES_MBR_END.length - 4);
    	
    	int pos = 0;
    	
    	
    	//TRIBES_MBR_BEGIN
        System.arraycopy(TRIBES_MBR_BEGIN,0,data,pos,TRIBES_MBR_BEGIN.length);
        pos += TRIBES_MBR_BEGIN.length;
        
        //package length
        XByteBuffer.toBytes(bodylength,data,pos);
        pos += 4;
        
        //alive data
        XByteBuffer.toBytes((long)alive,data,pos);
        pos += 8;
        
        //port
        XByteBuffer.toBytes(port,data,pos);
        pos += 4;
        
        //host length
        data[pos++] = hostlength;
        //host
        System.arraycopy(addr,0,data,pos,addr.length);
        pos+=addr.length;
        
        //command len - 4 bytes
        XByteBuffer.toBytes(command.length,data,pos);
        pos+=4;
        
        //command - command len bytes
        System.arraycopy(command,0,data,pos,command.length);
        pos+=command.length;
        
        //domain len - 4 bytes
        XByteBuffer.toBytes(domain.length,data,pos);
        pos+=4;
        //domain - domain len bytes
        System.arraycopy(domain,0,data,pos,domain.length);
        pos+=domain.length;
        
        //unique Id
        System.arraycopy(uniqueId,0,data,pos,uniqueId.length);
        pos+=uniqueId.length;
        
        //payload
        XByteBuffer.toBytes(payload.length,data,pos);
        pos+=4;
        System.arraycopy(payload,0,data,pos,payload.length);
        pos+=payload.length;
        
        //TRIBES_MBR_END
        System.arraycopy(TRIBES_MBR_END,0,data,pos,TRIBES_MBR_END.length);
        pos += TRIBES_MBR_END.length;
        
        
        //create local data
        dataPkg = data;
        return data;
    }
    
	
	
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
