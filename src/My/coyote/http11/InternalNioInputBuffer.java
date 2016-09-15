package My.coyote.http11;

import java.io.IOException;

import My.coyote.InputBuffer;
import My.coyote.Request;
import My.tomcat.util.buf.ByteChunk;
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
		 
	}
	
	
	
	
	 /**
     * Additional size we allocate to the buffer to be more effective when
     * skipping empty lines that may precede the request.
     */
    private static final int skipBlankLinesSize = 1024;
	
	
	// ------------------  Instance Variables ------------------  
	
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
     * Pointer to the current read buffer.
     */
    protected byte[] buf;
    
    
	
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
    
    
    
    
    
	// ------------------ InputBuffer Methods ------------------
    /**
     * Read some bytes.
     */
    public int doRead(ByteChunk chunk, Request req) 
        throws IOException {
    	
    	if (lastActiveFilter == -1)
            return inputStreamInputBuffer.doRead(chunk, req);
        else
            return activeFilters[lastActiveFilter].doRead(chunk,req);
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
    		
    		return 0;
    	}
    	
    }


}
