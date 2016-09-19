package My.coyote;

import java.io.IOException;

import My.tomcat.util.buf.ByteChunk;

/**
 * Output buffer.
 *
 * This class is used internally by the protocol implementation. All writes from higher level code should happen
 * via Resonse.doWrite().
 */

public interface OutputBuffer {

	/** Write the response. The caller ( tomcat ) owns the chunks.
    *
    * @param chunk data to write
    * @param response used to allow buffers that can be shared by multiple responses.
    * @return
    * @throws IOException
    */
   public int doWrite(ByteChunk chunk, Response response)
       throws IOException;
}
