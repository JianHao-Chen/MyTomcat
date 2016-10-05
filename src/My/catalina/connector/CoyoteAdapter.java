package My.catalina.connector;

import java.io.IOException;

import My.catalina.Context;
import My.catalina.Wrapper;
import My.coyote.Adapter;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.mapper.MappingData;
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
		
		boolean comet = false;
		
		try {
			
			// Parse and set Catalina and configuration specific 
            // request parameters
			req.getRequestProcessor().setWorkerThreadName(Thread.currentThread().getName());
			
			if (postParseRequest(req, request, res, response)) {
				
				// Calling the container
				connector.getContainer().getPipeline().getFirst().invoke(request, response);
			}
			
			if (!comet) {
				response.finishResponse();
			}
		}
		catch (IOException e) {
			
		}
		catch (Throwable t) {
			
		}
		finally {
			
			// Recycle the wrapper request and response
            if (!comet) {
            	request.recycle();
            }
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
     * Extract the path parameters from the request. This assumes parameters are
     * of the form /path;name=value;name2=value2/ etc. Currently only really
     * interested in the session ID that will be in this form. Other parameters
     * can safely be ignored.
     */
	protected void parsePathParameters(My.coyote.Request req,
            Request request) {
		
		// Process in bytes (this is default format so this is normally a NO-OP
        req.decodedURI().toBytes();
        
        ByteChunk uriBC = req.decodedURI().getByteChunk();
        
        int semicolon = uriBC.indexOf(';', 0);
        
        // What encoding to use? Some platforms, eg z/os, use a default
        // encoding that doesn't give the expected result so be explicit
        String enc = connector.getURIEncoding();
        if (enc == null) {
            enc = "ISO-8859-1";
        }
        
        boolean warnedEncoding = false;

        while (semicolon > -1) {
        	
        	// Parse path param, and extract it from 
        	// the decoded request URI
        	
        	// implements latter.
        }
        
	}
    
	
   
	 /**
     * Parse additional request parameters.
     */
    protected boolean postParseRequest(My.coyote.Request req, 
                                       Request request,
    		                       My.coyote.Response res, 
                                       Response response)
            throws Exception {
    	
    	// set scheme
    	req.scheme().setString(connector.getScheme());
    	
    	// Copy the raw URI to the decoded URI
    	MessageBytes decodedURI = req.decodedURI();
    	decodedURI.duplicate(req.requestURI());
    	
    	
    	// Parse the path parameters. This will:
        //   - strip out the path parameters
        //   - convert the decodedURI to bytes
        parsePathParameters(req, request);
    	
        
        
        // Request mapping.
        
        MessageBytes serverName = req.serverName();;
        
        MappingData mappingData = request.getMappingData();
        
        connector.getMapper().
        	map(serverName, decodedURI, mappingData);
        
        request.setContext((Context) request.getMappingData().context);
    	
    	
        // Had to do this after the context was set.
        // Unfortunately parseSessionId is still necessary as it 
        // affects the final URL. Safe as session cookies still 
        // haven't been parsed.
    	
        request.setWrapper(
        		(Wrapper) request.getMappingData().wrapper);
        	
    	
    	return true;
    	
    }
    
	
}
