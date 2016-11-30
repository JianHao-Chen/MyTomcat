package My.catalina.ha.session;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import My.catalina.Container;
import My.catalina.Lifecycle;
import My.catalina.Loader;
import My.catalina.ha.ClusterManager;
import My.catalina.session.ManagerBase;
import My.catalina.tribes.io.ReplicationStream;

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
	
	
	/**
     * Open Stream and use correct ClassLoader (Container) Switch
     * ThreadClassLoader
     */
	public ReplicationStream getReplicationStream(byte[] data) throws IOException {
		return getReplicationStream(data,0,data.length);
	}
	
	public ReplicationStream getReplicationStream(byte[] data, int offset, int length) throws IOException {
		ByteArrayInputStream fis = new ByteArrayInputStream(data, offset, length);
		return new ReplicationStream(fis, getClassLoaders());
	}
}
