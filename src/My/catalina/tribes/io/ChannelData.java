package My.catalina.tribes.io;

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
    
    
    
    /**
     * Generates a UUID and invokes setUniqueId
     */
    public void generateUUID() {
        byte[] data = new byte[16];
        UUIDGenerator.randomUUID(false,data,0);
        setUniqueId(data);
    }
    
    
}
