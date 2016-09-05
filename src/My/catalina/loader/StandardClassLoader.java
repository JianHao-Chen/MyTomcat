package My.catalina.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Subclass implementation of <b>java.net.URLClassLoader</b>. 
 * There are no functional differences between this class and 
 * java.net.URLClassLoader.
 */

public class StandardClassLoader 
	extends URLClassLoader{

	public StandardClassLoader(URL repositories[]) {
        super(repositories);
    }

    public StandardClassLoader(URL repositories[], ClassLoader parent) {
        super(repositories, parent);
    }
}
