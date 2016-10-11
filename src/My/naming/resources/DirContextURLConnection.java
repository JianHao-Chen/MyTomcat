package My.naming.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import My.tomcat.util.http.FastHttpDateFormat;

/**
 * Connection to a JNDI directory context.
 */


public class DirContextURLConnection extends URLConnection{

	// ------------------ Constructors ------------------ 
	
	public DirContextURLConnection(DirContext context, URL url) {
		super(url);
		if (context == null)
	    	throw new IllegalArgumentException
	        	("Directory context can't be null");
		
		this.context = context;
	}
	
	
	// ------------------- Instance Variables ------------------- 
	
	/**
     * Directory context.
     */
    protected DirContext context;
    
    
    /**
     * Associated resource.
     */
    protected Resource resource;
    
    
    /**
     * Associated DirContext.
     */
    protected DirContext collection;
    
    
    /**
     * Other unknown object.
     */
    protected Object object;
    
    
    /**
     * Attributes.
     */
    protected Attributes attributes;
    
    
    /**
     * Date.
     */
    protected long date;

	// ----------------------- Properties -----------------------
    
    /**
     * Connect to the DirContext, and retrive the bound object, as well as
     * its attributes. If no object is bound with the name specified in the
     * URL, then an IOException is thrown.
     * 
     * @throws IOException Object not found
     */
    public void connect()
        throws IOException {
        
        if (!connected) {
            
            try {
                date = System.currentTimeMillis();
                String path = getURL().getFile();
                if (context instanceof ProxyDirContext) {
                    ProxyDirContext proxyDirContext = 
                        (ProxyDirContext) context;
                    String hostName = proxyDirContext.getHostName();
                    String contextName = proxyDirContext.getContextName();
                    if (hostName != null) {
                        if (!path.startsWith("/" + hostName + "/"))
                            return;
                        path = path.substring(hostName.length()+ 1);
                    }
                    if (contextName != null) {
                        if (!path.startsWith(contextName + "/")) {
                            return;
                        } else {
                            path = path.substring(contextName.length());
                        }
                    }
                }
                object = context.lookup(path);
                attributes = context.getAttributes(path);
                if (object instanceof Resource)
                    resource = (Resource) object;
                if (object instanceof DirContext)
                    collection = (DirContext) object;
            } catch (NamingException e) {
                // Object not found
            }
            
            connected = true;
            
        }
        
    }
    
    
    
    
    /**
     * Return the content length value.
     */
    public int getContentLength() {
        return getHeaderFieldInt(ResourceAttributes.CONTENT_LENGTH, -1);
    }
    
    
    /**
     * Return the content type value.
     */
    public String getContentType() {
        return getHeaderField(ResourceAttributes.CONTENT_TYPE);
    }
    
    
    /**
     * Return the last modified date.
     */
    public long getDate() {
        return date;
    }
    
    
    /**
     * Return the last modified date.
     */
    public long getLastModified() {

        if (!connected) {
            // Try to connect (silently)
            try {
                connect();
            } catch (IOException e) {
            }
        }

        if (attributes == null)
            return 0;

        Attribute lastModified = 
            attributes.get(ResourceAttributes.LAST_MODIFIED);
        if (lastModified != null) {
            try {
                Date lmDate = (Date) lastModified.get();
                return lmDate.getTime();
            } catch (Exception e) {
            }
        }

        return 0;
    }
    

    protected String getHeaderValueAsString(Object headerValue) {
        if (headerValue == null) return null;
        if (headerValue instanceof Date) {
            // return date strings (ie Last-Modified) in HTTP format, rather
            // than Java format
            return FastHttpDateFormat.formatDate(
                    ((Date)headerValue).getTime(), null);
        }
        return headerValue.toString();
    }


