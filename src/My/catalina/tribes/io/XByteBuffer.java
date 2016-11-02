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
