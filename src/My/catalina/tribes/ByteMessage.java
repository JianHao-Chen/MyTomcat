package My.catalina.tribes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class ByteMessage implements Serializable, Externalizable{
	
	/**
     * Storage for the message to be sent
     */
    private byte[] message;
    
    /**
     * Creates an empty byte message
     * Constructor also for deserialization
     */
    public ByteMessage() {
    }

    /**
     * Creates a byte message wit h
     * @param data byte[] - the message contents
     */
    public ByteMessage(byte[] data) {
        message = data;
    }
    
    /**
     * Returns the message contents of this byte message
     * @return byte[] - message contents, can be null
     */
    public byte[] getMessage() {
        return message;
    }
    
    /**
     * Sets the message contents of this byte message
     * @param message byte[]
     */
    public void setMessage(byte[] message) {
        this.message = message;
    }

    /**
     * @see java.io.Externalizable#readExternal
     * @param in ObjectInput
     * @throws IOException
     */
    public void readExternal(ObjectInput in ) throws IOException {
        int length = in.readInt();
        message = new byte[length];
        in.readFully(message);
    }

    /**
     * @see java.io.Externalizable#writeExternal
     * @param out ObjectOutput
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(message!=null?message.length:0);
        if ( message!=null ) out.write(message,0,message.length);
    }

}
