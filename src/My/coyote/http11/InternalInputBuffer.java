package My.coyote.http11;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import My.coyote.InputBuffer;
import My.coyote.Request;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.MimeHeaders;

/**
 * Implementation of InputBuffer which provides HTTP request header parsing as
 * well as transfer decoding.
 */

public class InternalInputBuffer implements InputBuffer{

	
	// -------------------- Constructors --------------------
	
	/**
     * Default constructor.
     */
    public InternalInputBuffer(Request request) {
    	this(request, Constants.DEFAULT_HTTP_HEADER_BUFFER_SIZE);
    }
    
    /**
     * Alternate constructor.
     */
    public InternalInputBuffer(Request request, int headerBufferSize) {
    	
    	this.request = request;
        headers = request.getMimeHeaders();

        buf = new byte[headerBufferSize];
        inputStreamInputBuffer = new InputStreamInputBuffer();
        
        parsingHeader = true;
    }
    
    
    
	// --------------- Instance Variables ---------------
    
    /**
     * Associated Coyote request.
     */
    protected Request request;


    /**
     * Headers of the associated request.
     */
    protected MimeHeaders headers;
    
    /**
     * State.
     */
    protected boolean parsingHeader;
    
    /**
     * Pointer to the current read buffer.
     */
    protected byte[] buf;
    
    /**
     * Last valid byte.
     */
    protected int lastValid;


    /**
     * Position in the buffer.
     */
    protected int pos;


    /**
     * Pos of the end of the header in the buffer, which is also the
     * start of the body.
     */
    protected int end;


    /**
     * Underlying input stream.
     */
    protected InputStream inputStream;


    /**
     * Underlying input buffer.
     */
    protected InputBuffer inputStreamInputBuffer;
    
    
	// --------------------------- Properties -----------------------
    
    /**
     * Set the underlying socket input stream.
     */
    public void setInputStream(InputStream inputStream) {

        this.inputStream = inputStream;

    }
    /**
     * Get the underlying socket input stream.
     */
    public InputStream getInputStream() {

        return inputStream;

    }
    
    
    
	// -------------- InputBuffer Methods --------------------
    
    /**
     * Read some bytes.
     */
    public int doRead(ByteChunk chunk, Request req) 
        throws IOException {
    	
    	
    	return 0;
    }
    
    
	// ------------------ Public Methods ------------------
    
    
    
    /**
     * Read the request line. This function is meant to be used during the 
     * HTTP request header parsing. Do NOT attempt to read the request body 
     * using it.
     *
     * @throws IOException If an exception occurs during the underlying socket
     * read operations, or if the given buffer is not big enough to accomodate
     * the whole line.
     */
    public void parseRequestLine()
        throws IOException {
    	
    	int start = 0;
    	
    	//
        // Skipping blank lines
        //

        byte chr = 0;
        do {
        	// Read new bytes if needed
            if (pos >= lastValid) {
            	if (!fill())
            		throw new EOFException("iib.eof.error");
            }
            chr = buf[pos++];
            
        }while ((chr == Constants.CR) || (chr == Constants.LF));
        
        pos--;
        
        // Mark the current buffer position
        start = pos;
        
        //
        // Reading the method name
        // Method name is always US-ASCII
        //

        boolean space = false;
        
        while (!space) {
        	
        	// Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill())
                    throw new EOFException("iib.eof.error");
            }
            
            // Spec says no CR or LF in method name
            if (buf[pos] == Constants.CR || buf[pos] == Constants.LF) {
            	throw new IllegalArgumentException("iib.invalidmethod");
            }
            
            // Spec says single SP but it also says be tolerant of HT
            if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                space = true;
                request.method().setBytes(buf, start, pos - start);
            }
            
