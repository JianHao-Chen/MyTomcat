package javax.servlet;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Provides an output stream for sending binary data to the
 * client. A <code>ServletOutputStream</code> object is normally retrieved 
 * via the {@link ServletResponse#getOutputStream} method.
 *
 * <p>This is an abstract class that the servlet container implements.
 * Subclasses of this class
 * must implement the <code>java.io.OutputStream.write(int)</code>
 * method.
 */

public abstract class ServletOutputStream extends OutputStream{

	protected ServletOutputStream() { }
	
	 /**
     * Writes a <code>String</code> to the client, 
     * without a carriage return-line feed (CRLF) 
     * character at the end.
     */
	public void print(String s) throws IOException {
		if (s==null) s="null";
		
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt (i);
			
			// char in Java is 2 bytes.
			// Java use BIG-ENDIAN
			
			if ((c & 0xff00) != 0) { // high order byte must be zero
				String errMsg = "Error : not_iso8859_1";
				//System.out.println(errMsg);
				throw new CharConversionException(errMsg);
			}
			write (c);
		}
		
	}
	
	
	
	
}
