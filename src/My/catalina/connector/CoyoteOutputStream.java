package My.catalina.connector;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class CoyoteOutputStream extends ServletOutputStream {

	// ------------------ Instance Variables ------------------
	
	protected OutputBuffer ob;
	
	
	// ---------------------- Constructors ----------------------
	protected CoyoteOutputStream(OutputBuffer ob) {
        this.ob = ob;
    }
	
	
	// --------------------- Public Methods ---------------------
	
	/**
     * Prevent cloning the facade.
     */
    protected Object clone()
        throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
	
	// -------------------- Package Methods --------------------
    
    /**
     * Clear facade.
     */
    void clear() {
        ob = null;
    }
    
    
    
	// ------------------- OutputStream Methods ------------------
    
    public void write(int i) throws IOException {
    	ob.writeByte(i);
    }
	
    public void write(byte[] b, int off, int len)
    	throws IOException {
    ob.write(b, off, len);
}

	
}
