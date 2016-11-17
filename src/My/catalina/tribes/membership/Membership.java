package My.catalina.tribes.membership;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import My.catalina.tribes.Member;

/**
 * A <b>membership</b> implementation using simple multicast.
 * This is the representation of a multicast membership.
 * This class is responsible for maintaining a list of active cluster nodes in the cluster.
 * If a node fails to send out a heartbeat, the node will be dismissed.
 */

public class Membership {

	protected static final MemberImpl[] EMPTY_MEMBERS = new MemberImpl[0];
	
	private final Object membersLock = new Object();
	
	/**
     * The name of this membership, has to be the same as the name for the local
     * member
     */
    protected MemberImpl local;
    
    /**
     * A map of all the members in the cluster.
     */
    protected HashMap map = new HashMap();
    
    /**
     * A list of all the members in the cluster.
     */
    protected MemberImpl[] members = EMPTY_MEMBERS;
    
    /**
     * sort members by alive time
     */
   protected Comparator memberComparator = new MemberComparator();
    
    
    /**
     * Constructs a new membership
     * @param name - has to be the name of the local member. Used to filter the local member from the cluster membership
     */
    public Membership(MemberImpl local, boolean includeLocal) {
    	this.local = local;
        if ( includeLocal ) 
        	addMember(local);
    }
    
    public Membership(MemberImpl local) {
        this(local,false);
    }
    
    public Membership(MemberImpl local, Comparator comp) {
        this(local,comp,false);
    }

    public Membership(MemberImpl local, Comparator comp, boolean includeLocal) {
        this(local,includeLocal);
        this.memberComparator = comp;
    }
    
    
    /**
     * Reset the membership and start over fresh.
     * Ie, delete all the members and wait for them to ping again and join this membership
     */
    public synchronized void reset() {
        map.clear();
        members = EMPTY_MEMBERS ;
    }
    
    
    
    /**
     * Notify the membership that this member has announced itself.
     *
     * @param member - the member that just pinged us
     * @return - true if this member is new to the cluster, false otherwise.
     * @return - false if this member is the local member or updated.
     */
    public synchronized boolean memberAlive(MemberImpl member) {
    	boolean result = false;
    	
    	//ignore ourselves
        if (  member.equals(local) ) 
        	return result;
        
        //return true if the membership has changed
        MbrEntry entry = (MbrEntry)map.get(member);
        if ( entry == null ) {
        	entry = addMember(member);
        	result = true;
        }
        else {
        	//update the member alive time
        	//..
        }
        entry.accessed();
        
        return result;
        
    }
    
    
    
    /**
     * Add a member to this component and sort array with memberComparator
     * @param member The member to add
     */
    public synchronized MbrEntry addMember(MemberImpl member) {
    	
    	synchronized (membersLock) {
    		MbrEntry entry = new MbrEntry(member);
    		if (!map.containsKey(member) ) {
    			 map.put(member, entry);
    			 MemberImpl results[] = new MemberImpl[members.length + 1];
    			 for (int i = 0; i < members.length; i++) 
    				 results[i] = members[i];
    			 
    			 results[members.length] = member;
    			 members = results;
    			 
    			 Arrays.sort(members, memberComparator);
    		}
    		return entry;
    	}
    }
    
    
    /**
     * Remove a member from this component.
     * 
     * @param member The member to remove
     */
    public void removeMember(MemberImpl member) {
    	map.remove(member);
    	synchronized (membersLock) {
    		int n = -1;
    		for (int i = 0; i < members.length; i++) {
    			if (members[i] == member || members[i].equals(member)) {
    				n = i;
                    break;
    			}
    		}
    		if (n < 0) return;
    		
    		MemberImpl results[] = new MemberImpl[members.length - 1];
    		int j = 0;
            for (int i = 0; i < members.length; i++) {
                if (i != n)
                    results[j++] = members[i];
            }
            
            members = results;
    	}
    	
    }
    
    
    
    /**
     * Runs a refresh cycle and returns a list of members that has expired.
     * This also removes the members from the membership, in such a way that
     * getMembers() = getMembers() - expire()
     * @param maxtime - the max time a member can remain unannounced before it is considered dead.
     * @return the list of expired members
     */
    public synchronized MemberImpl[] expire(long maxtime) {
    	if(!hasMembers() )
    		return EMPTY_MEMBERS;
    	
    	return null;
    }
    
    
    /**
     * Returning that service has members or not
     */
    public boolean hasMembers() {
        return members.length > 0 ;
    }
    
    /**
     * Returning a list of all the members in the membership
     * We not need a copy: add and remove generate new arrays.
     */
    public MemberImpl[] getMembers() {
        if(hasMembers()) {
            return members;
        } else {
            return EMPTY_MEMBERS;
        }
    }
    
    
    public MemberImpl getMember(Member mbr) {
        if(hasMembers()) {
            MemberImpl result = null;
            for ( int i=0; i<this.members.length && result==null; i++ ) {
                if ( members[i].equals(mbr) ) 
                	result = members[i];
            }//for
            return result;
        } else {
            return null;
        }
    }
    
    
    
	// -------------------------- Inner Class -------------------------
    
    private class MemberComparator implements java.util.Comparator {
    	
    	public int compare(Object o1, Object o2) {
    		try {
                return compare((MemberImpl) o1, (MemberImpl) o2);
            } catch (ClassCastException x) {
                return 0;
            }
    	}
    	
    	public int compare(MemberImpl m1, MemberImpl m2) {
            //longer alive time, means sort first
            long result = m2.getMemberAliveTime() - m1.getMemberAliveTime();
            if (result < 0)
                return -1;
            else if (result == 0)
                return 0;
            else
                return 1;
        }
    	
    }
    
    
    
    /**
     * Inner class that represents a member entry
     */
    protected static class MbrEntry{
    	protected MemberImpl mbr;
        protected long lastHeardFrom;

        public MbrEntry(MemberImpl mbr) {
           this.mbr = mbr;
        }

        /**
         * Indicate that this member has been accessed.
         */
        public void accessed(){
           lastHeardFrom = System.currentTimeMillis();
        }

        /**
         * Return the actual Member object
         */
        public MemberImpl getMember() {
            return mbr;
        }

        /**
         * Check if this dude has expired
         * @param maxtime The time threshold
         */
        public boolean hasExpired(long maxtime) {
            long delta = System.currentTimeMillis() - lastHeardFrom;
            return delta > maxtime;
        }
    }
    
    
}
