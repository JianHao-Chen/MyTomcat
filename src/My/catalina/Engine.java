package My.catalina;

/**
* An <b>Engine</b> is a Container that represents the entire Catalina servlet
* engine.  It is useful in the following types of scenarios:
* <ul>
 * <li>You wish to use Interceptors that see every single request processed
 *     by the entire engine.
 * <li>You wish to run Catalina in with a standalone HTTP connector, but still
 *     want support for multiple virtual hosts.
 * </ul>
 * 
 * 
 * 
*/
	

public interface Engine extends Container{

	
	
	 /**
     * Return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService();


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service);
}
