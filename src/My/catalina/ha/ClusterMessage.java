package My.catalina.ha;

import java.io.Serializable;

import My.catalina.tribes.Member;

public interface ClusterMessage extends Serializable{
	public Member getAddress();
    public void setAddress(Member member);
    public String getUniqueId();
    public void setUniqueId(String id);
    public long getTimestamp();
    public void setTimestamp(long timestamp);
}
