package My.coyote.http11;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;

import My.coyote.ActionCode;
import My.coyote.ActionHook;
import My.coyote.Adapter;
import My.coyote.Request;
import My.coyote.RequestInfo;
import My.coyote.Response;
import My.tomcat.util.buf.Ascii;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.HexUtils;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.FastHttpDateFormat;
import My.tomcat.util.http.MimeHeaders;
import My.tomcat.util.net.JIoEndpoint;

public class Http11Processor implements ActionHook{

	/**
     * Logger.
     */
    protected static My.juli.logging.Log log
        = My.juli.logging.LogFactory.getLog(Http11Processor.class);

    
	// --------------------- Constructor ---------------------
    
    public Http11Processor(int headerBufferSize, JIoEndpoint endpoint) {
    	
    	this.endpoint = endpoint;
    	request = new Request();
    	inputBuffer = new InternalInputBuffer(request, headerBufferSize);
    	request.setInputBuffer(inputBuffer);

        response = new Response();
        response.setHook(this);
        outputBuffer = new InternalOutputBuffer(response, headerBufferSize);
        response.setOutputBuffer(outputBuffer);
        request.setResponse(response);
        
    }
    
    
	// -------------------- Instance Variables --------------------
    
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
    protected InternalInputBuffer inputBuffer = null;


    /**
     * Output.
     */
    protected InternalOutputBuffer outputBuffer = null;
    
    /**
     * State flag.
     */
    protected boolean started = false;


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
     * Content delimitator for the request (if false, the connection will
     * be closed at the end of the request).
     */
    protected boolean contentDelimitation = true;
    
    /**
     * Maximum number of Keep-Alive requests to honor.
     */
    protected int maxKeepAliveRequests = -1;

    /**
     * The number of seconds Tomcat will wait for a subsequent request
     * before closing the connection.
     */
    protected int keepAliveTimeout = -1;
    
    /**
     * Socket associated with the current connection.
     */
    protected Socket socket;


    /**
     * Remote Address associated with the current connection.
     */
    protected String remoteAddr = null;


    /**
     * Remote Host associated with the current connection.
     */
    protected String remoteHost = null;


    /**
     * Local Host associated with the current connection.
     */
    protected String localName = null;



    /**
     * Local port to which the socket is connected
     */
    protected int localPort = -1;


    /**
     * Remote port to which the socket is connected
     */
    protected int remotePort = -1;


    /**
     * The local Host address.
     */
    protected String localAddr = null;

    
    /**
     * Flag to disable setting a different time-out on uploads.
     */
    protected boolean disableUploadTimeout = false;

    /**
     * Maximum timeout on uploads. 5 minutes as in Apache HTTPD server.
     */
    protected int timeout = 300000;
    
    /**
     * Socket buffering.
     */
    protected int socketBuffer = -1;
    
    
    /**
     * Host name (used to avoid useless B2C conversion on the host name).
     */
    protected char[] hostNameC = new char[0];
    
    /**
     * Associated endpoint.
     */
    protected JIoEndpoint endpoint;
    
    /**
     * Allow a customized the server header for the tin-foil hat folks.
     */
    protected String server = null;
    
    
	// ---------------------------- Properties ----------------------
    
	// ---------------------- Public Methods ----------------------
    
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
     * Set the Keep-Alive timeout.
     */
    public void setKeepAliveTimeout(int timeout) {
        keepAliveTimeout = timeout;
    }