    /**
     * Returns an unmodifiable Map of the header fields.
     */
    public Map getHeaderFields() {

      if (!connected) {
          // Try to connect (silently)
          try {
              connect();
          } catch (IOException e) {
          }
      }

      if (attributes == null)
          return (Collections.EMPTY_MAP);

      HashMap headerFields = new HashMap(attributes.size());
      NamingEnumeration attributeEnum = attributes.getIDs();
      try {
          while (attributeEnum.hasMore()) {
              String attributeID = (String)attributeEnum.next();
              Attribute attribute = attributes.get(attributeID);
              if (attribute == null) continue;
              ArrayList attributeValueList = new ArrayList(attribute.size());
              NamingEnumeration attributeValues = attribute.getAll();
              while (attributeValues.hasMore()) {
                  Object attrValue = attributeValues.next();
                  attributeValueList.add(getHeaderValueAsString(attrValue));
              }
              attributeValueList.trimToSize(); // should be a no-op if attribute.size() didn't lie
              headerFields.put(attributeID, Collections.unmodifiableList(attributeValueList));
          }
      } catch (NamingException ne) {
            // Shouldn't happen
      }

      return Collections.unmodifiableMap(headerFields);

    }
    
    
    /**
     * Returns the name of the specified header field.
     */
    public String getHeaderField(String name) {

        if (!connected) {
            // Try to connect (silently)
            try {
                connect();
            } catch (IOException e) {
            }
        }
        
        if (attributes == null)
            return (null);

        NamingEnumeration attributeEnum = attributes.getIDs();
        try {
            while (attributeEnum.hasMore()) {
                String attributeID = (String)attributeEnum.next();
                if (attributeID.equalsIgnoreCase(name)) {
                    Attribute attribute = attributes.get(attributeID);
                    if (attribute == null) return null;
                    Object attrValue = attribute.get(attribute.size()-1);
                    return getHeaderValueAsString(attrValue);
                }
            }
        } catch (NamingException ne) {
            // Shouldn't happen
        }

        return (null);
        
    }
    
    
    /**
     * Get object content.
     */
    public Object getContent()
        throws IOException {
        
        if (!connected)
            connect();
        
        if (resource != null)
            return getInputStream();
        if (collection != null)
            return collection;
        if (object != null)
            return object;
        
        throw new FileNotFoundException(
                getURL() == null ? "null" : getURL().toString());
        
    }
    
    
    /**
     * Get object content.
     */
    public Object getContent(Class[] classes)
        throws IOException {
        
        Object object = getContent();
        
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].isInstance(object))
                return object;
        }
        
        return null;
        
    }
    
    
    /**
     * Get input stream.
     */
    public InputStream getInputStream() 
        throws IOException {
        
        if (!connected)
            connect();
        
        if (resource == null) {
            throw new FileNotFoundException(
                    getURL() == null ? "null" : getURL().toString());
        } else {
            // Reopen resource
            try {
                resource = (Resource) context.lookup(getURL().getFile());
            } catch (NamingException e) {
            }
        }
        
        return (resource.streamContent());
        
    }
    
    
   


    // --------------------------------------------------------- Public Methods
    
    
    /**
     * List children of this collection. The names given are relative to this
     * URI's path. The full uri of the children is then : path + "/" + name.
     */
    public Enumeration list()
        throws IOException {
        
        if (!connected) {
            connect();
        }
        
        if ((resource == null) && (collection == null)) {
            throw new FileNotFoundException(
                    getURL() == null ? "null" : getURL().toString());
        }
        
        Vector result = new Vector();
        
        if (collection != null) {
            try {
                NamingEnumeration enumeration = context.list(getURL().getFile());
                while (enumeration.hasMoreElements()) {
                    NameClassPair ncp = (NameClassPair) enumeration.nextElement();
                    result.addElement(ncp.getName());
                }
            } catch (NamingException e) {
                // Unexpected exception
                throw new FileNotFoundException(
                        getURL() == null ? "null" : getURL().toString());
            }
        }
        
        return result.elements();
        
    }
    
    
}
