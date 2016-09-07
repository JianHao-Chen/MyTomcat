package My.tomcat.util.digester;



import org.xml.sax.Attributes;

import My.catalina.Executor;
import My.catalina.Service;
import My.catalina.connector.Connector;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class ConnectorCreateRule extends Rule{

	protected static Log log = LogFactory.getLog(ConnectorCreateRule.class);
	
	
	
	  // ---------------- ----------------- Public Methods
	
	/**
     * Process the beginning of this element.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(Attributes attributes) throws Exception {
    	Service svc = (Service)digester.peek();
    	
    	Executor ex = null;
        if ( attributes.getValue("executor")!=null ) {
        	// handle this latter
        }
    	
        Connector con = new Connector(attributes.getValue("protocol"));
        
        digester.push(con);
    }
    
    
    
    /**
     * Process the end of this element.
     */
    public void end() throws Exception {
        Object top = digester.pop();
    }
}
