package My.catalina.session;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Globals;
import My.catalina.Loader;
import My.catalina.Session;
import My.catalina.Store;

public final class FileStore 
	extends StoreBase implements Store {
	
	
	public FileStore(){
		System.out.println("========FileStore========");
	}
	
	
	// ---------------------- Constants ----------------------
	/**
     * The extension to use for serialized session filenames.
     */
    private static final String FILE_EXT = ".session";
    
    
	// -------------------- Instance Variables --------------------
    
    /**
     * The pathname of the directory in which Sessions are stored.
     * This may be an absolute pathname, or a relative path that is
     * resolved against the temporary work directory for this application.
     */
    private String directory = ".";


    /**
     * A File representing the directory in which Sessions are stored.
     */
    private File directoryFile = null;
    
    
	// ---------------------- Properties --------------------------
    
    /**
     * Return the directory path for this Store.
     */
    public String getDirectory() {

        return (directory);

    }


    /**
     * Set the directory path for this Store.
     *
     * @param path The new directory path
     */
    public void setDirectory(String path) {

        String oldDirectory = this.directory;
        this.directory = path;
        this.directoryFile = null;
      
    }

    
    
    
    
    
	

	@Override
	public int getSize() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	
	
	/**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store.  If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */
	public String[] keys() throws IOException {
		// Acquire the list of files in our storage directory
        File file = directory();
        if (file == null) {
            return (new String[0]);
        }

        String files[] = file.list();
        
        
        if((files == null) || (files.length < 1)) {
            return (new String[0]);
        }
        
        // Build and return the list of session identifiers
        ArrayList list = new ArrayList();
        int n = FILE_EXT.length();
        
        for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith(FILE_EXT)) {
                list.add(files[i].substring(0, files[i].length() - n));
            }
        }
        
        return ((String[]) list.toArray(new String[list.size()]));
	}
	
	
	

	/**
     * Load and return the Session associated with the specified session
     * identifier from this Store, without removing it.  If there is no
     * such stored Session, return <code>null</code>.
     *
     * @param id Session identifier of the session to load
     *
     * @exception ClassNotFoundException if a deserialization error occurs
     * @exception IOException if an input/output error occurs
     */
	public Session load(String id) throws ClassNotFoundException, IOException {
		// Open an input stream to the specified pathname, if any
        File file = file(id);
        if (file == null) {
            return (null);
        }

        if (! file.exists()) {
            return (null);
        }
        
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        
        try {
        	fis = new FileInputStream(file.getAbsolutePath());
        	BufferedInputStream bis = new BufferedInputStream(fis);
        	ois = new ObjectInputStream(bis);
        }
        catch (FileNotFoundException e) {
        	
        }
        catch (IOException e) {
        	if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
                ois = null;
            }
            throw e;
        }
        
        
        
        try {
        	StandardSession session =
                (StandardSession) manager.createEmptySession();
        	
        	session.readObjectData(ois);
        	session.setManager(manager);
        	return (session);
        }
        finally {
            // Close the input stream
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
            }
        }
        
        
	}

	@Override
	public void remove(String id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() throws IOException {
		// TODO Auto-generated method stub
		
	}

	/**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */
	public void save(Session session) throws IOException {
		// Open an output stream to the specified pathname, if any
        File file = file(session.getIdInternal());
        if (file == null) {
            return;
        }
        
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        
        try {
        	fos = new FileOutputStream(file.getAbsolutePath());
        	oos = new ObjectOutputStream(new BufferedOutputStream(fos));
        }
        catch (IOException e) {
        	if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
            }
            throw e;
        }
        
        
        
        try {
        	((StandardSession)session).writeObjectData(oos);
        }
        finally {
            oos.close();
        }
	}
	
	
	
	
	
	
	// --------------------------- Private Methods ---------------------
	
	
	/**
     * Return a File object representing the pathname to our
     * session persistence directory, if any.  The directory will be
     * created if it does not already exist.
     */
    private File directory() {
    	
    	if (this.directory == null) {
            return (null);
        }
    	
    	if (this.directoryFile != null) {
            // NOTE:  Race condition is harmless, so do not synchronize
            return (this.directoryFile);
        }
    	
    	File file = new File(this.directory);
    	if (!file.isAbsolute()) {
    		Container container = manager.getContainer();
    		if (container instanceof Context) {
    			
    			ServletContext servletContext =
                    ((Context) container).getServletContext();
    			
    			File work = (File)
                	servletContext.getAttribute(Globals.WORK_DIR_ATTR);
    			
    			file = new File(work, this.directory);
    		}
    		else {
                throw new IllegalArgumentException
                    ("Parent Container is not a Context");
            }
    	}
    	
    	if (!file.exists() || !file.isDirectory()) {
    		file.delete();
            file.mkdirs();
    	}
    	this.directoryFile = file;
        return (file);
    	
    }
	
	
	
	/**
     * Return a File object representing the pathname to our
     * session persistence file, if any.
     *
     * @param id The ID of the Session to be retrieved. This is
     *    used in the file naming.
     */
    private File file(String id) {
    	
    	if (this.directory == null) {
            return (null);
        }
        String filename = id + FILE_EXT;
        File file = new File(directory(), filename);
        return (file);
    }

}
