package My.catalina.core;

import java.io.Serializable;
import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

public class ApplicationFilterConfig 
	implements FilterConfig, Serializable{

	private static My.juli.logging.Log log =
		My.juli.logging.LogFactory.getLog(ApplicationFilterConfig.class);

	
	
	
	
	
	
	@Override
	public String getFilterName() {
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
	
	// --------------------- Constructors ---------------------
	
	
	
}
