package My.catalina.connector;

import My.coyote.Adapter;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.net.SocketStatus;

/**
 * Implementation of a request processor which delegates the 
 * processing to a Coyote processor.
 */

public class CoyoteAdapter implements Adapter{

	private static Log log = LogFactory.getLog(CoyoteAdapter.class);

	
	
	// --------------------- Constructors ---------------------
	
	 /**
     * Construct a new CoyoteProcessor associated with the specified connector.
     *
     * @param connector CoyoteConnector that owns this processor
     */
    public CoyoteAdapter(Connector connector) {

        super();
        this.connector = connector;

    }
    
    
    
	// --------------------- Instance Variables ---------------------
    
    /**
     * The CoyoteConnector with which this processor is associated.
     */
    private Connector connector = null;


	// -------------------- Adapter Methods -------------------- 

	@Override
	public void service(Request req, Response res) throws Exception {
		// TODO Auto-generated method stub
		
	}



	@Override
	public boolean event(Request req, Response res, SocketStatus status)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void log(Request req, Response res, long time) {
		// TODO Auto-generated method stub
		
	}
	
    
    
    
	
   
    
    
	
}
