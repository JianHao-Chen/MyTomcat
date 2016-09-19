package My.catalina.connector;

import java.io.PrintWriter;

/**
 * Coyote implementation of the servlet writer.
 */

public class CoyoteWriter extends PrintWriter {

	// -------------------------- Constants--------------------------
	
	private static final char[] LINE_SEP = { '\r', '\n' };
	
	// -------------------- Instance Variables----------------------
	
	protected OutputBuffer ob;
    protected boolean error = false;

	// ------------------------ Constructors------------------------
    public CoyoteWriter(OutputBuffer ob) {
        super(ob);
        this.ob = ob;
    }
    
    
	// -------------- Public Methods --------------
    /**
     * Prevent cloning the facade.
     */
    protected Object clone()
        throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    
	// --------------------- Package Methods--------------------
    /**
     * Clear facade.
     */
    void clear() {
        ob = null;
    }


    /**
     * Recycle.
     */
    void recycle() {
        error = false;
    }
    
    
	// ---------------------- Writer Methods----------------------
    
    
}
