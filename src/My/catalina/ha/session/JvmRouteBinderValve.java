package My.catalina.ha.session;

import java.io.IOException;

import javax.servlet.ServletException;

import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.connector.Request;
import My.catalina.connector.Response;
import My.catalina.ha.CatalinaCluster;
import My.catalina.ha.ClusterValve;
import My.catalina.valves.ValveBase;

public class JvmRouteBinderValve 
	extends ValveBase 
	implements ClusterValve, Lifecycle{

	
	
	//-------------------- Instance Variables ----------------------

    /**
     * the cluster
     */
    protected CatalinaCluster cluster;
	
	
	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LifecycleListener[] findLifecycleListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}

	/**
     * @return Returns the cluster.
     */
    public CatalinaCluster getCluster() {
        return cluster;
    }
    
    /**
     * @param cluster The cluster to set.
     */
    public void setCluster(CatalinaCluster cluster) {
        this.cluster = cluster;
    }

	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
		// Pass this request on to the next valve in our pipeline
        getNext().invoke(request, response);
	}

}
