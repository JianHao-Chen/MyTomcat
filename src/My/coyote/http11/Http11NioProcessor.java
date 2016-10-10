package My.coyote.http11;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.SelectionKey;
import java.util.regex.Pattern;

import My.coyote.ActionCode;
import My.coyote.ActionHook;
import My.coyote.Adapter;
import My.coyote.Request;
import My.coyote.RequestInfo;
import My.coyote.Response;
import My.coyote.http11.filters.BufferedInputFilter;
import My.coyote.http11.filters.ChunkedInputFilter;
import My.coyote.http11.filters.IdentityInputFilter;
import My.coyote.http11.filters.VoidInputFilter;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.buf.Ascii;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.HexUtils;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.FastHttpDateFormat;
import My.tomcat.util.http.MimeHeaders;
import My.tomcat.util.net.NioChannel;
import My.tomcat.util.net.NioEndpoint;
import My.tomcat.util.net.NioEndpoint.Handler.SocketState;

/**
* Processes HTTP requests.
*/

public class Http11NioProcessor implements ActionHook{

	protected static Log log = LogFactory.getLog(Http11NioProcessor.class);

	
	/*
     * Tracks how many internal filters are in the filter library so they
     * are skipped when looking for pluggable filters. 
     */
    private int pluggableFilterIndex = Integer.MAX_VALUE;
	
	// ------------------ Constructors ------------------
	
