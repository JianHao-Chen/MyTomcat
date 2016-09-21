package My.catalina.core;


import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import My.catalina.Wrapper;

/**
 * Standard implementation of the <b>Wrapper</b> interface that represents
 * an individual servlet definition.  No child Containers are allowed, and
 * the parent Container must be a Context.
 */

public class StandardWrapper extends ContainerBase
implements ServletConfig, Wrapper{

	@Override
	public String getServletName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	protected void registerJMX(StandardContext ctx) {
		
	}

}
