package My.catalina;

import java.io.IOException;

import javax.naming.directory.DirContext;
import javax.servlet.ServletException;
import My.catalina.connector.Request;
import My.catalina.connector.Response;

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
     * Return a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     */
    public String getName();


    /**
     * Set a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     *
     * @param name New name of this container
     *
     * @exception IllegalStateException if this Container has already been
     *  added to the children of a parent Container (after which the name
     *  may not be changed)
     */
    public void setName(String name);
    
    
    
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
    
    
    
    /**
     * Add a container event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addContainerListener(ContainerListener listener);
    
    
    
    /**
     * Remove a container event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeContainerListener(ContainerListener listener);
    
    
    /**
     * Add a new child Container to those associated with this Container,
     * if supported.  Prior to adding this Container to the set of children,
     * the child's <code>setParent()</code> method must be called, with this
     * Container as an argument.  This method may thrown an
     * <code>IllegalArgumentException</code> if this Container chooses not
     * to be attached to the specified Container, in which case it is not added
     *
     * @param child New child Container to be added
     *
     * @exception IllegalArgumentException if this exception is thrown by
     *  the <code>setParent()</code> method of the child Container
     * @exception IllegalArgumentException if the new child does not have
     *  a name unique from that of existing children of this Container
     * @exception IllegalStateException if this Container does not support
     *  child Containers
     */
    public void addChild(Container child);
    
    
    
    
    /**
     * Return the JMX name associated with this container.
     */
    public String getObjectName();
    
    
    
    /**
     * Return the Loader with which this Container is associated.  If there is
     * no associated Loader, return the Loader associated with our parent
     * Container (if any); otherwise, return <code>null</code>.
     */
    public Loader getLoader();


    /**
     * Set the Loader with which this Container is associated.
     *
     * @param loader The newly associated loader
     */
    public void setLoader(Loader loader);
    
    
    
    /**
     * Return the Resources with which this Container is associated.  If there
     * is no associated Resources object, return the Resources associated with
     * our parent Container (if any); otherwise return <code>null</code>.
     */
    public DirContext getResources();


    /**
     * Set the Resources object with which this Container is associated.
     *
     * @param resources The newly associated Resources
     */
    public void setResources(DirContext resources);
    
    
    /**
     * Return the Manager with which this Container is associated.  If there is
     * no associated Manager, return the Manager associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Manager getManager();


    /**
     * Set the Manager with which this Container is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager);
    
    
    
    /**
     * Return the Cluster with which this Container is associated.  If there is
     * no associated Cluster, return the Cluster associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Cluster getCluster();


    /**
     * Set the Cluster with which this Container is associated.
     *
     * @param cluster the Cluster with which this Container is associated.
     */
    public void setCluster(Cluster cluster);
    
    
    
    
    /**
     * Return the Pipeline object that manages the Valves associated with
     * this Container.
     */
    public Pipeline getPipeline();
    
    
    
    /**
     * Return the child Container, associated with this Container, with
     * the specified name (if any); otherwise, return <code>null</code>
     *
     * @param name Name of the child Container to be retrieved
     */
    public Container findChild(String name);


    /**
     * Return the set of children Containers associated with this Container.
     * If this Container has no children, a zero-length array is returned.
     */
    public Container[] findChildren();

    
    
    /**
     * Process the specified Request, and generate the corresponding Response,
     * according to the design of this particular Container.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @exception IOException if an input/output error occurred while
     *  processing
     * @exception ServletException if a ServletException was thrown
     *  while processing this request
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException;
    
    
    
    
    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess();
    
    
    /**
     * Get the delay between the invocation of the backgroundProcess method on
     * this container and its children. Child containers will not be invoked
     * if their delay value is not negative (which would mean they are using 
     * their own thread). Setting this to a positive value will cause 
     * a thread to be spawn. After waiting the specified amount of time, 
     * the thread will invoke the executePeriodic method on this container 
     * and all its children.
     */
    public int getBackgroundProcessorDelay();
    
}