	public Http11NioProcessor(int rxBufSize, int txBufSize, int maxHttpHeaderSize, NioEndpoint endpoint) {
		
		this.endpoint = endpoint;
		
		request = new Request();
        int readTimeout = endpoint.getSoTimeout();
        inputBuffer = new InternalNioInputBuffer(request, maxHttpHeaderSize);
        request.setInputBuffer(inputBuffer);
        
        response = new Response();
        response.setHook(this);
        outputBuffer = new InternalNioOutputBuffer(response, maxHttpHeaderSize);
        response.setOutputBuffer(outputBuffer);
        request.setResponse(response);
        
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
     * HTTP/1.1 flag.
     */
    protected boolean http11 = true;


    /**
     * HTTP/0.9 flag.
     */
    protected boolean http09 = false;
    
    
    /**
     * Content delimitator for the request (if false, the connection will
     * be closed at the end of the request).
     */
    protected boolean contentDelimitation = true;
    
    
    /**
     * Is there an expectation ?
     */
    protected boolean expectation = false;
    
    
    /**
     * List of restricted user agents.
     */
    protected Pattern[] restrictedUserAgents = null;
    
    
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
     * Host name (used to avoid useless B2C conversion on the host name).
     */
    protected char[] hostNameC = new char[0];
    
    
    /**
     * Associated endpoint.
     */
    protected NioEndpoint endpoint;

    
    
    /**
     * Allow a customized the server header for the tin-foil hat folks.
     */
    protected String server = null;
    
    
    
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
     * Parse host.
     */
    public void parseHost(MessageBytes valueMB) {
    	
    	if (valueMB == null || valueMB.isNull()) {
    		//;
    	}
    	
    	ByteChunk valueBC = valueMB.getByteChunk();
    	
    	byte[] valueB = valueBC.getBytes();
    	int valueL = valueBC.getLength();
        int valueS = valueBC.getStart();
        int colonPos = -1;
        
        if (hostNameC.length < valueL) {
            hostNameC = new char[valueL];
        }
        
        for (int i = 0; i < valueL; i++) {
        	char b = (char) valueB[i + valueS];
        	hostNameC[i] = b;
        	if (b == ':'){
        		colonPos = i;
                break;
        	}
        		
        }
        
        
        if (colonPos < 0) {
        	//;
        }
        else {
        	request.serverName().setChars(hostNameC, 0, colonPos);
        }
        
        
        int port = 0;
        int mult = 1;
        for (int i = valueL - 1; i > colonPos; i--) {
        	
        	 int charValue = HexUtils.DEC[(int) valueB[i + valueS]];
        	 if (charValue == -1) {
        		// Invalid character
                 error = true;
                 // 400 - Bad request
                /* response.setStatus(400);
                 adapter.log(request, response, 0);*/
                 break;
        	 }
        	 port = port + (charValue * mult);
             mult = 10 * mult;
        }
        request.setServerPort(port);
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
    	
    	outputBuffer.setSocket(socket);
    	outputBuffer.setSelectorPool(endpoint.getSelectorPool());
    	
    	
    	// Error flag
        error = false;
        keepAlive = true;
        
        boolean openSocket = false;
        boolean recycle = true;
        boolean keptAlive = false;
        
        while (!error && keepAlive ) {
        	
        	// Parsing the request header
        	try {
        		
        		if(!inputBuffer.parseRequestLine(keptAlive)){
        			//no data available yet, since we might have read part
                    //of the request line, we can't recycle the processor
        			
        			
        			openSocket = true;
                    recycle = false;
                    break;
                    
        		}
        		
        		keptAlive = true;
        		
        		if ( !inputBuffer.parseHeaders() ) {
        			//we've read part of the request, don't recycle it
                    //instead associate it with the socket
        			
        			
        			openSocket = true;
                    recycle = false;
                    break;
                    
        		}
        		
        		request.setStartTime(System.currentTimeMillis());
        		
        	}catch (IOException e) {
        		error = true;
                break;
        	}
        	catch (Throwable t) {
        		// 400 - Bad Request
        		
        		/*
        		 * server don't understand the request
                response.setStatus(400);
                adapter.log(request, response, 0);
                error = true;
                */
        	}
        	
        	if (!error) {
        		// Setting up filters, and parse some request headers
        		rp.setStage(My.coyote.Constants.STAGE_PREPARE);
        	
        		try {
        			prepareRequest();
        		}
        		catch (Throwable t) {
        			
        			 // 400 - Internal Server Error
        			/*response.setStatus(400);
                    adapter.log(request, response, 0);*/
                    
                    error = true;
        		}
        	}
        	
        	
        	
        	
        	// Process the request in the adapter
        	if (!error) {
        		try {
        			rp.setStage(My.coyote.Constants.STAGE_SERVICE);
        			adapter.service(request, response);
        			
        		} catch (InterruptedIOException e) {
        			
        		}
        		catch (Throwable t) {
        			
        		}
        	}
        	
        	
        	endRequest();
        	
        	
            // Next request
            inputBuffer.nextRequest();
            outputBuffer.nextRequest();
            
        	
        	
        	// If there was an error, make sure the request is counted as
            // and error, and update the statistics counter
            if (error) {
                response.setStatus(500);
            }

            
            rp.setStage(My.coyote.Constants.STAGE_KEEPALIVE);
        	
        }
        
        rp.setStage(My.coyote.Constants.STAGE_ENDED);
        
        if ( recycle ) 
        		recycle();
        
        return (openSocket) ? 
        		(recycle?SocketState.OPEN:SocketState.LONG) : SocketState.CLOSED;
    }
    
    
    public void endRequest() {
    	
    	// Finish the handling of the request
    	try {
            inputBuffer.endRequest();
        }
    	 catch (IOException e) {
             error = true;
         } catch (Throwable t) {
        	 
        	 response.setStatus(500);
             adapter.log(request, response, 0);
             error = true;
         }
         
         
         
         try {
             outputBuffer.endRequest();
         } catch (IOException e) {
             error = true;
         } catch (Throwable t) {
             log.error("http11processor.response.finish");
             error = true;
         }
    	
    }
    
    
    
    public void recycle() {
        /*inputBuffer.recycle();
        outputBuffer.recycle();
        this.socket = null;
        this.cometClose = false;
        this.comet = false;
        remoteAddr = null;
        remoteHost = null;
        localAddr = null;
        localName = null;
        remotePort = -1;
        localPort = -1;*/
    }
    
    
	// ------------------ Protected Methods ------------------
    /**
     * After reading the request headers, we have to setup the request filters.
     */
    protected void prepareRequest() {
    	http11 = true;
        http09 = false;
        contentDelimitation = false;
        
        MessageBytes protocolMB = request.protocol();
        if (protocolMB.equals(Constants.HTTP_11)) {
            http11 = true;
            protocolMB.setString(Constants.HTTP_11);
        }
        else if (protocolMB.equals(Constants.HTTP_10)) {
            http11 = false;
            keepAlive = false;
            protocolMB.setString(Constants.HTTP_10);
        } else if (protocolMB.equals("")) {
            // HTTP/0.9
            http09 = true;
            http11 = false;
            keepAlive = false;
        } else {
        	// Unsupported protocol
            http11 = false;
            error = true;
            // Send 505; Unsupported HTTP version
            /*
            response.setStatus(505);
            adapter.log(request, response, 0);
            */
        }
        
        
        
        MessageBytes methodMB = request.method();
        if (methodMB.equals(Constants.GET)) {
            methodMB.setString(Constants.GET);
        } else if (methodMB.equals(Constants.POST)) {
            methodMB.setString(Constants.POST);
        }
        
        
        
        MimeHeaders headers = request.getMimeHeaders();
        // Check connection header
        MessageBytes connectionValueMB = headers.getValue("connection");
        if (connectionValueMB != null) {
        	 ByteChunk connectionValueBC = connectionValueMB.getByteChunk();
             if (findBytes(connectionValueBC, Constants.CLOSE_BYTES) != -1) {
                 keepAlive = false;
             } else if (findBytes(connectionValueBC,
                                  Constants.KEEPALIVE_BYTES) != -1) {
                 keepAlive = true;
             }
        }
        
        
        MessageBytes expectMB = null;
        if (http11)
            expectMB = headers.getValue("expect");
        if ((expectMB != null)
            && (expectMB.indexOfIgnoreCase("100-continue", 0) != -1)) {
            /*inputBuffer.setSwallowInput(false);
            expectation = true;*/
        }
        
        
        // Check user-agent header
        if ((restrictedUserAgents != null) && ((http11) || (keepAlive))) {
        	//;
        }
        
        // Check for a full URI (including protocol://host:port/)
        ByteChunk uriBC = request.requestURI().getByteChunk();
        if (uriBC.startsWithIgnoreCase("http", 0)) {
        	
        	int pos = uriBC.indexOf("://", 0, 3, 4);
            int uriBCStart = uriBC.getStart();
            int slashPos = -1;
            if (pos != -1) {
                byte[] uriB = uriBC.getBytes();
                slashPos = uriBC.indexOf('/', pos + 3);
                if (slashPos == -1) {
                    slashPos = uriBC.getLength();
                    // Set URI as "/"
                    request.requestURI().setBytes
                        (uriB, uriBCStart + pos + 1, 1);
                } else {
                    request.requestURI().setBytes
                        (uriB, uriBCStart + slashPos,
                         uriBC.getLength() - slashPos);
                }
                MessageBytes hostMB = headers.setValue("host");
                hostMB.setBytes(uriB, uriBCStart + pos + 3,
                                slashPos - pos - 3);
            }
        }
        
        
        // Input filter setup
        InputFilter[] inputFilters = inputBuffer.getFilters();
        
        
        // Parse transfer-encoding header
        MessageBytes transferEncodingValueMB = null;
        if (http11)
            transferEncodingValueMB = headers.getValue("transfer-encoding");
        if (transferEncodingValueMB != null) {
        	//;
        }
        
        
        
        // Parse content-length header
        long contentLength = request.getContentLengthLong();
        if (contentLength >= 0 && !contentDelimitation) {
        	//;
        }
        
        
        MessageBytes valueMB = headers.getValue("host");
        
        // Check host header
        if (http11 && (valueMB == null)) {
        	error = true;
            // 400 - Bad request
            /*response.setStatus(400);
            adapter.log(request, response, 0);*/
        }
        
        parseHost(valueMB);
        
        if (!contentDelimitation) {
        	// If there's no content length 
        	// assume the client is not broken and 
        	// didn't send a body
        	inputBuffer.addActiveFilter
            	(inputFilters[Constants.VOID_FILTER]);
        	contentDelimitation = true;
        }
        
        
        /*
        // Advertise sendfile support through a request attribute
        if (endpoint.getUseSendfile()) 
            request.setAttribute("org.apache.tomcat.sendfile.support", Boolean.TRUE);
        // Advertise comet support through a request attribute
        request.setAttribute("org.apache.tomcat.comet.support", Boolean.TRUE);
        // Advertise comet timeout support
        request.setAttribute("org.apache.tomcat.comet.timeout.support", Boolean.TRUE);
        */
    }
    
    
    
    /**
     * Initialize standard input and output filters.
     */
    protected void initializeFilters() {
    	
    	// Create and add the identity filters.
    	inputBuffer.addFilter(new IdentityInputFilter());
    	
    	// Create and add the chunked filters.
        inputBuffer.addFilter(new ChunkedInputFilter());
        
        // Create and add the void filters.
        inputBuffer.addFilter(new VoidInputFilter());
        
        // Create and add buffered input filter
        inputBuffer.addFilter(new BufferedInputFilter());
        
        pluggableFilterIndex = inputBuffer.filterLibrary.length;
    }
    
    
    
    
    /**
     * Specialized utility method: find a sequence of lower case bytes inside
     * a ByteChunk.
     */
    protected int findBytes(ByteChunk bc, byte[] b) {

        byte first = b[0];
        byte[] buff = bc.getBuffer();
        int start = bc.getStart();
        int end = bc.getEnd();

    // Look for first char
    int srcEnd = b.length;

    for (int i = start; i <= (end - srcEnd); i++) {
        if (Ascii.toLower(buff[i]) != first) continue;
        // found first char, now look for a match
            int myPos = i+1;
        for (int srcPos = 1; srcPos < srcEnd; ) {
                if (Ascii.toLower(buff[myPos++]) != b[srcPos++])
            break;
                if (srcPos == srcEnd) return i - start; // found it
        }
    }
    return -1;

    }
    
    
    
	// ------------- ActionHook Methods ------------- 
    /**
     * Send an action to the connector.
     */
    public void action(ActionCode actionCode, Object param) {
    	
    	if (actionCode == ActionCode.ACTION_COMMIT) {
    		
    		// Commit current response
    		if (response.isCommitted())
                return;
    		
    		// Validate and write response headers
    		try {
    			prepareResponse();
    			outputBuffer.commit();
    		}catch (IOException e) {
    			
    		}
    	}
    	else if (actionCode == ActionCode.ACTION_ACK) {
    		
    		// Acknowlege request

            // Send a 100 status back if it makes sense (response not committed
            // yet, and client specified an expectation for 100-continue)
    		if ((response.isCommitted()) || !expectation)
    			return;
    		
    		
    		
    	}
    	else if (actionCode == ActionCode.ACTION_CLOSE) {
    		// Close
    		
    		// End the processing of the current request, and stop any further
            // transactions with the client
    		
    		
    		
    		// this is for checking if is a comet connection
    		
    		//.....
    		
    		
    		
    		
    		 try {
    			 outputBuffer.endRequest();
    		 }catch (IOException e) {
    			// Set error flag
                 error = true;
    		 }
    		
    	}
    	
    	
    }
    
    
    
    
    /**
     * When committing the response, we have to validate the set of headers, as
     * well as setup the response filters.
     */
    protected void prepareResponse() throws IOException {
    	
    	boolean entityBody = true;
    	contentDelimitation = false;
    	
    	OutputFilter[] outputFilters = outputBuffer.getFilters();
    	
    	int statusCode = response.getStatus();
    	
    	if ((statusCode == 204) || (statusCode == 205)
                || (statusCode == 304)) {
    		
    		//...
    	}
    	
    	MessageBytes methodMB = request.method();
    	if (methodMB.equals("HEAD")) {
    		//...
    	}
    	
    	
    	MimeHeaders headers = response.getMimeHeaders();
    	if (!entityBody) {
    		// means don't have response body
    		response.setContentLength(-1);
    	}
    	else {
    		String contentType = response.getContentType();
    		if (contentType != null) {
                headers.setValue("Content-Type").setString(contentType);
            }
    	}
    	
    	
    	long contentLength = response.getContentLengthLong();
    	if (contentLength != -1) {
    		
    		headers.setValue("Content-Length").setLong(contentLength);
    		contentDelimitation = true;
    	}
    	else {
    		// ...
    	}
    	
    	
    	
    	// Add date header
        headers.setValue("Date").setString(FastHttpDateFormat.getCurrentDate());
    	
        if ((entityBody) && (!contentDelimitation)) {
            // Mark as close the connection after the request, and add the
            // connection: close header
            keepAlive = false;
        }
        
        // If we know that the request is bad this early, add the
        // Connection: close header.
        keepAlive = keepAlive && !statusDropsConnection(statusCode);
        
        if (!keepAlive) {
            headers.addValue(Constants.CONNECTION).setString(Constants.CLOSE);
        } else if (!http11 && !error) {
            headers.addValue(Constants.CONNECTION).setString(Constants.KEEPALIVE);
        }
        
        
        // Build the response header
        outputBuffer.sendStatus();
        
        
        // Add server header
        if (server != null) {
        	
        }else if (headers.getValue("Server") == null) {
        	// If app didn't set the header, use the default
            outputBuffer.write(Constants.SERVER_BYTES);
        }
        
        
        int size = headers.size();
        for (int i = 0; i < size; i++) {
        	outputBuffer.sendHeader(
        			headers.getName(i), headers.getValue(i));
        }
        
        outputBuffer.endHeaders();
    }
    
    
    /**
     * Determine if we must drop the connection because of the HTTP status
     * code.  Use the same list of codes as Apache/httpd.
     */
    protected boolean statusDropsConnection(int status) {
        return status == 400 /* SC_BAD_REQUEST */ ||
               status == 408 /* SC_REQUEST_TIMEOUT */ ||
               status == 411 /* SC_LENGTH_REQUIRED */ ||
               status == 413 /* SC_REQUEST_ENTITY_TOO_LARGE */ ||
               status == 414 /* SC_REQUEST_URI_TOO_LARGE */ ||
               status == 500 /* SC_INTERNAL_SERVER_ERROR */ ||
               status == 503 /* SC_SERVICE_UNAVAILABLE */ ||
               status == 501 /* SC_NOT_IMPLEMENTED */;
    }
	
}
