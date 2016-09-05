package My.catalina.startup;

import My.catalina.Engine;
import My.catalina.Lifecycle;
import My.catalina.core.StandardService;
import My.catalina.juli.logging.Log;
import My.catalina.juli.logging.LogFactory;
import My.catalina.util.LifecycleSupport;

/**
 * Convenience class to embed a Catalina servlet container environment
 * inside another application.  You must call the methods of this class in the
 * following order to ensure correct operation.
 * 
 *<ul>
 *<li>Instantiate a new instance of this class.</li>
 *<li>Set the relevant properties of this object itself.  In particular,
 *    you will want to establish the default Logger to be used, as well
 *    as the default Realm if you are using container-managed security.</li>
 *<li>Call <code>createEngine()</code> to create an Engine object, and then
 *     call its property setters as desired.</li>
 *<li>Call <code>createHost()</code> to create at least one virtual Host
 *     associated with the newly created Engine, and then call its property
 *     setters as desired.  After you customize this Host, add it to the
 *     corresponding Engine with <code>engine.addChild(host)</code>.</li>
 * <li>Call <code>createContext()</code> to create at least one Context
 *     associated with each newly created Host, and then call its property
 *     setters as desired.  You <strong>SHOULD</strong> create a Context with
 *     a pathname equal to a zero-length string, which will be used to process
 *     all requests not mapped to some other Context.  After you customize
 *     this Context, add it to the corresponding Host with
 *     <code>host.addChild(context)</code>.</li>
 * <li>Call <code>addEngine()</code> to attach this Engine to the set of
 *     defined Engines for this object.</li>
 * <li>Call <code>createConnector()</code> to create at least one TCP/IP
 *     connector, and then call its property setters as desired.</li>
 * <li>Call <code>addConnector()</code> to attach this Connector to the set
 *     of defined Connectors for this object.  The added Connector will use
 *     the most recently added Engine to process its received requests.</li>
 * <li>Repeat the above series of steps as often as required (although there
 *     will typically be only one Engine instance created).</li>
 * <li>Call <code>start()</code> to initiate normal operations of all the
 *     attached components.</li>
 * </ul>    
 *  
 */

public class Embedded extends StandardService implements Lifecycle{

	 private static Log log = LogFactory.getLog(Embedded.class);
	 
	// ------------------------- Constructors -------------------------
	public Embedded() {
		
	}
	
	

	 // ---------------------- Instance Variables ----------------------
	 /**
     * Is naming enabled ?
     */
    protected boolean useNaming = true;


    /**
     * Is standard streams redirection enabled ?
     */
    protected boolean redirectStreams = true;


    /**
     * The set of Engines that have been deployed in this server.  Normally
     * there will only be one.
     */
    protected Engine engines[] = new Engine[0];
    
    
    
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    /**
     * Has this component been started yet?
     */
    protected boolean started = false;

    /**
     * Use await.
     */
    protected boolean await = false;
    
    
    // -------------------------- Properties --------------------------
    public void setAwait(boolean b) {
        await = b;
    }

    public boolean isAwait() {
        return await;
    }
    
    
    public void setCatalinaHome( String s ) {
        System.setProperty( "catalina.home", s);
    }

    public void setCatalinaBase( String s ) {
        System.setProperty( "catalina.base", s);
    }

    public String getCatalinaHome() {
        return System.getProperty("catalina.home");
    }

    public String getCatalinaBase() {
        return System.getProperty("catalina.base");
    }
    
    // ------------------------- Public Methods -------------------------
    
}
