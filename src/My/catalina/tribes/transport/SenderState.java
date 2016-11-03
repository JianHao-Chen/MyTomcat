package My.catalina.tribes.transport;

import java.util.HashMap;

import My.catalina.tribes.Member;

public class SenderState {

	public static final int READY = 0;
    public static final int SUSPECT = 1;
    public static final int FAILING = 2;
    
    protected static HashMap memberStates = new HashMap();
    
    public static SenderState getSenderState(Member member) {
        return getSenderState(member,true);
    }

    public static SenderState getSenderState(Member member, boolean create) {
        SenderState state = (SenderState)memberStates.get(member);
        if ( state == null && create) {
            synchronized ( memberStates ) {
                state = (SenderState)memberStates.get(member);
                if ( state == null ) {
                    state = new SenderState();
                    memberStates.put(member,state);
                }
            }
        }
        return state;
    }
    
    public static void removeSenderState(Member member) {
        synchronized ( memberStates ) {
            memberStates.remove(member);
        }
    }
    
 // ----------------------------------------------------- Instance Variables

    private int state = READY;

    //  ----------------------------------------------------- Constructor

    
    private SenderState() {
        this(READY);
    }

    private SenderState(int state) {
        this.state = state;
    }
    
    /**
     * 
     * @return boolean
     */
    public boolean isSuspect() {
        return (state == SUSPECT) || (state == FAILING);
    }

    public void setSuspect() {
        state = SUSPECT;
    }
    
    public boolean isReady() {
        return state == READY;
    }
    
    public void setReady() {
        state = READY;
    }
    
    public boolean isFailing() {
        return state == FAILING;
    }
    
    public void setFailing() {
        state = FAILING;
    }
    
    
}
