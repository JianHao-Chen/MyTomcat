package My.catalina.ha.tcp;

import java.util.HashMap;
import java.util.Map;

import My.catalina.Container;
import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.ha.CatalinaCluster;
import My.catalina.ha.ClusterManager;
import My.catalina.ha.session.DeltaManager;
import My.catalina.tribes.Channel;
import My.catalina.tribes.ChannelListener;
import My.catalina.tribes.MembershipListener;
import My.catalina.tribes.group.GroupChannel;
import My.catalina.util.LifecycleSupport;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

/**
 * A <b>Cluster </b> implementation using simple multicast. Responsible for
 * setting up a cluster and provides callers with a valid multicast
 * receiver/sender.
 */

public class SimpleTcpCluster 
	implements CatalinaCluster, Lifecycle, LifecycleListener,
				MembershipListener, ChannelListener{
	
	public static Log log = LogFactory.getLog(SimpleTcpCluster.class);
	

	// ------------------- Instance Variables -------------------
	/**
     * Descriptive information about this component implementation.
     */
    protected static final String info = "SimpleTcpCluster/2.2";
    
    public static final String BEFORE_MEMBERREGISTER_EVENT = "before_member_register";

    public static final String AFTER_MEMBERREGISTER_EVENT = "after_member_register";

    public static final String BEFORE_MANAGERREGISTER_EVENT = "before_manager_register";

    public static final String AFTER_MANAGERREGISTER_EVENT = "after_manager_register";

    public static final String BEFORE_MANAGERUNREGISTER_EVENT = "before_manager_unregister";

    public static final String AFTER_MANAGERUNREGISTER_EVENT = "after_manager_unregister";

    public static final String BEFORE_MEMBERUNREGISTER_EVENT = "before_member_unregister";

    public static final String AFTER_MEMBERUNREGISTER_EVENT = "after_member_unregister";

    public static final String SEND_MESSAGE_FAILURE_EVENT = "send_message_failure";

    public static final String RECEIVE_MESSAGE_FAILURE_EVENT = "receive_message_failure";
    
    
    /**
     * Group channel.
     */
    protected Channel channel = new GroupChannel();
    
    /**
     * The cluster name to join
     */
    protected String clusterName ;
    
    
    /**
     * The Container associated with this Cluster.
     */
    protected Container container = null;

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    /**
     * Has this component been started?
     */
    protected boolean started = false;
    
    /**
     * The context name <-> manager association for distributed contexts.
     */
    protected Map managers = new HashMap();
    
    protected ClusterManager managerTemplate = new DeltaManager();

    
    
    
    
	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClusterName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClusterName(String clusterName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lifecycleEvent(LifecycleEvent event) {
		// TODO Auto-generated method stub
		
	}

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
    
    
}
