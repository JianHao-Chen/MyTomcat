package My.catalina.ha.tcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import My.catalina.ha.ClusterMessage;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Engine;
import My.catalina.Host;
import My.catalina.Lifecycle;
import My.catalina.LifecycleEvent;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Manager;
import My.catalina.Valve;
import My.catalina.ha.CatalinaCluster;
import My.catalina.ha.ClusterListener;
import My.catalina.ha.ClusterManager;
import My.catalina.ha.ClusterValve;
import My.catalina.ha.session.ClusterSessionListener;
import My.catalina.ha.session.DeltaManager;
import My.catalina.ha.session.JvmRouteBinderValve;
import My.catalina.ha.session.JvmRouteSessionIDBinderListener;
import My.catalina.tribes.Channel;
import My.catalina.tribes.ChannelListener;
import My.catalina.tribes.Member;
import My.catalina.tribes.MembershipListener;
import My.catalina.tribes.group.GroupChannel;
import My.catalina.tribes.group.interceptors.MessageDispatch15Interceptor;
import My.catalina.tribes.group.interceptors.TcpFailureDetector;
import My.catalina.util.LifecycleSupport;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.tomcat.util.IntrospectionUtils;

/**
 * A <b>Cluster </b> implementation using simple multicast. Responsible for
 * setting up a cluster and provides callers with a valid multicast
 * receiver/sender!
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

    /**
     * Listeners of messages
     */
    protected List clusterListeners = new ArrayList();
    
    private List valves = new ArrayList();
    
    
    
    private int channelStartOptions = Channel.DEFAULT;
    
    private int channelSendOptions = Channel.SEND_OPTIONS_ASYNCHRONOUS;
    
    
	// -------------------- Properties --------------------
    
    
    /**
     *   For Debug
     */
    public SimpleTcpCluster() {
    	System.out.println("SimpleTcpCluster constructor....");
    }
    
    
    
    /**
     * Return descriptive information about this Cluster implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (info);
    }

    /**
     * Set the name of the cluster to join, if no cluster with this name is
     * present create one.
     * 
     * @param clusterName
     *            The clustername to join
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * Return the name of the cluster that this Server is currently configured
     * to operate within.
     * 
     * @return The name of the cluster associated with this server
     */
    public String getClusterName() {
        if(clusterName == null && container != null)
            return container.getName() ;
        return clusterName;
    }
    
    
    /**
     * Add cluster valve 
     * Cluster Valves are only add to container when cluster is started!
     * @param valve The new cluster Valve.
     */
    public void addValve(Valve valve) {
        if (valve instanceof ClusterValve && (!valves.contains(valve)))
            valves.add(valve);
    }

    /**
     * get all cluster valves
     * @return current cluster valves
     */
    public Valve[] getValves() {
        return (Valve[]) valves.toArray(new Valve[valves.size()]);
    }
    
    
    
    
    /**
     * Set the Container associated with our Cluster
     * 
     * @param container
     *            The Container to use
     */
    public void setContainer(Container container) {
        Container oldContainer = this.container;
        this.container = container;
    }

    /**
     * Get the Container associated with our Cluster
     * 
     * @return The Container associated with our Cluster
     */
    public Container getContainer() {
        return (this.container);
    }
    
    
    /**
     * Create new Manager without add to cluster (comes with start the manager)
     * 
     * @param name
     *            Context Name of this manager
     * @see org.apache.catalina.Cluster#createManager(java.lang.String)
     * @see #addManager(String, Manager)
     * @see DeltaManager#start()
     */
    public synchronized Manager createManager(String name) {
    	
    	Manager manager = null;
    	try {
    		manager = managerTemplate.cloneFromTemplate();
    		((ClusterManager)manager).setName(name);
    	}
    	catch (Exception x) {
    		manager = new My.catalina.ha.session.DeltaManager();
    	}
    	finally {
    		if ( manager != null && (manager instanceof ClusterManager))
    			((ClusterManager)manager).setCluster(this);
    	}
    	return manager;
    }
    
    public void registerManager(Manager manager) {
    	if (! (manager instanceof ClusterManager)) {
            log.warn("Manager [ " + manager + "] does not implement ClusterManager, addition to cluster has been aborted.");
            return;
        }
    	
    	ClusterManager cmanager = (ClusterManager) manager ;
    	cmanager.setDistributable(true);
    	
    	String clusterName = getManagerName(cmanager.getName(), manager);
    	cmanager.setName(clusterName);
        cmanager.setCluster(this);
        cmanager.setDefaultMode(false);
        
        managers.put(clusterName, manager);
        
    }
    
    public String getManagerName(String name, Manager manager) {
    	String clusterName = name ;
    	if ( clusterName == null ) 
    		clusterName = manager.getContainer().getName();
    	
    	if(getContainer() instanceof Engine) {
    		Container context = manager.getContainer() ;
    		if(context != null && context instanceof Context) {
    			Container host = ((Context)context).getParent();
    			if(host != null && host instanceof Host && clusterName!=null && !(clusterName.indexOf("#")>=0))
                    clusterName = host.getName() +"#" + clusterName ;
    		}
    	}
    	return clusterName;
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
	
	
	
	/**
     * Get the cluster listeners associated with this cluster. If this Array has
     * no listeners registered, a zero-length array is returned.
     */
    public ClusterListener[] findClusterListeners() {
    	if (clusterListeners.size() > 0) {
    		ClusterListener[] listener = new ClusterListener[clusterListeners.size()];
            clusterListeners.toArray(listener);
            return listener;
    	}
    	else
            return new ClusterListener[0];
    }

    
    /**
     * add cluster message listener and register cluster to this listener
     * 
     * @see org.apache.catalina.ha.CatalinaCluster#addClusterListener(org.apache.catalina.ha.MessageListener)
     */
    public void addClusterListener(ClusterListener listener) {
    	if (listener != null && !clusterListeners.contains(listener)) {
            clusterListeners.add(listener);
            listener.setCluster(this);
        }
    }
	
	
	
	// ------------------------- public ----------------------
	
	 /**
     * Prepare for the beginning of active use of the public methods of this
     * component. This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized. <BR>
     * Starts the cluster communication channel, this will connect with the
     * other nodes in the cluster, and request the current session state to be
     * transferred to this node.
     * 
     * @exception IllegalStateException
     *                if this component has already been started
     * @exception LifecycleException
     *                if this component detects a fatal error that prevents this
     *                component from being used
     */
	public void start() throws LifecycleException {
		if (started)
            throw new LifecycleException("cluster.alreadyStarted");
		if (log.isInfoEnabled()) log.info("Cluster is about to start");
		
		try {
			checkDefaults();
			registerClusterValve();
			
			channel.addMembershipListener(this);
			channel.addChannelListener(this);
			channel.start(channelStartOptions);
			
			this.started = true;
			
		}
		catch (Exception x) {
			log.error("Unable to start cluster.", x);
            throw new LifecycleException(x);
		}
		
	}
	
	
	protected void checkDefaults() {
		if ( clusterListeners.size() == 0 ) {
			addClusterListener(new JvmRouteSessionIDBinderListener()); 
        	addClusterListener(new ClusterSessionListener());
		}
		
		if ( valves.size() == 0 ) {
			addValve(new JvmRouteBinderValve());
            addValve(new ReplicationValve());
		}
		
		if ( channel == null ) 
			channel = new GroupChannel();
		
		if ( channel instanceof GroupChannel
				&& 
				!((GroupChannel)channel).getInterceptors().hasNext()) 
		{
			channel.addInterceptor(new MessageDispatch15Interceptor());
            channel.addInterceptor(new TcpFailureDetector());
		}
		
	}
	
	
	/**
     * register all cluster valve to host or engine
     * @throws Exception
     * @throws ClassNotFoundException
     */
    protected void registerClusterValve() throws Exception {
    	
    	if(container != null ) {
    		for (Iterator iter = valves.iterator(); iter.hasNext();) {
    			ClusterValve valve = (ClusterValve) iter.next();
    			if (valve != null) {
    				IntrospectionUtils.callMethodN(getContainer(), "addValve",
                            new Object[] { valve },
                            new Class[] { My.catalina.Valve.class });
    			}
    			valve.setCluster(this);
    		}
    	}
    }
	
	
	
	

	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	/**
     * has members
     */
    protected boolean hasMembers = false;
    public boolean hasMembers() {
        return hasMembers;
    }
    
    
    /**
     * Get all current cluster members
     * @return all members or empty array 
     */
    public Member[] getMembers() {
        return channel.getMembers();
    }

    /**
     * Return the member that represents this node.
     * 
     * @return Member
     */
    public Member getLocalMember() {
        return channel.getLocalMember(true);
    }
	
	
	/**
     * New cluster member is registered
     * 
     * @see org.apache.catalina.ha.MembershipListener#memberAdded(org.apache.catalina.ha.Member)
     */
    public void memberAdded(Member member) {
    	try {
    		hasMembers = channel.hasMembers();
    		if (log.isInfoEnabled()) 
    			log.info("Replication member added:" + member);
    		
    		// Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(BEFORE_MEMBERREGISTER_EVENT, member);
    		
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(AFTER_MEMBERREGISTER_EVENT, member);
    	}
    	catch (Exception x) {
            log.error("Unable to connect to replication system.", x);
        }
    }
    
    
    
    /**
     * send message to all cluster members same cluster domain
     * 
     * @see org.apache.catalina.ha.CatalinaCluster#send(org.apache.catalina.ha.ClusterMessage)
     */
    public void sendClusterDomain(ClusterMessage msg) {
        send(msg,null);
    } 
    
    
    /**
     * send a cluster message to one member
     */
    public void send(ClusterMessage msg, Member dest) {
    	try {
    		msg.setAddress(getLocalMember());
    		if (dest != null) {
    			
    		}
    		else {
    			if (channel.getMembers().length>0)
    				channel.send(channel.getMembers(),msg,channelSendOptions);
    			else if (log.isDebugEnabled()) 
                    log.debug("No members in cluster, ignoring message:"+msg);
    		}
    	}
    	catch (Exception x) {
    		log.error("Unable to send message through cluster sender.", x);
    	}
    }
    
    
}
