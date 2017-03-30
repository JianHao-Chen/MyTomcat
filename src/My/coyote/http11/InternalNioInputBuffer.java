package My.coyote.http11;

import java.io.EOFException;
import java.io.IOException;

import My.coyote.InputBuffer;
import My.coyote.Request;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.MimeHeaders;
import My.tomcat.util.net.NioChannel;
import My.tomcat.util.net.NioSelectorPool;

/**
 * Implementation of InputBuffer which provides HTTP request header parsing as
 * well as transfer decoding.
 */

public class InternalNioInputBuffer implements InputBuffer{

	
	
	// -------------------------------------------------------------- Constants

    enum HeaderParseStatus {DONE, HAVE_MORE_HEADERS, NEED_MORE_DATA}
    enum HeaderParsePosition {HEADER_START, HEADER_NAME, HEADER_VALUE, HEADER_MULTI_LINE}
	
	// ------------------ Constructors -----------------
	public InternalNioInputBuffer(Request request, int headerBufferSize) {
		
		this.request = request;
		headers = request.getMimeHeaders();
		
		this.headerBufferSize = headerBufferSize;
		
		inputStreamInputBuffer = new SocketInputBuffer();
		
		
		filterLibrary = new InputFilter[0];
        activeFilters = new InputFilter[0];
        lastActiveFilter = -1;
		
		
		parsingHeader = true;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerParsePos = HeaderParsePosition.HEADER_START;
		 
	}
	
	
	
	
	 /**
     * Additional size we allocate to the buffer to be more effective when
     * skipping empty lines that may precede the request.
     */
    private static final int skipBlankLinesSize = 1024;
	
	
	// ------------------  Instance Variables ------------------  
    
    /**
     * How many bytes in the buffer are occupied by skipped blank lines that
     * precede the request.
     */
    private int skipBlankLinesBytes;
	
    /**
     * Maximum allowed size of the HTTP request line plus headers.
     */
    private final int headerBufferSize;
    
    
    /**
     * Associated Coyote request.
     */
    protected Request request;
    
    
    /**
     * Headers of the associated request.
     */
    protected MimeHeaders headers;
    
    
    
	 /**
     * Underlying socket.
     */
    protected NioChannel socket;
    
    /**
     * Selector pool, for blocking reads and blocking writes
     */
    protected NioSelectorPool pool;
    
    
    /**
     * Underlying input buffer.
     */
    protected InputBuffer inputStreamInputBuffer;
    
    /**
     * Filter library.
     * Note: Filter[0] is always the "chunked" filter.
     */
    protected InputFilter[] filterLibrary;


    /**
     * Active filters (in order).
     */
    protected InputFilter[] activeFilters;
    
    /**
     * Add an input filter to the filter library.
     */
    public void addFilter(InputFilter filter) {

        InputFilter[] newFilterLibrary = 
            new InputFilter[filterLibrary.length + 1];
        for (int i = 0; i < filterLibrary.length; i++) {
            newFilterLibrary[i] = filterLibrary[i];
        }
        newFilterLibrary[filterLibrary.length] = filter;
        filterLibrary = newFilterLibrary;

        activeFilters = new InputFilter[filterLibrary.length];

    }

    
    /**
     * Get filters.
     */
    public InputFilter[] getFilters() {

        return filterLibrary;

    }
    
    
    /**
     * Clear filters.
     */
    public void clearFilters() {

        filterLibrary = new InputFilter[0];
        lastActiveFilter = -1;

    }
    
    
    /**
     * Add an input filter to the filter library.
     */
    public void addActiveFilter(InputFilter filter) {

        if (lastActiveFilter == -1) {
            filter.setBuffer(inputStreamInputBuffer);
        } else {
            for (int i = 0; i <= lastActiveFilter; i++) {
                if (activeFilters[i] == filter)
                    return;
            }
            filter.setBuffer(activeFilters[lastActiveFilter]);
        }

        activeFilters[++lastActiveFilter] = filter;

        filter.setRequest(request);

    }
    


    /**
     * Index of the last active filter.
     */
    protected int lastActiveFilter;
    
    public void setSelectorPool(NioSelectorPool pool) { 
        this.pool = pool;
    }
    
