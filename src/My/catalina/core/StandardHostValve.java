package My.catalina.core;

import java.io.IOException;

import javax.servlet.ServletException;

import My.catalina.connector.Request;
import My.catalina.connector.Response;
import My.catalina.valves.ValveBase;
import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class StandardHostValve extends ValveBase {

	private static Log log = LogFactory.getLog(StandardHostValve.class);
	
	
	
	 // ------------- Public Methods -------------
	public final void invoke(Request request, Response response)
    throws IOException, ServletException {
		
	}
	
	
}
