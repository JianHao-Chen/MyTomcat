package My.coyote.http11;

import java.io.IOException;

import My.coyote.OutputBuffer;
import My.coyote.Response;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.http.HttpMessages;
import My.tomcat.util.http.MimeHeaders;
import My.tomcat.util.net.NioChannel;
import My.tomcat.util.net.NioSelectorPool;

public class InternalNioOutputBuffer implements OutputBuffer{

	int bbufLimit = 0;
	
	// ------------------ Constructors ------------------
	
	/**
     * Default constructor.
     */
	public InternalNioOutputBuffer(Response response) {
		this(response, Constants.DEFAULT_HTTP_HEADER_BUFFER_SIZE);
	}
	
	/**
     * Alternate constructor.
     */
    public InternalNioOutputBuffer(Response response, int headerBufferSize) {
    	this.response = response;
        headers = response.getMimeHeaders();
        
        buf = new byte[headerBufferSize];
        
        if (headerBufferSize < (8 * 1024)) {
            bbufLimit = 6 * 1500;    
        } else {
            bbufLimit = (headerBufferSize / 1500 + 1) * 1500;
        }
        
        outputStreamOutputBuffer = new SocketOutputBuffer();

        filterLibrary = new OutputFilter[0];
        activeFilters = new OutputFilter[0];
        lastActiveFilter = -1;

        committed = false;
        finished = false;
        
        HttpMessages.getMessage(200);
    }
    
    
	// -------------- Instance Variables --------------
    
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
     * Pointer to the current write buffer.
     */
    protected byte[] buf;


    /**
     * Position in the buffer.
     */
    protected int pos;

    /**
     * Number of bytes last written
     */
    protected Integer lastWrite = new Integer(1);

    /**
     * Underlying socket.
     */
    protected NioChannel socket;
    
    /**
     * Selector pool, for blocking reads and blocking writes
     */
    protected NioSelectorPool pool;



    /**
     * Underlying output buffer.
     */
    protected OutputBuffer outputStreamOutputBuffer;


    /**
     * Filter library.
     * Note: Filter[0] is always the "chunked" filter.
     */
    protected OutputFilter[] filterLibrary;


    /**
     * Active filter (which is actually the top of the pipeline).
     */
    protected OutputFilter[] activeFilters;


    /**
     * Index of the last active filter.
     */
    protected int lastActiveFilter;
    
    
    // ------------------------- Properties -------------------------
    
    /**
     * Set the underlying socket.
     */
    public void setSocket(NioChannel socket) {
        this.socket = socket;
    }

    /**
     * Get the underlying socket input stream.
     */
    public NioChannel getSocket() {
        return socket;
    }

    public void setSelectorPool(NioSelectorPool pool) { 
        this.pool = pool;
    }

    public NioSelectorPool getSelectorPool() {
        return pool;
    }    
    /**
     * Set the socket buffer size.
     */
    public void setSocketBuffer(int socketBufferSize) {
        // FIXME: Remove
    }


    /**
     * Add an output filter to the filter library.
     */
    public void addFilter(OutputFilter filter) {

        OutputFilter[] newFilterLibrary = 
            new OutputFilter[filterLibrary.length + 1];
        for (int i = 0; i < filterLibrary.length; i++) {
            newFilterLibrary[i] = filterLibrary[i];
        }
        newFilterLibrary[filterLibrary.length] = filter;
        filterLibrary = newFilterLibrary;

        activeFilters = new OutputFilter[filterLibrary.length];

    }


    /**
     * Get filters.
     */
    public OutputFilter[] getFilters() {

        return filterLibrary;

    }


    /**
     * Clear filters.
     */
    public void clearFilters() {

        filterLibrary = new OutputFilter[0];
        lastActiveFilter = -1;

    }


    /**
     * Add an output filter to the filter library.
     */
    public void addActiveFilter(OutputFilter filter) {

        if (lastActiveFilter == -1) {
            filter.setBuffer(outputStreamOutputBuffer);
        } else {
            for (int i = 0; i <= lastActiveFilter; i++) {
                if (activeFilters[i] == filter)
                    return;
            }
            filter.setBuffer(activeFilters[lastActiveFilter]);
        }

        activeFilters[++lastActiveFilter] = filter;

        filter.setResponse(response);

    }

	
    
    
	// -------------------- Public Methods --------------------
  
    @Override
	public int doWrite(ByteChunk chunk, Response response) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
    
    
    
    // ----------------------------------- OutputStreamOutputBuffer Inner Class


    /**
     * This class is an output buffer which will write data to an output
     * stream.
     */
    protected class SocketOutputBuffer 
        implements OutputBuffer {


        /**
         * Write chunk.
         */
        public int doWrite(ByteChunk chunk, Response res) 
            throws IOException {

            int len = chunk.getLength();
            int start = chunk.getStart();
            byte[] b = chunk.getBuffer();
       //     addToBB(b, start, len);
            return chunk.getLength();

        }


    }

    
}
