package My.catalina.ha.session;

import My.catalina.Container;
import My.catalina.Lifecycle;
import My.catalina.Loader;
import My.catalina.ha.ClusterManager;
import My.catalina.session.ManagerBase;

public abstract class ClusterManagerBase 
	extends ManagerBase 
	implements Lifecycle, ClusterManager{

	
	public static ClassLoader[] getClassLoaders(Container container) {
		Loader loader = null;
        ClassLoader classLoader = null;
        if (container != null) 
        	loader = container.getLoader();
        if (loader != null) 
        	classLoader = loader.getClassLoader();
        else 
        	classLoader = Thread.currentThread().getContextClassLoader();
        
        if ( classLoader == Thread.currentThread().getContextClassLoader() ) {
            return new ClassLoader[] {classLoader};
        } else {
            return new ClassLoader[] {classLoader,Thread.currentThread().getContextClassLoader()};
        }
        
	}
	
	public ClassLoader[] getClassLoaders() {
        return getClassLoaders(container);
    }
}