    public NioSelectorPool getSelectorPool() {
        return pool;
    }
    
    
    /**
     * Known size of the NioChannel read buffer.
     */
    private int socketReadBufferSize;
    
    
    /**
     * Position in the buffer.
     */
    protected int pos;
    
    /**
     * Last valid byte.
     */
    protected int lastValid;
    
    
    /**
     * Pos of the end of the header in the buffer, which is also the
     * start of the body.
     */
    protected int end;
    
    
    /**
     * Pointer to the current read buffer.
     */
    protected byte[] buf;
    
    
    /**
     * Parsing state - used for non blocking parsing so that
     * when more data arrives, we can pick up where we left off.
     */
    protected boolean parsingHeader;
    protected boolean parsingRequestLine;
    protected int parsingRequestLinePhase = 0;
    protected boolean parsingRequestLineEol = false;
    protected int parsingRequestLineStart = 0;
    protected int parsingRequestLineQPos = -1;
    protected HeaderParsePosition headerParsePos;
    
    
	
	// ----------------- Properties -----------------
	
	 /**
     * Set the underlying socket.
     */
    public void setSocket(NioChannel socket) {
        this.socket = socket;
        socketReadBufferSize = socket.getBufHandler().getReadBuffer().capacity();
        int bufLength = skipBlankLinesSize + headerBufferSize
                + socketReadBufferSize;
        if (buf == null || buf.length < bufLength) {
            buf = new byte[bufLength];
        }
    }
    
    
    
    
	// ------------------------------------------------------ Protected Methods

