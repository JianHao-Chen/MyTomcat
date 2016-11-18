package My.catalina.tribes;

import java.io.Serializable;
import java.util.Arrays;

public final class UniqueId implements Serializable{
    protected byte[] id;
    
    public UniqueId() {
    }

    public UniqueId(byte[] id) {
        this.id = id;
    }
    
    public UniqueId(byte[] id, int offset, int length) {
        this.id = new byte[length];
        System.arraycopy(id,offset,this.id,0,length);
    }
    
    public int hashCode() {
        if ( id == null ) return 0;
        return Arrays.hashCode(id);
    }
    
    public boolean equals(Object other) {
        boolean result = (other instanceof UniqueId);
        if ( result ) {
            UniqueId uid = (UniqueId)other;
            if ( this.id == null && uid.id == null ) result = true;
            else if ( this.id == null && uid.id != null ) result = false;
            else if ( this.id != null && uid.id == null ) result = false;
            else result = Arrays.equals(this.id,uid.id);
        }//end if
        return result;
    }
    
    public byte[] getBytes() {
        return id;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("UniqueId");
        buf.append(My.catalina.tribes.util.Arrays.toString(id));
        return buf.toString();
    }

}
