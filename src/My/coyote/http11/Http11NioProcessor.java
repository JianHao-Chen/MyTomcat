package My.coyote.http11;

import java.io.IOException;

import My.coyote.Adapter;
import My.coyote.Request;
import My.coyote.RequestInfo;
import My.coyote.Response;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.net.NioChannel;
import My.tomcat.util.net.NioEndpoint;
import My.tomcat.util.net.NioEndpoint.Handler.SocketState;

/**
* Processes HTTP requests.
*/

public class Http11NioProcessor {

	protected static Log log = LogFactory.getLog(Http11NioProcessor.class);

	
	// ------------------ Constructors ------------------
	
	public Http11NioProcessor(int rxBufSize, int txBufSize, int maxHttpHeaderSize, NioEndpoint endpoint) {
		
		this.endpoint = endpoint;
		
		request = new Request();
        int readTimeout = endpoint.getSoTimeout();
        inputBuffer = new InternalNioInputBuffer(request, maxHttpHeaderSize);
        request.setInputBuffer(inputBuffer);
        
        initializeFilters();
	}
	
	
	
	// ------------------ Instance Variables ------------------
	
	/**
     * Associated adapter.
     */
    protected Adapter adapter = null;
	
    /**
     * Request object.
     */
    protected Request request = null;

    /**
     * Response object.
     */
    protected Response response = null;
	
    /**
     * Input.
     */
    protected InternalNioInputBuffer inputBuffer = null;
    
    /**
     * Output.
     */
    protected InternalNioOutputBuffer outputBuffer = null;
    
    
    /**
     * Error flag.
     */
    protected boolean error = false;
    
    
    /**
     * Keep-alive.
     */
    protected boolean keepAlive = true;
    
    
    /**
     * Maximum number of Keep-Alive requests to honor.
     */
    protected int maxKeepAliveRequests = -1;
    
    
    /**
     * Socket associated with the current connection.
     */
    protected NioChannel socket = null;
    
    
    /**
     * Socket buffering.
     */
    protected int socketBuffer = -1;
    
    
    /**
     * Remote Address associated with the current connection.
     */
    protected String remoteAddr = null;

    /**
     * Remote port to which the socket is connected
     */
    protected int remotePort = -1;

    /**
     * Remote Host associated with the current connection.
     */
    protected String remoteHost = null;
    
    
    
    /**
     * The local Host address.
     */
    protected String localAddr = null;
    
    
    /**
     * Local Host associated with the current connection.
     */
    protected String localName = null;



    /**
     * Local port to which the socket is connected
     */
    protected int localPort = -1;
    
    
    /**
     * Maximum timeout on uploads. 5 minutes as in Apache HTTPD server.
     */
    protected int timeout = 300000;
    
    
    /**
     * Associated endpoint.
     */
    protected NioEndpoint endpoint;

    
    
	// ------------- Properties -------------
    
    
    
	
	// ---------------- Connector Methods ----------------
    
    /**
     * Set the associated adapter.
     *
     * @param adapter the new adapter
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }
    
    /**
     * Get the associated adapter.
     *
     * @return the associated adapter
     */
    public Adapter getAdapter() {
        return adapter;
    }
    
    
    
    /**
     * Set the maximum number of Keep-Alive requests to honor.
     * This is to safeguard from DoS attacks.  Setting to a negative
     * value disables the check.
     */
    public void setMaxKeepAliveRequests(int mkar) {
        maxKeepAliveRequests = mkar;
    }


    /**
     * Return the number of Keep-Alive requests that we will honor.
     */
    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }
    
    /**
     * Set the upload timeout.
     */
    public void setTimeout( int timeouts ) {
        timeout = timeouts ;
    }

    /**
     * Get the upload timeout.
     */
    public int getTimeout() {
        return timeout;
    }
    
    /**
     * Set the socket buffer flag.
     */
    public void setSocketBuffer(int socketBuffer) {
        this.socketBuffer = socketBuffer;
     //   outputBuffer.setSocketBuffer(socketBuffer);
    }

    /**
     * Get the socket buffer flag.
     */
    public int getSocketBuffer() {
        return socketBuffer;
    }
    
    
    /**
     * Process pipelined HTTP requests using the specified input and output
     * streams.
     *
     * @throws IOException error during an I/O operation
     */
    public SocketState process(NioChannel socket)
    throws IOException {
    	
    	RequestInfo rp = request.getRequestProcessor();
    	rp.setStage(My.coyote.Constants.STAGE_PARSE);

    	// Setting up the socket
    	this.socket = socket;
    	inputBuffer.setSocket(socket);
    	inputBuffer.setSelectorPool(endpoint.getSelectorPool());
    	
    	// Error flag
        error = false;
        keepAlive = true;
        
    	return null;
    }
    
    
    
    /**
     * Initialize standard input and output filters.
     */
    protected void initializeFilters() {
    	
    }
	
}
