package My.catalina.core;

import My.catalina.Engine;
import My.catalina.Service;
import My.catalina.Valve;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

/**
 * Standard implementation of the <b>Engine</b> interface.  Each
 * child container must be a Host implementation to process the specific
 * fully qualified host name of that virtual host.
 */

public class StandardEngine extends ContainerBase implements Engine{

	private static Log log = LogFactory.getLog(StandardEngine.class);
	
	// --------------------- Constructors ---------------------
	
	public StandardEngine() {
		super();
        pipeline.setBasic(new StandardEngineValve());
	}
	
	
	 // ---------------- Instance Variables ----------------
	
	
	 /**
     * The <code>Service</code> that owns this Engine, if any.
     */
    private Service service = null;
	
	
	
	 // ------------------- Properties -------------------
	
	 /**
     * Return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService() {

        return (this.service);

    }


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service) {
        this.service = service;
    }

}
