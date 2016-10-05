package My.coyote.http11;

import java.io.IOException;
import java.io.OutputStream;

import My.coyote.ActionCode;
import My.coyote.OutputBuffer;
import My.coyote.Response;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.CharChunk;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.http.MimeHeaders;

public class InternalOutputBuffer 
	implements OutputBuffer, ByteChunk.ByteOutputChannel{

	
	private static final My.juli.logging.Log log
    = My.juli.logging.LogFactory.getLog(InternalOutputBuffer.class);
	
	
	
	// ----------------------- Constructors ----------------------
	
	/**
     * Default constructor.
     */
    public InternalOutputBuffer(Response response) {
        this(response, Constants.DEFAULT_HTTP_HEADER_BUFFER_SIZE);
    }


    /**
     * Alternate constructor.
     */
    public InternalOutputBuffer(Response response, int headerBufferSize) {
    	
    	this.response = response;
        
        headers = response.getMimeHeaders();

        buf = new byte[headerBufferSize];

        outputStreamOutputBuffer = new OutputStreamOutputBuffer();
     
        socketBuffer = new ByteChunk();
        socketBuffer.setByteOutputChannel(this);
        
        committed = false;
        finished = false;
    }
    
    
    
	// ------------------------ Variables ------------------------
    
    /**
     * Associated Coyote response.
     */
    protected Response response;
    
    /**
     * Headers of the associated request.
     */
    protected MimeHeaders headers;


    /**
     * Committed flag.
     */
    protected boolean committed;


    /**
     * Finished flag.
     */
    protected boolean finished;


    /**
     * The buffer used for header composition.
     */
    protected byte[] buf;


    /**
     * Position in the buffer.
     */
    protected int pos;


    /**
     * Underlying output stream.
     */
    protected OutputStream outputStream;


    /**
     * Underlying output buffer.
     */
    protected OutputBuffer outputStreamOutputBuffer;
    
    /**
     * Socket buffer.
     */
    protected ByteChunk socketBuffer;


    /**
     * Socket buffer (extra buffering to reduce number of packets sent).
     */
    protected boolean useSocketBuffer = false;
    
    
	// -------------------- Properties --------------------
    
    /**
     * Set the underlying socket output stream.
     */
    public void setOutputStream(OutputStream outputStream) {
    	this.outputStream = outputStream;
    }
    
    /**
     * Get the underlying socket output stream.
     */
    public OutputStream getOutputStream() {

        return outputStream;

    }

    /**
     * Set the socket buffer size.
     */
    public void setSocketBuffer(int socketBufferSize) {

        if (socketBufferSize > 500) {
            useSocketBuffer = true;
            socketBuffer.allocate(socketBufferSize, socketBufferSize);
        } else {
            useSocketBuffer = false;
        }

    }
    
    
    
	// ---------------------- Public Methods ----------------------
    
    
    
	// ------------------- HTTP/1.1 Output Methods -------------------
    
    
    /**
     * Recycle the output buffer. This should be called when closing the 
     * connection.
     */
    public void recycle() {

        // Recycle Request object
        response.recycle();
        socketBuffer.recycle();

        outputStream = null;
        pos = 0;
        committed = false;
        finished = false;
    }
    
    
    /**
     * End processing of current HTTP request.
     * Note: All bytes of the current request should have been already 
     * consumed. This method only resets all the pointers so that we are ready
     * to parse the next HTTP request.
     */
    public void nextRequest() {
    	
    	// Recycle Request object
        response.recycle();
        socketBuffer.recycle();
        
        // Reset pointers
        pos = 0;
        committed = false;
        finished = false;
    	
    }
    
    
    
    
    /**
     * End request.
     * 
     * @throws IOException an undelying I/O error occured
     */
    public void endRequest()
        throws IOException {
    	
    	if (!committed) {

            // Send the connector a request for commit. The connector should
            // then validate the headers, send them (using sendHeader) and 
            // set the filters accordingly.
            response.action(ActionCode.ACTION_COMMIT, null);

        }
    	
    	if (finished)
            return;
    	
    	if (useSocketBuffer) {
            socketBuffer.flushBuffer();
        }
    	
    	finished = true;
    	
    	
    }
    
    
    /**
     * Send an acknoledgement.
     */
    public void sendAck()
        throws IOException {

        if (!committed)
            outputStream.write(Constants.ACK_BYTES);

    }
    
    
    /**
     * Send the response status line.
     */
    public void sendStatus() {
    	
    	// Write protocol name
        write(Constants.HTTP_11_BYTES);
        
        buf[pos++] = Constants.SP;

        // Write status code
        int status = response.getStatus();
        switch (status) {
        case 200:
            write(Constants._200_BYTES);
            break;
        case 400:
            write(Constants._400_BYTES);
            break;
        case 404:
            write(Constants._404_BYTES);
            break;
        default:
            write(status);	
        }
    }
    
    
    /**
     * Send a header.
     * 
     * @param name Header name
     * @param value Header value
     */
    public void sendHeader(MessageBytes name, MessageBytes value) {

        write(name);
        buf[pos++] = Constants.COLON;
        buf[pos++] = Constants.SP;
        write(value);
        buf[pos++] = Constants.CR;
        buf[pos++] = Constants.LF;

    }
    
    
    /**
     * End the header block.
     */
    public void endHeaders() {

        buf[pos++] = Constants.CR;
        buf[pos++] = Constants.LF;

    }
    
    
    
	// ----------------- OutputBuffer Methods --------------------
    
    /**
     * Write the contents of a byte chunk.
     * 
     * @param chunk byte chunk
     * @return number of bytes written
     * @throws IOException an undelying I/O error occured
     */
    public int doWrite(ByteChunk chunk, Response res) 
        throws IOException {
    	
    	if (!committed) {

            // Send the connector a request for commit. The connector should
            // then validate the headers, send them (using sendHeaders) and 
            // set the filters accordingly.
            response.action(ActionCode.ACTION_COMMIT, null);

        }
    	
    	return outputStreamOutputBuffer.doWrite(chunk, response);
    }
    
    
	// --------------------- Protected Methods ---------------------
    
    
    /**
     * Commit the response.
     * 
     * @throws IOException an undelying I/O error occured
     */
    protected void commit()
        throws IOException {
    	
    	 // The response is now committed
        committed = true;
        response.setCommitted(true);

        if (pos > 0) {
        	
        	// Sending the response header buffer
            if (useSocketBuffer) {
            	socketBuffer.append(buf, 0, pos);
            }
            else {
                outputStream.write(buf, 0, pos);
            }
        }
    }
    
    
    /**
     * This method will write the contents of the specyfied message bytes 
     * buffer to the output stream, without filtering. This method is meant to
     * be used to write the response header.
     * 
     * @param mb data to be written
     */
    protected void write(MessageBytes mb) {

        if (mb.getType() == MessageBytes.T_BYTES) {
            ByteChunk bc = mb.getByteChunk();
            write(bc);
        } else if (mb.getType() == MessageBytes.T_CHARS) {
            CharChunk cc = mb.getCharChunk();
            write(cc);
        } else {
            write(mb.toString());
        }

    }

    
    
    /**
     * This method will write the contents of the specyfied message bytes 
     * buffer to the output stream, without filtering. This method is meant to
     * be used to write the response header.
     * 
     * @param bc data to be written
     */
    protected void write(ByteChunk bc) {
    	
    	// Writing the byte chunk to the output buffer
        int length = bc.getLength();
        System.arraycopy(bc.getBytes(), bc.getStart(), buf, pos, length);
        pos = pos + length;
    }
    
    
    /**
     * This method will write the contents of the specyfied char 
     * buffer to the output stream, without filtering. This method is meant to
     * be used to write the response header.
     * 
     * @param cc data to be written
     */
    protected void write(CharChunk cc) {
    	
    	int start = cc.getStart();
        int end = cc.getEnd();
        char[] cbuf = cc.getBuffer();
        for (int i = start; i < end; i++) {
            char c = cbuf[i];
            // Note:  This is clearly incorrect for many strings,
            // but is the only consistent approach within the current
            // servlet framework.  It must suffice until servlet output
            // streams properly encode their output.
            if ((c <= 31) && (c != 9)) {
                c = ' ';
            } else if (c == 127) {
                c = ' ';
            }
            buf[pos++] = (byte) c;
        }
    }
    
    
    
    /**
     * This method will write the contents of the specyfied byte 
     * buffer to the output stream, without filtering. This method is meant to
     * be used to write the response header.
     * 
     * @param b data to be written
     */
    public void write(byte[] b) {

        // Writing the byte chunk to the output buffer
        System.arraycopy(b, 0, buf, pos, b.length);
        pos = pos + b.length;

    }
    
    
    /**
     * This method will write the contents of the specyfied String to the 
     * output stream, without filtering. This method is meant to be used to 
     * write the response header.
     * 
     * @param s data to be written
     */
    protected void write(String s) {

        if (s == null)
            return;

        // From the Tomcat 3.3 HTTP/1.0 connector
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt (i);
            // Note:  This is clearly incorrect for many strings,
            // but is the only consistent approach within the current
            // servlet framework.  It must suffice until servlet output
            // streams properly encode their output.
            if ((c <= 31) && (c != 9)) {
                c = ' ';
            } else if (c == 127) {
                c = ' ';
            }
            buf[pos++] = (byte) c;
        }

    }
    
    
    /**
     * This method will print the specified integer to the output stream, 
     * without filtering. This method is meant to be used to write the 
     * response header.
     * 
     * @param i data to be written
     */
    protected void write(int i) {

        write(String.valueOf(i));

    }
    
    
    /**
     * Callback to write data from the buffer.
     */
    public void realWriteBytes(byte cbuf[], int off, int len)
        throws IOException {
        if (len > 0) {
            outputStream.write(cbuf, off, len);
        }
    }
    
    
    
	// ------- OutputStreamOutputBuffer Inner Class  -------
    
    /**
     * This class is an output buffer which will write data to an output
     * stream.
     */
    protected class OutputStreamOutputBuffer 
        implements OutputBuffer {
    	
    	
    	/**
         * Write chunk.
         */
        public int doWrite(ByteChunk chunk, Response res) 
            throws IOException {
        	
        	int length = chunk.getLength();
            if (useSocketBuffer) {
                socketBuffer.append(chunk.getBuffer(), chunk.getStart(), 
                                    length);
            } else {
                outputStream.write(chunk.getBuffer(), chunk.getStart(), 
                                   length);
            }
            return length;
        	
        }
    }
    
}
