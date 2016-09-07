package My.catalina.core;

import My.catalina.Engine;
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

}
