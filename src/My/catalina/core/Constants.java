package My.catalina.core;

public class Constants {
	
	 public static final String Package = "My.catalina.core";
	    public static final int MAJOR_VERSION = 2;
	    public static final int MINOR_VERSION = 5;

	    public static final String JSP_SERVLET_CLASS =
	        "My.jasper.servlet.JspServlet";
	    public static final String JSP_SERVLET_NAME = "jsp";
	    public static final String PRECOMPILE = 
	        System.getProperty("My.jasper.Constants.PRECOMPILE",
	                "jsp_precompile");
}
