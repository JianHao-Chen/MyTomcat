package My.catalina;

/**
 * A <b>Container</b> is an object that can execute requests received from
 * a client, and return responses based on those requests. A Container may
 * optionally support a pipeline of Valves that process the request in an
 * order configured at runtime, by implementing the <b>Pipeline</b> interface
 * as well.
 * 
 * Containers will exist at several conceptual levels within Catalina. It can be
 * Engine,Host,Context,Wrapper. But : 
 * 	A given deployment of Catalina need not include Containers at all of the
 * 	levels described above.
 * 
 *  A Container may also be associated with a number of support components
 * that provide functionality which might be shared (by attaching it to a
 * parent Container) or individually customized.  The following support
 * components are currently recognized:
 * <ul>
 * <li><b>Loader</b> - Class loader to use for integrating new Java classes
 *     for this Container into the JVM in which Catalina is running.
 * <li><b>Logger</b> - Implementation of the <code>log()</code> method
 *     signatures of the <code>ServletContext</code> interface.
 * <li><b>Manager</b> - Manager for the pool of Sessions associated with
 *     this Container.
 * <li><b>Realm</b> - Read-only interface to a security domain, for
 *     authenticating user identities and their corresponding roles.
 * <li><b>Resources</b> - JNDI directory context enabling access to static
 *     resources, enabling custom linkages to existing server components when
 *     Catalina is embedded in a larger server.
 * </ul>
 * 
 * 
 */

public interface Container {

	 // ----------------------------------------------------- Manifest Constants


    /**
     * The ContainerEvent event type sent when a child container is added
     * by <code>addChild()</code>.
     */
    public static final String ADD_CHILD_EVENT = "addChild";


    /**
     * The ContainerEvent event type sent when a Mapper is added
     * by <code>addMapper()</code>.
     */
    public static final String ADD_MAPPER_EVENT = "addMapper";


    /**
     * The ContainerEvent event type sent when a valve is added
     * by <code>addValve()</code>, if this Container supports pipelines.
     */
    public static final String ADD_VALVE_EVENT = "addValve";


    /**
     * The ContainerEvent event type sent when a child container is removed
     * by <code>removeChild()</code>.
     */
    public static final String REMOVE_CHILD_EVENT = "removeChild";


    /**
     * The ContainerEvent event type sent when a Mapper is removed
     * by <code>removeMapper()</code>.
     */
    public static final String REMOVE_MAPPER_EVENT = "removeMapper";


    /**
     * The ContainerEvent event type sent when a valve is removed
     * by <code>removeValve()</code>, if this Container supports pipelines.
     */
    public static final String REMOVE_VALVE_EVENT = "removeValve";


    // ------------------------------------------------------------- Properties

    
    /**
     * Return the Container for which this Container is a child, if there is
     * one.  If there is no defined parent, return <code>null</code>.
     */
    public Container getParent();


    /**
     * Set the parent Container to which this Container is being added as a
     * child.  This Container may refuse to become attached to the specified
     * Container by throwing an exception.
     *
     * @param container Container to which this Container is being added
     *  as a child
     *
     * @exception IllegalArgumentException if this Container refuses to become
     *  attached to the specified Container
     */
    public void setParent(Container container);
    
    
    
    /**
     * Return the parent class loader (if any) for web applications.
     */
    public ClassLoader getParentClassLoader();


    /**
     * Set the parent class loader (if any) for web applications.
     * This call is meaningful only <strong>before</strong> a Loader has
     * been configured, and the specified value (if non-null) should be
     * passed as an argument to the class loader constructor.
     *
     * @param parent The new parent class loader
     */
    public void setParentClassLoader(ClassLoader parent);
    
    
    
}
