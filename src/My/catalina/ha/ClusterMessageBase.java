package My.catalina.ha;

import My.catalina.tribes.Member;

public class ClusterMessageBase implements ClusterMessage{

	protected transient Member address;
    private String uniqueId;
    private long timestamp;
    public ClusterMessageBase() {
    }
    
    public Member getAddress() {
        return address;
    }
    
    public void setAddress(Member member) {
        this.address = member;
    }
    
    
    public String getUniqueId() {
        return uniqueId;
    }
    
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    

    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
