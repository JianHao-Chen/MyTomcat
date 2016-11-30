package My.catalina.tribes.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Custom subclass of <code>ObjectInputStream</code> that loads from the
 * class loader for this web application.  This allows classes defined only
 * with the web application to be found correctly.
 */

public final class ReplicationStream extends ObjectInputStream {

	/**
     * The class loader we will use to resolve classes.
     */
    private ClassLoader[] classLoaders = null;
    
    /**
     * Construct a new instance of CustomObjectInputStream
     *
     * @param stream The input stream we will read from
     * @param classLoader The class loader used to instantiate objects
     *
     * @exception IOException if an input/output error occurs
     */
    public ReplicationStream(InputStream stream,
                             ClassLoader[] classLoaders)
        throws IOException {

        super(stream);
        this.classLoaders = classLoaders;
    }
    
    
}