    /**
     * Return the number Keep-Alive timeout.
     */
    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }
    
    /**
     * Set the socket buffer flag.
     */
    public void setSocketBuffer(int socketBuffer) {
        this.socketBuffer = socketBuffer;
        outputBuffer.setSocketBuffer(socketBuffer);
    }

    /**
     * Get the socket buffer flag.
     */
    public int getSocketBuffer() {
        return socketBuffer;
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
     * Set the server header name.
     */
    public void setServer( String server ) {
        if (server==null || server.equals("")) {
            this.server = null;
        } else {
            this.server = server;
        }
    }

    /**
     * Get the server header name.
     */
    public String getServer() {
        return server;
    }
    
    
    /**
     * Set the flag to control upload time-outs.
     */
    public void setDisableUploadTimeout(boolean isDisabled) {
        disableUploadTimeout = isDisabled;
    }

    /**
     * Get the flag that controls upload time-outs.
     */
    public boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }


    /** Get the request associated with this processor.
     *
     * @return The request
     */
    public Request getRequest() {
        return request;
    }
    
    
    /**
     * Process pipelined HTTP requests on the specified socket.
     *
     * @param socket Socket from which the HTTP requests will be read
     *               and the HTTP responses will be written.
     *  
     * @throws IOException error during an I/O operation
     */
    public void process(Socket theSocket)
        throws IOException {
    	
    	RequestInfo rp = request.getRequestProcessor();
    	rp.setStage(My.coyote.Constants.STAGE_PARSE);
    	
    	// Set the remote address
        remoteAddr = null;
        remoteHost = null;
        localAddr = null;
        localName = null;
        remotePort = -1;
        localPort = -1;
        
        // Setting up the I/O
        this.socket = theSocket;
        inputBuffer.setInputStream(socket.getInputStream());
        outputBuffer.setOutputStream(socket.getOutputStream());
        
        // Error flag
        error = false;
        keepAlive = true;

        int keepAliveLeft = maxKeepAliveRequests;
        int soTimeout = endpoint.getSoTimeout();
    	
        // When using an executor, these values may return non-positive values
        int curThreads = endpoint.getCurrentThreadsBusy();
        int maxThreads = endpoint.getMaxThreads();
        if (curThreads > 0 && maxThreads > 0) {
            // Only auto-disable keep-alive if the current thread usage % can be
            // calculated correctly
            if ((curThreads*100)/maxThreads > 75) {
                keepAliveLeft = 1;
            }
        }
        
        try {
            socket.setSoTimeout(soTimeout);
        } catch (Throwable t) {
            log.debug("http11processor.socket.timeout");
            error = true;
        }
        
        
        boolean keptAlive = false;

        while (started && !error && keepAlive && !endpoint.isPaused()) {
        	
        	// Parsing the request header
        	try {
        		if (keptAlive) {
        			if (keepAliveTimeout > 0) {
                        socket.setSoTimeout(keepAliveTimeout);
                    }
                    else if (soTimeout > 0) {
                        socket.setSoTimeout(soTimeout);
                    }
        		}
        		
        		inputBuffer.parseRequestLine();
        		
        		request.setStartTime(System.currentTimeMillis());
        		keptAlive = true;
        		
        		if (disableUploadTimeout) {
                    socket.setSoTimeout(soTimeout);
                } else {
                    socket.setSoTimeout(timeout);
                }
        		
        		inputBuffer.parseHeaders();
        	}
        	catch (IOException e) {
        		error = true;
                break;
        	}
        	catch (Throwable t) {
        		//...
        	}

        	if (!error) {
        		
        		// Setting up filters, and parse some request headers
                rp.setStage(My.coyote.Constants.STAGE_PREPARE);
                
                try {
                    prepareRequest();
                } catch (Throwable t) {
                	
                	// 400 - Internal Server Error
                    response.setStatus(400);
                    adapter.log(request, response, 0);
                    error = true;
                }
        	}
        	
        	if (maxKeepAliveRequests > 0 && --keepAliveLeft == 0)
                keepAlive = false;
        	
        	// Process the request in the adapter
            if (!error) {
            	
            	try {
            		rp.setStage(My.coyote.Constants.STAGE_SERVICE);
                    adapter.service(request, response);
                    // Handle when the response was committed before a serious
                    // error occurred.  Throwing a ServletException should both
                    // set the status to 500 and set the errorException.
                    // If we fail here, then the response is likely already
                    // committed, so we can't try and set headers.
                    if(keepAlive && !error) { // Avoid checking twice.
                        /*error = response.getErrorException() != null ||
                                statusDropsConnection(response.getStatus());*/
                    }
            	}
            	catch (InterruptedIOException e) {
            		error = true;
            	}catch (Throwable t) {
            		log.error("http11processor.request.process");
                    // 500 - Internal Server Error
                    response.setStatus(500);
                    adapter.log(request, response, 0);
                    error = true;
            	}
            }
            
            
            // Finish the handling of the request
            try {
            	rp.setStage(My.coyote.Constants.STAGE_ENDINPUT);
            	
            	inputBuffer.endRequest();
            }catch (IOException e) {
            	error = true;
            }catch (Throwable t) {
            	error = true;
            }
            
            
            try {
                rp.setStage(My.coyote.Constants.STAGE_ENDOUTPUT);
                outputBuffer.endRequest();
            }catch (IOException e) {
                error = true;
            }
            catch (Throwable t) {
            	error = true;
            }
                
            
            // If there was an error, make sure the request is counted as
            // and error, and update the statistics counter
            if (error) {
            	response.setStatus(500);
            }
            
        	
           rp.setStage(My.coyote.Constants.STAGE_KEEPALIVE);
           
           inputBuffer.nextRequest();
           
           outputBuffer.nextRequest();
        }
        
        rp.setStage(My.coyote.Constants.STAGE_ENDED);
    	
        // Recycle
        inputBuffer.recycle();
        outputBuffer.recycle();
        
        this.socket = null;
    }
    
    
    
    
	// --------------- ActionHook Methods ---------------
    /**
     * Send an action to the connector.
     *
     * @param actionCode Type of the action
     * @param param Action parameter
     */
    public void action(ActionCode actionCode, Object param) {
    	
    	if (actionCode == ActionCode.ACTION_START) {
    		started = true;
    	}
    	else if (actionCode == ActionCode.ACTION_ACK) {
    		// Acknowlege request

            // Send a 100 status back if it makes sense (response not committed
            // yet, and client specified an expectation for 100-continue)
    		
    		return;
    	}
    	else if (actionCode == ActionCode.ACTION_CLOSE) {
    		
    		// Close

            // End the processing of the current request, and stop any further
            // transactions with the client
    		try {
                outputBuffer.endRequest();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }
    	}
    	else if (actionCode == ActionCode.ACTION_COMMIT) {
    		if (response.isCommitted())
                return;
    		
    		// Validate and write response headers
            prepareResponse();
            
            try {
            	outputBuffer.commit();
            }catch (IOException e) {
            	// Set error flag
                error = true;
            }
            
    	}
    	else if (actionCode == ActionCode.ACTION_STOP) {
    		
    		started = false;
    	}
    }
    
    
    
    
	// --------------------- Connector Methods --------------------- 
    
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
    
    
	// ------------------- Protected Methods -------------------
    
    /**
     * After reading the request headers, we have to setup the request filters.
     */
    protected void prepareRequest() {
    	
    	http11 = true;
 
    	MessageBytes protocolMB = request.protocol();
        if (protocolMB.equals(Constants.HTTP_11)) {
        	http11 = true;
            protocolMB.setString(Constants.HTTP_11);
        }else{
        	// ignore other protocol
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
        
        
        // Check for a full URI (including protocol://host:port/)
        ByteChunk uriBC = request.requestURI().getByteChunk();
        if (uriBC.startsWithIgnoreCase("http", 0)) {
        	//...
        }
        
        
        MessageBytes valueMB = headers.getValue("host");
        parseHost(valueMB);
        
    }
    
    
    /**
     * When committing the response, we have to validate the set of headers, as
     * well as setup the response filters.
     */
    protected void prepareResponse() {
    	boolean entityBody = true;
    	contentDelimitation = false;
    	
    //	OutputFilter[] outputFilters = outputBuffer.getFilters();
    	
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
     * Parse host.
     */
    public void parseHost(MessageBytes valueMB) {
    	
    	if (valueMB == null || valueMB.isNull()) {
    		//...
    	}
    	
    	ByteChunk valueBC = valueMB.getByteChunk();
    	byte[] valueB = valueBC.getBytes();
        int valueL = valueBC.getLength();
        int valueS = valueBC.getStart();
        int colonPos = -1;
        if (hostNameC.length < valueL) {
            hostNameC = new char[valueL];
        }
        
        boolean ipv6 = (valueB[valueS] == '[');
        boolean bracketClosed = false;
        for (int i = 0; i < valueL; i++) {
            char b = (char) valueB[i + valueS];
            hostNameC[i] = b;
            if (b == ']') {
                bracketClosed = true;
            } else if (b == ':') {
                if (!ipv6 || bracketClosed) {
                    colonPos = i;
                    break;
                }
            }
        }
        
        if (colonPos < 0) {
        	//...
        }else {
        	
        	request.serverName().setChars(hostNameC, 0, colonPos);

            int port = 0;
            int mult = 1;
            
            for (int i = valueL - 1; i > colonPos; i--) {
            	
            	int charValue = HexUtils.DEC[(int) valueB[i + valueS]];
                if (charValue == -1) {
                    // Invalid character
                    error = true;
                    // 400 - Bad request
                    response.setStatus(400);
                    adapter.log(request, response, 0);
                    break;
                }
                port = port + (charValue * mult);
                mult = 10 * mult;
            }
            request.setServerPort(port);
        }
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
        	if (Ascii.toLower(buff[i]) != first) 
        		continue;
        	// found first char, now look for a match
            int myPos = i+1;
            for (int srcPos = 1; srcPos < srcEnd; ) {
            	if (Ascii.toLower(buff[myPos++]) != b[srcPos++])
                    break;
            	if (srcPos == srcEnd) // found it
            		return i - start; 
            }
        }
        
        return -1;
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
