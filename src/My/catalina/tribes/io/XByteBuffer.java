package My.catalina.tribes.io;

/**
 * The XByteBuffer provides a dual functionality.
 * One, it stores message bytes and automatically extends the byte buffer if needed.<BR>
 * Two, it can encode and decode packages so that they can be defined and identified
 * as they come in on a socket.
 * <br>
 * <b>THIS CLASS IS NOT THREAD SAFE</B><BR>
 * <br/>
 */

public class XByteBuffer {

	public static My.juli.logging.Log log =
        My.juli.logging.LogFactory.getLog( XByteBuffer.class );
	
	/**
     * This is a package header, 7 bytes (FLT2002)
     */
    public static final byte[] START_DATA = {70,76,84,50,48,48,50};
    
    /**
     * This is the package footer, 7 bytes (TLF2003)
     */
    public static final byte[] END_DATA = {84,76,70,50,48,48,51};
 
    /**
     * Default size on the initial byte buffer
     */
    private static final int DEF_SIZE = 2048;
 
    /**
     * Default size to extend the buffer with
     */
    private static final int DEF_EXT  = 1024;
    
    /**
     * Variable to hold the data
     */
    protected byte[] buf = null;
    
    /**
     * Current length of data in the buffer
     */
    protected int bufSize = 0;
    
    /**
     * Flag for discarding invalid packages
     * If this flag is set to true, and append(byte[],...) is called,
     * the data added will be inspected, and if it doesn't start with 
     * <code>START_DATA</code> it will be thrown away.
     * 
     */
    protected boolean discard = true;

    /**
     * Constructs a new XByteBuffer
     * @param size - the initial size of the byte buffer
     * @todo use a pool of byte[] for performance
     */
    public XByteBuffer(int size, boolean discard) {
        buf = new byte[size];
        this.discard = discard;
    }
    
    public XByteBuffer(byte[] data,boolean discard) {
        this(data,data.length+128,discard);
    }
    
    public XByteBuffer(byte[] data, int size,boolean discard) {
        int length = Math.max(data.length,size);
        buf = new byte[length];
        System.arraycopy(data,0,buf,0,data.length);
        bufSize = data.length;
        this.discard = discard;
    }
    
    
    public int getLength() {
        return bufSize;
    }

    public void setLength(int size) {
        if ( size > buf.length ) throw new ArrayIndexOutOfBoundsException("Size is larger than existing buffer.");
        bufSize = size;
    }
    
    
    public byte[] getBytesDirect() {
        return this.buf;
    }
    
    
    /**
     * Returns the bytes in the buffer, in its exact length
     */
    public byte[] getBytes() {
        byte[] b = new byte[bufSize];
        System.arraycopy(buf,0,b,0,bufSize);
        return b;
    }
    
    /**
     * Resets the buffer
     */
    public void clear() {
        bufSize = 0;
    }
    
    
    
    
    /**
     * Creates a complete data package
     * @param indata - the message data to be contained within the package
     * @param compressed - compression flag for the indata buffer
     * @return - a full package (header,size,data,footer)
     * 
     */
    public static byte[] createDataPackage(ChannelData cdata) {
    	int dlength = cdata.getDataPackageLength();
    	
    	int length = getDataPackageLength(dlength);
    	byte[] data = new byte[length];
    	
    	int offset = 0;
        System.arraycopy(START_DATA, 0, data, offset, START_DATA.length);
        offset += START_DATA.length;
        
        toBytes(dlength,data, START_DATA.length);
        offset += 4;
        cdata.getDataPackage(data,offset);
        offset += dlength;
        System.arraycopy(END_DATA, 0, data, offset, END_DATA.length);
        offset += END_DATA.length;
        return data;
    }
    
    public static int getDataPackageLength(int datalength) {
        int length = 
            START_DATA.length + //header length
            4 + //data length indicator
            datalength + //actual data length
            END_DATA.length; //footer length
        return length;

    }
    
    
    
    
    
    
    
    
	
	
	/**
     * Convert four bytes to an int
     * @param b - the byte array containing the four bytes
     * @param off - the offset
     * @return the integer value constructed from the four bytes
     * @exception java.lang.ArrayIndexOutOfBoundsException
     */
    public static int toInt(byte[] b,int off){
        return ( ( (int) b[off+3]) & 0xFF) +
            ( ( ( (int) b[off+2]) & 0xFF) << 8) +
            ( ( ( (int) b[off+1]) & 0xFF) << 16) +
            ( ( ( (int) b[off+0]) & 0xFF) << 24);
    }

