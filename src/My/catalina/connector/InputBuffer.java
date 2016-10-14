package My.catalina.connector;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import My.tomcat.util.buf.B2CConverter;
import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.CharChunk;

public class InputBuffer extends Reader
	implements ByteChunk.ByteInputChannel, CharChunk.CharInputChannel,
    CharChunk.CharOutputChannel {
	
	// -------------------------------------------------------------- Constants


    public static final String DEFAULT_ENCODING = 
        My.coyote.Constants.DEFAULT_CHARACTER_ENCODING;
    public static final int DEFAULT_BUFFER_SIZE = 8*1024;

    // The buffer can be used for byte[] and char[] reading
    // ( this is needed to support ServletInputStream and BufferedReader )
    public final int INITIAL_STATE = 0;
    public final int CHAR_STATE = 1;
    public final int BYTE_STATE = 2;
    
    
    
	// ------------------------- Constructors -------------------------
    
    /**
     * Default constructor. Allocate the buffer with the default buffer size.
     */
    public InputBuffer() {

        this(DEFAULT_BUFFER_SIZE);

    }


    /**
     * Alternate constructor which allows specifying the initial buffer size.
     * 
     * @param size Buffer size to use
     */
    public InputBuffer(int size) {

    	this.size = size;
        bb = new ByteChunk(size);
        bb.setLimit(size);
        bb.setByteInputChannel(this);
        cb = new CharChunk(size);
        cb.setLimit(size);
        cb.setOptimizedWrite(false);
        cb.setCharInputChannel(this);
        cb.setCharOutputChannel(this);
    }
    
	
	// ------------------ Instance Variables ------------------
	
	/**
     * Associated Coyote request.
     */
    private My.coyote.Request coyoteRequest;
    
    
    /**
     * The byte buffer.
     */
    private ByteChunk bb;


    /**
     * The chunk buffer.
     */
    private CharChunk cb;


    /**
     * State of the output buffer.
     */
    private int state = 0;


    /**
     * Number of bytes read.
     */
    private int bytesRead = 0;


    /**
     * Number of chars read.
     */
    private int charsRead = 0;

    
    /**
     * Flag which indicates if the input buffer is closed.
     */
    private boolean closed = false;
    
    /**
     * Byte chunk used to input bytes.
     */
    private ByteChunk inputChunk = new ByteChunk();
    
    
    /**
     * Encoding to use.
     */
    private String enc;


    /**
     * Encoder is set.
     */
    private boolean gotEnc = false;


    /**
     * List of encoders.
     */
    protected HashMap encoders = new HashMap();


    /**
     * Current byte to char converter.
     */
    protected B2CConverter conv;


    /**
     * Buffer position.
     */
    private int markPos = -1;


    /**
     * Buffer size.
     */
    private int size = -1;
    
    
	
	// ---------------------- Properties ----------------------
	 /**
     * Associated Coyote request.
     * 
     * @param coyoteRequest Associated Coyote request
     */
    public void setRequest(My.coyote.Request coyoteRequest) {
	this.coyoteRequest = coyoteRequest;
    }


    /**
     * Get associated Coyote request.
     * 
     * @return the associated Coyote request
     */
    public My.coyote.Request getRequest() {
        return this.coyoteRequest;
    }
	
	
	
	
	
	// ------------------ Public Methods ------------------
    
    /**
     * Recycle the output buffer.
     */
    public void recycle() {
    	
    	 state = INITIAL_STATE;
         bytesRead = 0;
         charsRead = 0;
        
         // If usage of mark made the buffer too big, reallocate it
         if (cb.getChars().length > size) {
             cb = new CharChunk(size);
             cb.setLimit(size);
             cb.setOptimizedWrite(false);
             cb.setCharInputChannel(this);
             cb.setCharOutputChannel(this);
         } else {
             cb.recycle();
         }
         markPos = -1;
         bb.recycle(); 
         closed = false;
         
         if (conv != null) {
             conv.recycle();
         }
         
         gotEnc = false;
         enc = null;
        
    }
    
    
    
    public int readByte()
    throws IOException {
    	
    	if (closed)
            throw new IOException("inputBuffer.streamClosed");
    	
    	return bb.substract();
    }
	
    
    public int read(byte[] b, int off, int len)
    throws IOException {
    	
    	if (closed)
            throw new IOException("inputBuffer.streamClosed");
    	
    	return bb.substract(b, off, len);
    	
    }
    
	

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() throws IOException {
		 closed = true;
		
	}

	@Override
	public void realWriteChars(char[] cbuf, int off, int len)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int realReadChars(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	 /** 
     * Reads new bytes in the byte chunk.
     * 
     * @param cbuf Byte buffer to be written to the response
     * @param off Offset
     * @param len Length
     * 
     * @throws IOException An underlying IOException occurred
     */
	public int realReadBytes(byte[] cbuf, int off, int len) throws IOException {
		if (closed)
            return -1;
        if (coyoteRequest == null)
            return -1;

        if(state == INITIAL_STATE)
            state = BYTE_STATE;

        int result = coyoteRequest.doRead(bb);

        return result;
	}
}
