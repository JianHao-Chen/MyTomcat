package My.catalina.ha.session;

import My.catalina.ha.ClusterMessageBase;

public class SessionMessageImpl 
	extends ClusterMessageBase 
	implements SessionMessage, java.io.Serializable{

	public SessionMessageImpl() {
    }
	
	/*
     * Private serializable variables to keep the messages state
     */
    private int mEvtType = -1;
    private byte[] mSession;
    private String mSessionID;

    private String mContextName;
    private long serializationTimestamp;
    private boolean timestampSet = false ;
    private String uniqueId;
    
    
    private SessionMessageImpl( String contextName,
            int eventtype,
            byte[] session,
            String sessionID){
    	
    	mEvtType = eventtype;
        mSession = session;
        mSessionID = sessionID;
        mContextName = contextName;
        uniqueId = sessionID;
    }
    
    
    /**
     * Creates a session message. Depending on what event type you want this
     * message to represent, you populate the different parameters in the constructor<BR>
      * The following rules apply dependent on what event type argument you use:<BR>
     * <B>EVT_SESSION_CREATED</B><BR>
     *    The parameters: session, sessionID must be set.<BR>
     * <B>EVT_SESSION_EXPIRED</B><BR>
     *    The parameters: sessionID must be set.<BR>
     * <B>EVT_SESSION_ACCESSED</B><BR>
     *    The parameters: sessionID must be set.<BR>
     * <B>EVT_GET_ALL_SESSIONS</B><BR>
     *    get all sessions from from one of the nodes.<BR>
     * <B>EVT_SESSION_DELTA</B><BR>
     *    Send attribute delta (add,update,remove attribute or principal, ...).<BR>
     * <B>EVT_ALL_SESSION_DATA</B><BR>
     *    Send complete serializes session list<BR>
     * <B>EVT_ALL_SESSION_TRANSFERCOMPLETE</B><BR>
     *    send that all session state information are transfered
     *    after GET_ALL_SESSION received from this sender.<BR>
     * <B>EVT_CHANGE_SESSION_ID</B><BR>
     *    send original sessionID and new sessionID.<BR>
     * @param contextName - the name of the context (application
     * @param eventtype - one of the 8 event type defined in this class
     * @param session - the serialized byte array of the session itself
     * @param sessionID - the id that identifies this session
     * @param uniqueID - the id that identifies this message
     */
    public SessionMessageImpl( String contextName,
                           int eventtype,
                           byte[] session,
                           String sessionID,
                           String uniqueID)
    {
    	
    }
    
    
    
}
