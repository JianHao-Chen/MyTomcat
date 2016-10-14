package My.catalina.connector;

import java.io.IOException;

import javax.servlet.ServletInputStream;

public class CoyoteInputStream extends ServletInputStream{

	// -------------- Instance Variables -----------------


    protected InputBuffer ib;
    
    
	// ----------------------- Constructors -----------------------
    
    
    protected CoyoteInputStream(InputBuffer ib) {
        this.ib = ib;
    }
    
    
 // -------------------------------------------------------- Package Methods


    /**
     * Clear facade.
     */
    void clear() {
        ib = null;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Prevent cloning the facade.
     */
    protected Object clone()
        throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }


    // --------------------------------------------- ServletInputStream Methods


    public int read()
        throws IOException {    
    	
    	return ib.readByte();
    }
    
    
    public int read(final byte[] b, final int off, final int len)
    throws IOException {
    	
    	return ib.read(b, off, len);
    }
}
