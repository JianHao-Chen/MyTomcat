package My.catalina.tribes.io;

import My.catalina.tribes.Channel;
import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;
import My.catalina.tribes.membership.MemberImpl;
import My.catalina.tribes.util.UUIDGenerator;

public class ChannelData implements ChannelMessage{

	public static ChannelData[] EMPTY_DATA_ARRAY = new ChannelData[0];
	
	/**
     * The options this message was sent with
     */
    private int options = 0 ;
    /**
     * The message data, stored in a dynamic buffer
     */
    private XByteBuffer message ;
    /**
     * The timestamp that goes with this message
     */
    private long timestamp ;
    /**
     * A unique message id
     */
    private byte[] uniqueId ;
    /**
     * The source or reply-to address for this message
     */
    private Member address;
    
    /**
     * Creates an empty channel data with a new unique Id
     * @see #ChannelData(boolean)
     */
    public ChannelData() {
        this(true);
    }
    
    /**
     * Creates a new channel data object with data
     * @param uniqueId - unique message id
     * @param message - message data
     * @param timestamp - message timestamp
     */
    public ChannelData(byte[] uniqueId, XByteBuffer message, long timestamp) {
        this.uniqueId = uniqueId;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    
    
    /**
     * Create an empty channel data object
     * @param generateUUID boolean - if true, a unique Id will be generated
     */
    public ChannelData(boolean generateUUID) {
        if ( generateUUID ) 
        	generateUUID();
    }
    
    /**
     * @return Returns the message byte buffer
     */
    public XByteBuffer getMessage() {
        return message;
    }
    /**
     * @param message The message to send.
     */
    public void setMessage(XByteBuffer message) {
        this.message = message;
    }
    /**
     * @return Returns the timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }
    /**
     * @param timestamp The timestamp to send
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * @return Returns the uniqueId.
     */
    public byte[] getUniqueId() {
        return uniqueId;
    }
    /**
     * @param uniqueId The uniqueId to send.
     */
    public void setUniqueId(byte[] uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    /**
     * @return returns the message options 
     * see org.apache.catalina.tribes.Channel#sendMessage(org.apache.catalina.tribes.Member[], java.io.Serializable, int)
     *                                                 
     */
    public int getOptions() {
        return options;
    }
    /**
     * @param sets the message options
     */
    public void setOptions(int options) {
        this.options = options;
    }
    
    /**
     * Returns the source or reply-to address
     * @return Member
     */
    public Member getAddress() {
        return address;
    }

    /**
     * Sets the source or reply-to address
     * @param address Member
     */
    public void setAddress(Member address) {
        this.address = address;
    }
    
    
    
    /**
     * Serializes the ChannelData object into a byte[] array
     * @return byte[]
     */
    public byte[] getDataPackage()  {
    	int length = getDataPackageLength();
        byte[] data = new byte[length];
        int offset = 0;
        return getDataPackage(data,offset);
    }
    
    
    
    public byte[] getDataPackage(byte[] data, int offset)  {
    	byte[] addr = ((MemberImpl)address).getData(false);
    	
    	XByteBuffer.toBytes(options,data,offset);
    	offset += 4; //options
    	
    	XByteBuffer.toBytes(timestamp,data,offset);
    	offset += 8; //timestamp
    	
    	XByteBuffer.toBytes(uniqueId.length,data,offset);
        offset += 4; //uniqueId.length
        System.arraycopy(uniqueId,0,data,offset,uniqueId.length);
        offset += uniqueId.length; //uniqueId data
    	
        XByteBuffer.toBytes(addr.length,data,offset);
        offset += 4; //addr.length
        System.arraycopy(addr,0,data,offset,addr.length);
        offset += addr.length; //addr data

        XByteBuffer.toBytes(message.getLength(),data,offset);
        offset += 4; //message.length
        System.arraycopy(message.getBytesDirect(),0,data,offset,message.getLength());
        offset += message.getLength(); //message data
        return data;
    }
    
    
    public int getDataPackageLength() {
    	int length = 
            4 + //options
            8 + //timestamp  off=4
            4 + //unique id length off=12
            uniqueId.length+ //id data off=12+uniqueId.length
            4 + //addr length off=12+uniqueId.length+4
            ((MemberImpl)address).getDataLength()+ //member data off=12+uniqueId.length+4+add.length
            4 + //message length off=12+uniqueId.length+4+add.length+4
            message.getLength();
        return length;
    }
    
    
    
    public static ChannelData getDataFromPackage(byte[] b)  {
    	ChannelData data = new ChannelData(false);
    	
    	int offset = 0;
        data.setOptions(XByteBuffer.toInt(b,offset));
        offset += 4; //options
        data.setTimestamp(XByteBuffer.toLong(b,offset));
        offset += 8; //timestamp
        data.uniqueId = new byte[XByteBuffer.toInt(b,offset)];
        offset += 4; //uniqueId length
        System.arraycopy(b,offset,data.uniqueId,0,data.uniqueId.length);
        offset += data.uniqueId.length; //uniqueId data
        byte[] addr = new byte[XByteBuffer.toInt(b,offset)];
        offset += 4; //addr length
        System.arraycopy(b,offset,addr,0,addr.length);
        data.setAddress(MemberImpl.getMember(addr));
        offset += addr.length; //addr data
        int xsize = XByteBuffer.toInt(b,offset);
        //data.message = new XByteBuffer(new byte[xsize],false);
        data.message = BufferPool.getBufferPool().getBuffer(xsize,false);
        offset += 4; //message length
        System.arraycopy(b,offset,data.message.getBytesDirect(),0,xsize);
        data.message.append(b,offset,xsize);
        offset += xsize; //message data
        return data;
    	
    }
    
    
    /**
     * Deserializes a ChannelData object from a byte array
     */
    public static ChannelData getDataFromPackage(XByteBuffer xbuf)  {
    	
    	ChannelData data = new ChannelData(false);
    	int offset = 0;
    	
    	data.setOptions(XByteBuffer.toInt(xbuf.getBytesDirect(),offset));
    	offset += 4; //options
    	data.setTimestamp(XByteBuffer.toLong(xbuf.getBytesDirect(),offset));
    	offset += 8; //timestamp
    	data.uniqueId = new byte[XByteBuffer.toInt(xbuf.getBytesDirect(),offset)];
    	offset += 4; //uniqueId length
   
    	System.arraycopy(xbuf.getBytesDirect(),offset,data.uniqueId,0,data.uniqueId.length);
    	offset += data.uniqueId.length; //uniqueId data
    	
    	int addrlen = XByteBuffer.toInt(xbuf.getBytesDirect(),offset);
        offset += 4; //addr length
        data.setAddress(MemberImpl.getMember(xbuf.getBytesDirect(),offset,addrlen));
        offset += addrlen; //addr data
        
        int xsize = XByteBuffer.toInt(xbuf.getBytesDirect(),offset);
        offset += 4; //xsize length
        System.arraycopy(xbuf.getBytesDirect(),offset,xbuf.getBytesDirect(),0,xsize);
        xbuf.setLength(xsize);
        
        data.message = xbuf;
        return data;
    }
    
    
    
    /**
     * Generates a UUID and invokes setUniqueId
     */
    public void generateUUID() {
        byte[] data = new byte[16];
        UUIDGenerator.randomUUID(false,data,0);
        setUniqueId(data);
    }
    
    
    /**
     * Create a shallow clone, only the data gets recreated
     * @return ClusterData
     */
    public Object clone() {
    	ChannelData clone = new ChannelData(false);
        clone.options = this.options;
        clone.message = new XByteBuffer(this.message.getBytesDirect(),false);
        clone.timestamp = this.timestamp;
        clone.uniqueId = this.uniqueId;
        clone.address = this.address;
        return clone;
    }
    
    /**
     * Complete clone
     */
    public Object deepclone() {
        byte[] d = this.getDataPackage();
        return ChannelData.getDataFromPackage(d);
    }
    
    
    /**
     * Utility method, returns true if the options flag indicates that an ack
     * is to be sent after the message has been received and processed
     */
    public static boolean sendAckSync(int options) {
    	return ( (Channel.SEND_OPTIONS_USE_ACK & options) == Channel.SEND_OPTIONS_USE_ACK) &&
        ( (Channel.SEND_OPTIONS_SYNCHRONIZED_ACK & options) == Channel.SEND_OPTIONS_SYNCHRONIZED_ACK);
    }
    
    /**
     * Utility method, returns true if the options flag indicates that an ack
     * is to be sent after the message has been received but not yet processed
     */
    public static boolean sendAckAsync(int options) {
    	 return ( (Channel.SEND_OPTIONS_USE_ACK & options) == Channel.SEND_OPTIONS_USE_ACK) &&
         ( (Channel.SEND_OPTIONS_SYNCHRONIZED_ACK & options) != Channel.SEND_OPTIONS_SYNCHRONIZED_ACK);
    }
    
}