    /**
     * Convert eight bytes to a long
     * @param b - the byte array containing the four bytes
     * @param off - the offset
     * @return the long value constructed from the eight bytes
     * @exception java.lang.ArrayIndexOutOfBoundsException
     */
    public static long toLong(byte[] b,int off){
        return ( ( (long) b[off+7]) & 0xFF) +
            ( ( ( (long) b[off+6]) & 0xFF) << 8) +
            ( ( ( (long) b[off+5]) & 0xFF) << 16) +
            ( ( ( (long) b[off+4]) & 0xFF) << 24) +
            ( ( ( (long) b[off+3]) & 0xFF) << 32) +
            ( ( ( (long) b[off+2]) & 0xFF) << 40) +
            ( ( ( (long) b[off+1]) & 0xFF) << 48) +
            ( ( ( (long) b[off+0]) & 0xFF) << 56);
    }

    
    /**
     * Converts an integer to four bytes
     * @param n - the integer
     * @return - four bytes in an array
     * @deprecated use toBytes(boolean,byte[],int)
     */
    public static byte[] toBytes(boolean bool) {
        byte[] b = new byte[1] ;
        return toBytes(bool,b,0);

    }
    
    public static byte[] toBytes(boolean bool, byte[] data, int offset) {
        data[offset] = (byte)(bool?1:0);
        return data;
    }
    
    /**
     * 
     * @param <any> long
     * @return use
     */
    public static boolean toBoolean(byte[] b, int offset) {
        return b[offset] != 0;
    }

    
    /**
     * Converts an integer to four bytes
     * @param n - the integer
     * @return - four bytes in an array
     * @deprecated use toBytes(int,byte[],int)
     */
    public static byte[] toBytes(int n) {
        return toBytes(n,new byte[4],0);
    }

    public static byte[] toBytes(int n,byte[] b, int offset) {
        b[offset+3] = (byte) (n);
        n >>>= 8;
        b[offset+2] = (byte) (n);
        n >>>= 8;
        b[offset+1] = (byte) (n);
        n >>>= 8;
        b[offset+0] = (byte) (n);
        return b;
    }

    /**
     * Converts an long to eight bytes
     * @param n - the long
     * @return - eight bytes in an array
     * @deprecated use toBytes(long,byte[],int)
     */
    public static byte[] toBytes(long n) {
        return toBytes(n,new byte[8],0);
    }
    public static byte[] toBytes(long n, byte[] b, int offset) {
        b[offset+7] = (byte) (n);
        n >>>= 8;
        b[offset+6] = (byte) (n);
        n >>>= 8;
        b[offset+5] = (byte) (n);
        n >>>= 8;
        b[offset+4] = (byte) (n);
        n >>>= 8;
        b[offset+3] = (byte) (n);
        n >>>= 8;
        b[offset+2] = (byte) (n);
        n >>>= 8;
        b[offset+1] = (byte) (n);
        n >>>= 8;
        b[offset+0] = (byte) (n);
        return b;
    }
    
    
    /**
     * Similar to a String.IndexOf, but uses pure bytes
     * @param src - the source bytes to be searched
     * @param srcOff - offset on the source buffer
     * @param find - the string to be found within src
     * @return - the index of the first matching byte. -1 if the find array is not found
     */
    public static int firstIndexOf(byte[] src, int srcOff, byte[] find){
    	int result = -1;
        if (find.length > src.length) 
        	return result;
        
        if (find.length == 0 || src.length == 0) 
        	return result;
        
        if (srcOff >= src.length ) 
        	throw new java.lang.ArrayIndexOutOfBoundsException();
        
        boolean found = false;
        int srclen = src.length;
        int findlen = find.length;
        byte first = find[0];
        int pos = srcOff;
        
        while (!found) {
        	//find the first byte
        	while (pos < srclen){
        		if (first == src[pos])
                    break;
        		pos++;
        	}
        	if (pos >= srclen)
                return -1;
        	
        	//we found the first character
            //match the rest of the bytes - they have to match
            if ( (srclen - pos) < findlen)
                return -1;
            
            //assume it does exist
            found = true;
            for (int i = 1; ( (i < findlen) && found); i++)
                found = found && (find[i] == src[pos + i]);
            if (found)
                result = pos;
            else if ( (srclen - pos) < findlen)
                return -1; //no more matches possible
            else
                pos++;
        }
        
        return result;
        
    }
    
	
}