    /**
     * Fill the internal buffer using data from the undelying input stream.
     * 
     * @return false if at end of stream
     */
    protected boolean fill(boolean timeout, boolean block)
        throws IOException, EOFException {
    	
    	boolean read = false;

        if (parsingHeader) {

            if (lastValid == buf.length) {
                throw new IllegalArgumentException
                    ("iib.requestheadertoolarge.error");
            }

            // Do a simple read with a short timeout
            read = readSocket(timeout,block)>0;
        } else {
            lastValid = pos = end;
            // Do a simple read with a short timeout
            read = readSocket(timeout, block)>0;
        }
        return read;
    	
    }
    
    
    
    
	// ------------------ InputBuffer Methods ------------------
    /**
     * Read some bytes.
     */
    public int doRead(ByteChunk chunk, Request req) 
        throws IOException {

    	return inputStreamInputBuffer.doRead(chunk, req);
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    
	// ------------- InputStreamInputBuffer Inner Class -------------
    
    /**
     * This class is an input buffer which will read its data from an input
     * stream.
     */
    protected class SocketInputBuffer 
    	implements InputBuffer {
    	
    	/**
         * Read bytes into the specified chunk.
         */
    	public int doRead(ByteChunk chunk, Request req ) 
    		throws IOException {
    		
    		if (pos >= lastValid) {
                if (!fill(true,true)) //read body, must be blocking, as the thread is inside the app
                    return -1;
            }

            int length = lastValid - pos;
            chunk.setBytes(buf, pos, length);
            pos = lastValid;

            return (length);
    	}
    	
    }
    
    
	// ---------------------- Public Methods ----------------------
    
    /**
     * Recycle the input buffer. This should be called when closing the 
     * connection.
     */
    public void recycle() {
    	
    	// Recycle Request object
        request.recycle();
        
        socket = null;
        lastValid = 0;
        pos = 0;
        
        parsingHeader = true;
        headerParsePos = HeaderParsePosition.HEADER_START;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerData.recycle();
    }
    
    
    /**
     * End processing of current HTTP request.
     * Note: All bytes of the current request should have been already 
     * consumed. This method only resets all the pointers so that we are ready
     * to parse the next HTTP request.
     */
    public void nextRequest() {
    	
    	// Recycle Request object
        request.recycle();
        
        
        // Reset pointers
        lastValid = lastValid - pos;
        pos = 0;
        
        parsingHeader = true;
        headerParsePos = HeaderParsePosition.HEADER_START;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerData.recycle();
    	
    }
    
    
    /**
     * End request (consumes leftover bytes).
     * 
     * @throws IOException an undelying I/O error occured
     */
    public void endRequest()
        throws IOException {
    	
    	
    }
    
    
    
    /**
     * Read the request line. This function is meant to be used during the 
     * HTTP request header parsing. Do NOT attempt to read the request body 
     * using it.
     *
     * @throws IOException If an exception occurs during the underlying socket
     * read operations, or if the given buffer is not big enough to accomodate
     * the whole line.
     * @return true if data is properly fed; false if no data is available 
     * immediately and thread should be freed
     */
    public boolean parseRequestLine(boolean useAvailableDataOnly)
        throws IOException {
    	
    	//check state
    	if ( !parsingRequestLine ) return true;
    	
    	if ( parsingRequestLinePhase == 0 ) {
    		byte chr = 0;
    		do{
    			// Read new bytes if needed
    			if (pos >= lastValid) {
    				
    				if (useAvailableDataOnly) {
                        return false;
                    }
    				
    				// Ignore bytes that were read
    				pos = lastValid = 0;
    				
    				// Do a simple read with a short timeout
    				if ( readSocket(true, false)==0 ) 
    					return false;
    			}
    			chr = buf[pos++];
    		}while((chr == Constants.CR) || (chr == Constants.LF));
    		
    		pos--;
    		
    		// handle the situation that has empty lines in header.
    		if (pos >= skipBlankLinesSize) {
    			// Move data, to have enough space for further reading
                // of headers and body
    			System.arraycopy(buf, pos, buf, 0, lastValid - pos);
    			lastValid -= pos;
                pos = 0;
    		}
    		
    		skipBlankLinesBytes = pos;
    		parsingRequestLineStart = pos;
            parsingRequestLinePhase = 2;
            
    	}	// parsingRequestLinePhase == 0
    	
    	
    	if ( parsingRequestLinePhase == 2 ) {
    		
    		// Reading the method name
            // Method name is always US-ASCII
    		boolean space = false;
    		
    		while (!space) {
    			 
    			// Read new bytes if needed
    			if (pos >= lastValid) {
    				//implement latter.
    			}
    			if (buf[pos] == Constants.CR || buf[pos] == Constants.LF) {
    				//implement latter.
    			}
    			if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
    				space = true;
    				request.method().setBytes(buf, parsingRequestLineStart, pos - parsingRequestLineStart);
    			}
    			
    			pos++;
    		}
    		
    		parsingRequestLinePhase = 3;
    	}
    	
    	if ( parsingRequestLinePhase == 3 ) {
    		// handle multiple SP , HT
    		/*
    		 * 	SP	= <US-ASCII SP, space （32）>
       			HT	= <US-ASCII HT, horizontal-tab（9）>
    		 */
    		parsingRequestLineStart = pos;
            parsingRequestLinePhase = 4;
    	}
    	
    	if (parsingRequestLinePhase == 4) {
    		
    		int end = 0;
    		
    		// Reading the URI
    		boolean space = false;
            while (!space) {
            	// Read new bytes if needed
                if (pos >= lastValid) {
                	
                }
                if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                	space = true;
                    end = pos;
                }
                else if ((buf[pos] == Constants.CR) 
                        || (buf[pos] == Constants.LF)) {
                	// HTTP/0.9 style request
                	;
                }
                else if ((buf[pos] == Constants.QUESTION) 
                        && (parsingRequestLineQPos == -1)) {
                    // 处理   http://.../SessionExample;jsessionid=111?dataname=&datavalue=
                    parsingRequestLineQPos = pos;
                }
                
                pos++;
            }
            
            request.unparsedURI().setBytes(buf, parsingRequestLineStart,  end - parsingRequestLineStart);
            
            if (parsingRequestLineQPos >= 0) {
                
                request.queryString().setBytes(buf, parsingRequestLineQPos + 1, 
                        end - parsingRequestLineQPos - 1);
                
                request.requestURI().setBytes(buf, parsingRequestLineStart, parsingRequestLineQPos - parsingRequestLineStart);
            }
            else{
                request.requestURI().setBytes(buf, parsingRequestLineStart, end - parsingRequestLineStart);
            }
            
    		
            parsingRequestLinePhase = 5;
    	}

    	if ( parsingRequestLinePhase == 5 ) {
    		
    		boolean space = true;
            while (space) {
            	// Read new bytes if needed
                if (pos >= lastValid) {
                	
                }
                
                if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                    pos++;
                } else {
                    space = false;
                }    
            }
            
