package My.catalina.ha.session;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;

public class DeltaRequest implements Externalizable{
	
	public static My.juli.logging.Log log =
        My.juli.logging.LogFactory.getLog( DeltaRequest.class );
	
	public static final int TYPE_ATTRIBUTE = 0;
    public static final int TYPE_PRINCIPAL = 1;
    public static final int TYPE_ISNEW = 2;
    public static final int TYPE_MAXINTERVAL = 3;
    public static final int TYPE_AUTHTYPE = 4;

    public static final int ACTION_SET = 0;
    public static final int ACTION_REMOVE = 1;

    public static final String NAME_PRINCIPAL = "__SET__PRINCIPAL__";
    public static final String NAME_MAXINTERVAL = "__SET__MAXINTERVAL__";
    public static final String NAME_ISNEW = "__SET__ISNEW__";
    public static final String NAME_AUTHTYPE = "__SET__AUTHTYPE__";
    
    private String sessionId;
    private LinkedList actions = new LinkedList();
    private LinkedList actionPool = new LinkedList();
    
    private boolean recordAllActions = false;
    
	public DeltaRequest() {
    }
	
	public DeltaRequest(String sessionId, boolean recordAllActions) {
		this.recordAllActions = recordAllActions;
		if(sessionId != null)
            setSessionId(sessionId);
	}
	
	
	public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        if ( sessionId == null ) {
            new Exception("Session Id is null for setSessionId").fillInStackTrace().printStackTrace();
        }
    }
    
    public void reset() {
    	while ( actions.size() > 0 ) {
    		//...
    	}
    	actions.clear();
    }

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}
	
	
}
