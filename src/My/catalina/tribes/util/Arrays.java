package My.catalina.tribes.util;

import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;
import My.catalina.tribes.UniqueId;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class Arrays {
protected static Log log = LogFactory.getLog(Arrays.class);
    
    public static boolean contains(byte[] source, int srcoffset, byte[] key, int keyoffset, int length) {
        if ( srcoffset < 0 || srcoffset >= source.length) throw new ArrayIndexOutOfBoundsException("srcoffset is out of bounds.");
        if ( keyoffset < 0 || keyoffset >= key.length) throw new ArrayIndexOutOfBoundsException("keyoffset is out of bounds.");
        if ( length > (key.length-keyoffset) ) throw new ArrayIndexOutOfBoundsException("not enough data elements in the key, length is out of bounds.");
        //we don't have enough data to validate it
        if ( length > (source.length-srcoffset) ) return false;
        boolean match = true;
        int pos = keyoffset;
        for ( int i=srcoffset; match && i<length; i++ ) {
            match = (source[i] == key[pos++]);
        }
        return match;
    }
    
    public static String toString(byte[] data) {
        return toString(data,0,data!=null?data.length:0);
    }

    public static String toString(byte[] data, int offset, int length) {
        return toString(data,offset,length,false);
    }

    public static String toString(byte[] data, int offset, int length, boolean unsigned) {
        StringBuffer buf = new StringBuffer("{");
        if ( data != null && length > 0 ) {
            int i = offset;
            if (unsigned) {
                buf.append(data[i++] & 0xff);
                for (; i < length; i++) {
                    buf.append(", ").append(data[i] & 0xff);
                }
            } else {
                buf.append(data[i++]);
                for (; i < length; i++) {
                    buf.append(", ").append(data[i]);
                }
            }
        }
        buf.append("}");
        return buf.toString();
    }

    public static String toString(Object[] data) {
        return toString(data,0,data!=null?data.length:0);
    }
    
    public static String toString(Object[] data, int offset, int length) {
        StringBuffer buf = new StringBuffer("{");
        if ( data != null && length > 0 ) {
            buf.append(data[offset++]);
            for (int i = offset; i < length; i++) {
                buf.append(", ").append(data[i]);
            }
        }
        buf.append("}");
        return buf.toString();
    }
    
    public static String toNameString(Member[] data) {
        return toNameString(data,0,data!=null?data.length:0);
    }
    
    public static String toNameString(Member[] data, int offset, int length) {
        StringBuffer buf = new StringBuffer("{");
        if ( data != null && length > 0 ) {
            buf.append(data[offset++].getName());
            for (int i = offset; i < length; i++) {
                buf.append(", ").append(data[i].getName());
            }
        }
        buf.append("}");
        return buf.toString();
    }

    public static int add(int[] data) {
        int result = 0;
        for (int i=0;i<data.length; i++ ) result += data[i];
        return result;
    }
    
    public static UniqueId getUniqudId(ChannelMessage msg) {
        return new UniqueId(msg.getUniqueId());
    }

    public static UniqueId getUniqudId(byte[] data) {
        return new UniqueId(data);
    }
    
    public static boolean equals(byte[] o1, byte[] o2) {
        return java.util.Arrays.equals(o1,o2);
    }

    public static boolean equals(Object[] o1, Object[] o2) {
        boolean result = o1.length == o2.length;
        if ( result ) for (int i=0; i<o1.length && result; i++ ) result = o1[i].equals(o2[i]);
        return result;
    }
}
