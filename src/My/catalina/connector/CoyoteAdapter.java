package My.catalina.connector;

import java.io.IOException;

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

	// ---------------------- Constants ----------------------
	public static final int ADAPTER_NOTES = 1;
	
	
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
	public void service(My.coyote.Request req, My.coyote.Response res) throws Exception {
		
		Request request = (Request) req.getNote(ADAPTER_NOTES);
		Response response = (Response) res.getNote(ADAPTER_NOTES);
		
		if (request == null) {
			
			// Create objects
            request = (Request) connector.createRequest();
            request.setCoyoteRequest(req);
            
            response = (Response) connector.createResponse();
            response.setCoyoteResponse(res);
            
            
            // Link objects
            request.setResponse(response);
            response.setRequest(request);
            
            // Set as notes
            req.setNote(ADAPTER_NOTES, request);
            res.setNote(ADAPTER_NOTES, response);
            
            // Set query string encoding
            req.getParameters().setQueryStringEncoding
            (connector.getURIEncoding());
		}
		
		
		
		try {
			
			// Parse and set Catalina and configuration specific 
            // request parameters
			req.getRequestProcessor().setWorkerThreadName(Thread.currentThread().getName());
			
			if (postParseRequest(req, request, res, response)) {
				
			}
			
		}
		catch (IOException e) {
			
		}
		catch (Throwable t) {
			
		}
		finally {
			
		}
		
		
		
		
		
	}



	@Override
	public boolean event(My.coyote.Request req, My.coyote.Response res, SocketStatus status)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void log(My.coyote.Request req, My.coyote.Response res, long time) {
		// TODO Auto-generated method stub
		
	}
	
    
    
    
	
   
	 /**
     * Parse additional request parameters.
     */
    protected boolean postParseRequest(My.coyote.Request req, 
                                       Request request,
    		                       My.coyote.Response res, 
                                       Response response)
            throws Exception {
    	
    	return true;
    	
    }
    
	
}
