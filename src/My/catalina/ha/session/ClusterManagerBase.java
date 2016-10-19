package My.catalina.ha.session;

import My.catalina.Lifecycle;
import My.catalina.ha.ClusterManager;
import My.catalina.session.ManagerBase;

public abstract class ClusterManagerBase 
	extends ManagerBase 
	implements Lifecycle, ClusterManager{

}
