package My.jasper.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class JspServlet extends HttpServlet{

	// Logger
    private Log log = LogFactory.getLog(JspServlet.class);
    
    private ServletContext context;
    private ServletConfig config;
    
    
    
	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
    
    
    
    
}
