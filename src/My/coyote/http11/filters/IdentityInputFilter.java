package My.coyote.http11.filters;

import java.io.IOException;

import My.coyote.InputBuffer;
import My.coyote.Request;
import My.coyote.http11.InputFilter;
import My.tomcat.util.buf.ByteChunk;

public class IdentityInputFilter implements InputFilter{

	 // -------------------------------------------------------------- Constants


    protected static final String ENCODING_NAME = "identity";
    protected static final ByteChunk ENCODING = new ByteChunk();


    // ----------------------------------------------------- Static Initializer


    static {
        ENCODING.setBytes(ENCODING_NAME.getBytes(), 0, ENCODING_NAME.length());
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Content length.
     */
    protected long contentLength = -1;


    /**
     * Remaining bytes.
     */
    protected long remaining = 0;


    /**
     * Next buffer in the pipeline.
     */
    protected InputBuffer buffer;


    /**
     * Chunk used to read leftover bytes.
     */
    protected ByteChunk endChunk = new ByteChunk();


    // ------------------------------------------------------------- Properties


    /**
     * Get content length.
     */
    public long getContentLength() {
        return contentLength;
    }


    /**
     * Get remaining bytes.
     */
    public long getRemaining() {
        return remaining;
    }


    // ---------------------------------------------------- InputBuffer Methods


    /**
     * Read bytes.
     * 
     * @return If the filter does request length control, this value is
     * significant; it should be the number of bytes consumed from the buffer,
     * up until the end of the current request body, or the buffer length, 
     * whichever is greater. If the filter does not do request body length
     * control, the returned value should be -1.
     */
    public int doRead(ByteChunk chunk, Request req)
        throws IOException {

        int result = -1;

        if (contentLength >= 0) {
            if (remaining > 0) {
                int nRead = buffer.doRead(chunk, req);
                if (nRead > remaining) {
                    // The chunk is longer than the number of bytes remaining
                    // in the body; changing the chunk length to the number
                    // of bytes remaining
                    chunk.setBytes(chunk.getBytes(), chunk.getStart(), 
                                   (int) remaining);
                    result = (int) remaining;
                } else {
                    result = nRead;
                }
                remaining = remaining - nRead;
            } else {
                // No more bytes left to be read : return -1 and clear the 
                // buffer
                chunk.recycle();
                result = -1;
            }
        }

        return result;

    }


    // ---------------------------------------------------- InputFilter Methods


    /**
     * Read the content length from the request.
     */
    public void setRequest(Request request) {
        contentLength = request.getContentLengthLong();
        remaining = contentLength;
    }


    /**
     * End the current request.
     */
    public long end()
        throws IOException {

        // Consume extra bytes.
        while (remaining > 0) {
            int nread = buffer.doRead(endChunk, null);
            if (nread > 0 ) {
                remaining = remaining - nread;
            } else { // errors are handled higher up.
                remaining = 0;
            }
        }

        // If too many bytes were read, return the amount.
        return -remaining;

    }


    /**
     * Amount of bytes still available in a buffer.
     */
    public int available() {
        return 0;
    }
    

    /**
     * Set the next buffer in the filter pipeline.
     */
    public void setBuffer(InputBuffer buffer) {
        this.buffer = buffer;
    }


    /**
     * Make the filter ready to process the next request.
     */
    public void recycle() {
        contentLength = -1;
        remaining = 0;
        endChunk.recycle();
    }


    /**
     * Return the name of the associated encoding; Here, the value is 
     * "identity".
     */
    public ByteChunk getEncodingName() {
        return ENCODING;
    }
}