            parsingRequestLineStart = pos;
            parsingRequestLinePhase = 6;
    	}
    	
    	if (parsingRequestLinePhase == 6) { 
    		
    		// Mark the current buffer position
    		end = 0;
    		
    		// Reading the protocol
            // Protocol is always US-ASCII
    		
    		while (!parsingRequestLineEol) {
    			// Read new bytes if needed
                if (pos >= lastValid) {
                	
                }
                
                if (buf[pos] == Constants.CR) {
                    end = pos;
                } else if (buf[pos] == Constants.LF) {
                    if (end == 0)
                        end = pos;
                    parsingRequestLineEol = true;
                }
                pos++;
    		}
    		
    		if ( (end - parsingRequestLineStart) > 0) {
                request.protocol().setBytes(buf, parsingRequestLineStart, end - parsingRequestLineStart);
            } else {
                request.protocol().setString("");
            }
    		
    		parsingRequestLine = false;
            parsingRequestLinePhase = 0;
            parsingRequestLineEol = false;
            parsingRequestLineStart = 0;
            return true;
    	}
    	
    	throw new IllegalStateException("Invalid request line parse phase:"+parsingRequestLinePhase);
    	
    }
    
    
    
    /**
     * Parse the HTTP headers.
     */
    public boolean parseHeaders()
        throws IOException {
    	
    	 HeaderParseStatus status = HeaderParseStatus.HAVE_MORE_HEADERS;
    	 
    	 do {
    		 status = parseHeader();
    	 }while ( status == HeaderParseStatus.HAVE_MORE_HEADERS );
    	 
    	 if (status == HeaderParseStatus.DONE) {
    		 parsingHeader = false;
             end = pos;
             // Checking that
             // (1) Headers plus request line size does not exceed its limit
             // (2) There are enough bytes to avoid expanding the buffer when
             // reading body
             // Technically, (2) is technical limitation, (1) is logical
             // limitation to enforce the meaning of headerBufferSize
             // From the way how buf is allocated and how blank lines are being
             // read, it should be enough to check (1) only.
             if (end - skipBlankLinesBytes > headerBufferSize
                     || buf.length - end < socketReadBufferSize) {
                 throw new IllegalArgumentException(
                         "Request header too large");
             }
             return true;
    	 }
    	 else 
             return false;

    }
    
    
    /**
     * Parse an HTTP header.
     * 
     * @return false after reading a blank line (which indicates that the
     * HTTP header parsing is done
     */
    public HeaderParseStatus parseHeader()
        throws IOException {
    	
    	//
        // Check for blank line
        //
    	byte chr = 0;
    	
    	while (headerParsePos == HeaderParsePosition.HEADER_START) {
    		
    		// Read new bytes if needed
    		if (pos >= lastValid) {
    			
    		}
    		
    		chr = buf[pos];
    		
    		 if ((chr == Constants.CR) || (chr == Constants.LF)) {
                 if (chr == Constants.LF) {
                     pos++;
                     return HeaderParseStatus.DONE;
                 }
             } else {
                 break;
             }
    		 
    		 pos++;
    	}
    	
    	if ( headerParsePos == HeaderParsePosition.HEADER_START ) {
    		// Mark the current buffer position
            headerData.start = pos;
            headerParsePos = HeaderParsePosition.HEADER_NAME;
    	}
    	
    	//
        // Reading the header name
        // Header name is always US-ASCII
        //
    	
    	while (headerParsePos == HeaderParsePosition.HEADER_NAME) {
    		
    		// Read new bytes if needed
            if (pos >= lastValid) {
            	
            }
            
            if (buf[pos] == Constants.COLON) {
                headerParsePos = HeaderParsePosition.HEADER_VALUE;
                headerData.headerValue = headers.addValue(buf, headerData.start, pos - headerData.start);
            }
            chr = buf[pos];
            if ((chr >= Constants.A) && (chr <= Constants.Z)) {
                buf[pos] = (byte) (chr - Constants.LC_OFFSET);
            }

            pos++;
            if ( headerParsePos == HeaderParsePosition.HEADER_VALUE ) { 
                // Mark the current buffer position
                headerData.start = pos;
                headerData.realPos = pos;
            }
    	}
    	
    	
    	//
        // Reading the header value (which can be spanned over multiple lines)
        //

        boolean eol = false;
        while (headerParsePos == HeaderParsePosition.HEADER_VALUE ||
                headerParsePos == HeaderParsePosition.HEADER_MULTI_LINE) {
        	
        	if ( headerParsePos == HeaderParsePosition.HEADER_VALUE ) {
        	
        		boolean space = true;

                // Skipping spaces
                while (space) {
                	
                	 // Read new bytes if needed
                    if (pos >= lastValid) {
                    	
                    }
                    
                    if ((buf[pos] == Constants.SP) || (buf[pos] == Constants.HT)) {
                        pos++;
                    } else {
                        space = false;
                    }
                }
                
                headerData.lastSignificantChar = headerData.realPos;

                // Reading bytes until the end of the line
                while (!eol) {
                	
                	// Read new bytes if needed
                    if (pos >= lastValid) {
                    	
                    }
                    
                    
                    if (buf[pos] == Constants.CR) {
                    } else if (buf[pos] == Constants.LF) {
                        eol = true;
                    } else if (buf[pos] == Constants.SP) {
                        buf[headerData.realPos] = buf[pos];
                        headerData.realPos++;
                    } else {
                        buf[headerData.realPos] = buf[pos];
                        headerData.realPos++;
                        headerData.lastSignificantChar = headerData.realPos;
                    }

                    pos++;
                }
                
                
                headerData.realPos = headerData.lastSignificantChar;

                // Checking the first character of the new line. If the character
                // is a LWS, then it's a multiline header
                headerParsePos = HeaderParsePosition.HEADER_MULTI_LINE;
                
                
        	}
        	
        	// Read new bytes if needed
            if (pos >= lastValid) {
            	
            }
            
            chr = buf[pos];
            if ( headerParsePos == HeaderParsePosition.HEADER_MULTI_LINE ) {
            	
            	 if ( (chr != Constants.SP) && (chr != Constants.HT)) {
                     headerParsePos = HeaderParsePosition.HEADER_START;
                 } else {
                     eol = false;
                     // Copying one extra space in the buffer (since there must
                     // be at least one space inserted between the lines)
                     buf[headerData.realPos] = chr;
                     headerData.realPos++;
                     headerParsePos = HeaderParsePosition.HEADER_VALUE;
                 }
            }
        }
        
        // Set the header value
        headerData.headerValue.setBytes(buf, headerData.start, headerData.realPos - headerData.start);
        headerData.recycle();
        return HeaderParseStatus.HAVE_MORE_HEADERS;
    	
    }
    
    
    protected HeaderParseData headerData = new HeaderParseData();
    public static class HeaderParseData {
    	int start = 0;
        int realPos = 0;
        int lastSignificantChar = 0;
        MessageBytes headerValue = null;
        public void recycle() {
            start = 0;
            realPos = 0;
            lastSignificantChar = 0;
            headerValue = null;
        }
    }
    
    
    
    
    /**
     * Perform no blocking read with a timeout if desired
     * @param timeout boolean - if we want to use the timeout data
     * @param block - true if the system should perform a blocking read, false otherwise
     * @return boolean - true if data was read, false is no data read, EOFException if EOF is reached
     * @throws IOException if a socket exception occurs
     * @throws EOFException if end of stream is reached
     */
    private int readSocket(boolean timeout, boolean block) throws IOException {
    	int nRead = 0;
    	socket.getBufHandler().getReadBuffer().clear();
    	if ( block ) {
    		//implement latter
    	}else{
    		nRead = socket.read(socket.getBufHandler().getReadBuffer());
    	}
    	
    	if(nRead > 0){
    		socket.getBufHandler().getReadBuffer().flip();
    		// insure the buf has enough space
    		expand(nRead + pos);
    		// read data from byteBuffer to buf
    		socket.getBufHandler().getReadBuffer().get(buf,pos,nRead);
    		// update lastValid byte pos in buf
    		lastValid = pos + nRead;
    		return nRead;
    	}else if(nRead == -1)
    		throw new EOFException("Reach EOF !!");
    	else 
    		return 0;
    }
    
    
    private void expand(int newsize) {
    	// implement latter.
    }


}
