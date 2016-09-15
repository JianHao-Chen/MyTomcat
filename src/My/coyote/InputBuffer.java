package My.coyote;

import java.io.IOException;

import My.tomcat.util.buf.ByteChunk;

/**
 * This class is used only in the protocol implementation. 
 * All reading from tomcat ( or adapter ) should be done
 * using Request.doRead().
 */

public interface InputBuffer {

	/** Return from the input stream.
    IMPORTANT: the current model assumes that the protocol will 'own' the
    buffer and return a pointer to it in ByteChunk ( i.e. the param will
    have chunk.getBytes()==null before call, and the result after the call ).
	 */
	public int doRead(ByteChunk chunk, Request request) 
    	throws IOException;
}