            pos++;
            
        }
        
        
        // Spec says single SP but also says be tolerant of multiple and/or HT
        while (space) {
        	
        	// Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill())
                    throw new EOFException("iib.eof.error");
            }
            
            if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                pos++;
            } else {
                space = false;
            }
            
        }
        
        // Mark the current buffer position
        start = pos;
        int end = 0;
        int questionPos = -1;

        //
        // Reading the URI
        //

        boolean eol = false;
        
        while (!space) {
        	
        	// Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill())
                    throw new EOFException("iib.eof.error");
            }
            
            // Spec says single SP but it also says be tolerant of HT
            if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                space = true;
                end = pos;
            } else if ((buf[pos] == Constants.CR) 
                       || (buf[pos] == Constants.LF)) {
                // HTTP/0.9 style request
                eol = true;
                space = true;
                end = pos;
            } else if ((buf[pos] == Constants.QUESTION) 
                       && (questionPos == -1)) {
                questionPos = pos;
            }
            
            pos++;
        }
        
        
        request.unparsedURI().setBytes(buf, start, end - start);
        if (questionPos >= 0) {
            //...
        } else {
            request.requestURI().setBytes(buf, start, end - start);
        }
        
        
        // Spec says single SP but also says be tolerant of multiple and/or HT
        while (space) {
        	
        	// Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill())
                    throw new EOFException("iib.eof.error");
            }
            
            if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                pos++;
            } else {
                space = false;
            }
        }
        
        // Mark the current buffer position
        start = pos;
        end = 0;
        
        
        //
        // Reading the protocol
        // Protocol is always US-ASCII
        //

        while (!eol) {
        	
        	// Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill())
                    throw new EOFException("iib.eof.error");
            }
            
            if (buf[pos] == Constants.CR) {
                end = pos;
            } else if (buf[pos] == Constants.LF) {
                if (end == 0)
                    end = pos;
                eol = true;
            }

            pos++;
        	
        }

        if ((end - start) > 0) {
            request.protocol().setBytes(buf, start, end - start);
        } else {
            request.protocol().setString("");
        }	
        	
    }
    
    
    /**
     * Parse the HTTP headers.
     */
    public void parseHeaders()
        throws IOException {

        while (parseHeader()) {
        }

        parsingHeader = false;
        end = pos;

    }
    
    
    /**
     * Parse an HTTP header.
     * 
     * @return false after reading a blank line (which indicates that the
     * HTTP header parsing is done
     */
    public boolean parseHeader()
        throws IOException {

        //
        // Check for blank line
        //

        byte chr = 0;
        while (true) {

            // Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill())
                    throw new EOFException("iib.eof.error");
            }

            chr = buf[pos];

            if ((chr == Constants.CR) || (chr == Constants.LF)) {
                if (chr == Constants.LF) {
                    pos++;
                    return false;
                }
            } else {
                break;
            }

            pos++;

        }

        // Mark the current buffer position
        int start = pos;

        //
        // Reading the header name
        // Header name is always US-ASCII
        //

        boolean colon = false;
        MessageBytes headerValue = null;

        while (!colon) {

            // Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill())
                    throw new EOFException("iib.eof.error");
            }

            if (buf[pos] == Constants.COLON) {
                colon = true;
                headerValue = headers.addValue(buf, start, pos - start);
            }
            chr = buf[pos];
            if ((chr >= Constants.A) && (chr <= Constants.Z)) {
                buf[pos] = (byte) (chr - Constants.LC_OFFSET);
            }

            pos++;

        }

        // Mark the current buffer position
        start = pos;
        int realPos = pos;

        //
        // Reading the header value (which can be spanned over multiple lines)
        //

        boolean eol = false;
        boolean validLine = true;

        while (validLine) {

            boolean space = true;

            // Skipping spaces
            while (space) {

                // Read new bytes if needed
                if (pos >= lastValid) {
                    if (!fill())
                        throw new EOFException("iib.eof.error");
                }

                if ((buf[pos] == Constants.SP) || (buf[pos] == Constants.HT)) {
                    pos++;
                } else {
                    space = false;
                }

            }

            int lastSignificantChar = realPos;

            // Reading bytes until the end of the line
            while (!eol) {

                // Read new bytes if needed
                if (pos >= lastValid) {
                    if (!fill())
                        throw new EOFException("iib.eof.error");
                }

                if (buf[pos] == Constants.CR) {
                } else if (buf[pos] == Constants.LF) {
                    eol = true;
                } else if (buf[pos] == Constants.SP) {
                    buf[realPos] = buf[pos];
                    realPos++;
                } else {
                    buf[realPos] = buf[pos];
                    realPos++;
                    lastSignificantChar = realPos;
                }

                pos++;

            }

            realPos = lastSignificantChar;

            // Checking the first character of the new line. If the character
            // is a LWS, then it's a multiline header

            // Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill())
                    throw new EOFException("iib.eof.error");
            }

            chr = buf[pos];
            if ((chr != Constants.SP) && (chr != Constants.HT)) {
                validLine = false;
            } else {
                eol = false;
                // Copying one extra space in the buffer (since there must
                // be at least one space inserted between the lines)
                buf[realPos] = chr;
                realPos++;
            }

        }

        // Set the header value
        headerValue.setBytes(buf, start, realPos - start);

        return true;

    }
    
    
    /**
     * Recycle the input buffer. This should be called when closing the 
     * connection.
     */
    public void recycle() {

        // Recycle Request object
        request.recycle();

        inputStream = null;
        lastValid = 0;
        pos = 0;

        parsingHeader = true;

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
        
        // Copy leftover bytes to the beginning of the buffer
        if (lastValid - pos > 0) {
        	//...
        	
        }
        
        
        // Reset pointers
        lastValid = lastValid - pos;
        pos = 0;
        parsingHeader = true;
    }
    
    
    /**
     * End request (consumes leftover bytes).
     * 
     * @throws IOException an undelying I/O error occured
     */
    public void endRequest()
        throws IOException {
    	
    }
    
	// ---------------- Protected Methods -------------------
    
    /**
     * Fill the internal buffer using data from the undelying input stream.
     * 
     * @return false if at end of stream
     */
    protected boolean fill()
        throws IOException {
    	
    	int nRead = 0;
    	
    	if (parsingHeader) {
    		
    		if (lastValid == buf.length) {
    			throw new IllegalArgumentException("requestheadertoolarge.error");
    		}
    		
    		nRead = inputStream.read(buf, pos, buf.length - lastValid);
    		
    		if (nRead > 0) {
                lastValid = pos + nRead;
            }
    	}else{
    		
    		//....
    	}
    	
    	return (nRead > 0);
    }
    
    
	// --------- InputStreamInputBuffer Inner Class ---------
    
    /**
     * This class is an input buffer which will read its data from an input
     * stream.
     */
    protected class InputStreamInputBuffer 
        implements InputBuffer {
    	
    	
    	/**
         * Read bytes into the specified chunk.
         */
        public int doRead(ByteChunk chunk, Request req ) 
            throws IOException {
        	
        	if (pos >= lastValid) {
                if (!fill())
                    return -1;
            }

            int length = lastValid - pos;
            chunk.setBytes(buf, pos, length);
            pos = lastValid;

            return (length);
        }
    }
    
}
