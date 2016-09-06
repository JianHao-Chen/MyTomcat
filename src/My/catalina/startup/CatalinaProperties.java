package My.catalina.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class CatalinaProperties {

	private static Log log = LogFactory.getLog(CatalinaProperties.class);
	
	private static Properties properties = null;
	
	static {

        loadProperties();

    }
	
	
	 /**
     * Return specified property value.
     */
    public static String getProperty(String name) {
	
        return properties.getProperty(name);

    }
	
	
	 private static void loadProperties() {
		 InputStream is = null;
		 Throwable error = null;
		 
		 try{
			 File home = new File(getCatalinaBase());
			 File conf = new File(home, "conf");
			 File properties = new File(conf, "catalina.properties");
			 is = new FileInputStream(properties);
		 }catch (Throwable t) {
             // Ignore
         }
		 
		 //is is null, handle here
		 if(is==null){
			 //
		 }
		 
		 if (is != null) {
			 try {
				 properties = new Properties();
	             properties.load(is);
	             is.close(); 
			 }
			 catch (Throwable t) {
	                error = t;
	          }
		 }
		 
		 if ((is == null) || (error != null)) {
	            // Do something
	            log.warn("Failed to load catalina.properties", error);
	            // That's fine - we have reasonable defaults.
	            properties=new Properties();
	     }
		 
		 
		// Register the properties as system properties
		 Enumeration enumeration = properties.propertyNames();
		 while (enumeration.hasMoreElements()) {
			 String name = (String) enumeration.nextElement();
	            String value = properties.getProperty(name);
	            if (value != null) {
	                System.setProperty(name, value);
	            }
		 }
		 
	 }
	 
	 
	 /**
	 * Get the value of the catalina.home environment variable.
	 */
	    private static String getCatalinaHome() {
	        return System.getProperty("catalina.home",
	                                  System.getProperty("user.dir"));
	    }
	 
	 /**
	  * Get the value of the catalina.base environment variable.
	  */
	    private static String getCatalinaBase() {
	        return System.getProperty("catalina.base", getCatalinaHome());
	    }
}
