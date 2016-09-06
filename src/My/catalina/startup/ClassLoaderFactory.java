package My.catalina.startup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;
import My.catalina.loader.StandardClassLoader;

/**
 * <p>Utility class for building class loaders for Catalina.  The factory
 * method requires the following parameters in order to build a new class
 * loader (with suitable defaults in all cases):</p>
 * <ul>
 * <li>A set of directories containing unpacked classes (and resources)
 *     that should be included in the class loader's
 *     repositories.</li>
 * <li>A set of directories containing classes and resources in JAR files.
 *     Each readable JAR file discovered in these directories will be
 *     added to the class loader's repositories.</li>
 * <li><code>ClassLoader</code> instance that should become the parent of
 *     the new class loader.</li>
 * </ul>
 */


public final class ClassLoaderFactory {

	private static Log log = LogFactory.getLog(ClassLoaderFactory.class);
	 
	protected static final Integer IS_DIR = new Integer(0);
    protected static final Integer IS_JAR = new Integer(1);
    protected static final Integer IS_GLOB = new Integer(2);
    protected static final Integer IS_URL = new Integer(3);

    
    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param locations Array of strings containing class directories, jar files,
     *  jar directories or URLS that should be added to the repositories of
     *  the class loader. The type is given by the member of param types.
     * @param types Array of types for the members of param locations.
     *  Possible values are IS_DIR (class directory), IS_JAR (single jar file),
     *  IS_GLOB (directory of jar files) and IS_URL (URL).
     * @param parent Parent class loader for the new class loader, or
     *  <code>null</code> for the system class loader.
     *
     * @exception Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(String locations[],
                                                Integer types[],
                                                ClassLoader parent)
        throws Exception {
    	
    	 if (log.isDebugEnabled())
             log.debug("Creating new class loader");
    	 
    	// Construct the "class path" for this class loader
         Set<URL> set = new LinkedHashSet<URL>();
         
         if (locations != null && types != null && locations.length == types.length) {
        	 for (int i = 0; i < locations.length; i++){
        		 
        		 String location = locations[i];
        		 
        		 if ( types[i] == IS_URL ) {
        			 URL url = new URL(location);
        			 set.add(url);
        		 }
        		 else if ( types[i] == IS_DIR ) {
        			 File directory = new File(location);
        			 directory = directory.getCanonicalFile();
        			 if (!validateFile(directory, IS_DIR))
                         continue;
        			 
        			 URL url = directory.toURI().toURL();
        			 set.add(url);
        		 }
        		 else if ( types[i] == IS_JAR ) {
        			 File file=new File(location);
                     file = file.getCanonicalFile();
                     if (!validateFile(file, IS_JAR))
                         continue;
                     
                     URL url = file.toURI().toURL();
                     set.add(url);
        		 }
        		 else if ( types[i] == IS_GLOB ) {
        			 File directory=new File(location);
                     directory = directory.getCanonicalFile();
                     if (!validateFile(directory, IS_GLOB))
                         continue;
                                    
                     String filenames[] = directory.list();
                     for (int j = 0; j < filenames.length; j++) {
                    	 String filename = filenames[j].toLowerCase();
                         if (!filename.endsWith(".jar"))
                             continue;
                         
                         File file = new File(directory, filenames[j]);
                         file = file.getCanonicalFile();
                         if (!validateFile(file, IS_JAR))
                             continue;

                         URL url = file.toURI().toURL();
                         set.add(url);
                     }
        		 }
        	 }
         }
         
         // Construct the class loader itself
         URL[] array = set.toArray(new URL[set.size()]);
         
         StandardClassLoader classLoader = null;
         
         if (parent == null)
             classLoader = new StandardClassLoader(array);
         else
             classLoader = new StandardClassLoader(array, parent);
         return (classLoader);
         
    }
    
    
    
    private static boolean validateFile(File file,
    		Integer type) throws IOException {
    	
    	if (type == IS_DIR || type == IS_GLOB) {
    		
    		if (!file.exists() || !file.isDirectory() || !file.canRead()) 
    			return false;

    	}
    	else if (type == IS_JAR) {
    		
    		 if (!file.exists() || !file.canRead()) 
    			 return false; 
    	}
    	
    	return true;
    }
}
