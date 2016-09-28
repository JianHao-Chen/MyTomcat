package My.naming.resources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

public class ResourceAttributes implements Attributes{

	
	/**
     * Last modification date.
     */
    public static final String LAST_MODIFIED = "getlastmodified";
	
    
    /**
     * ETag.
     * 
     *  means Entity Tag , use to identify the Entity has been 
     *  modified since last request.
     */
    public static final String ETAG = "getetag";
    
	
    
    /**
     * HTTP date format.
     */
    protected static final SimpleDateFormat format =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    
    /**
     * Date formats using for Date parsing.
     */
    protected static final SimpleDateFormat formats[] = {
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    };
    
	// --------------------- Constructors ---------------------


	/**
     * Default constructor.
     */
    public ResourceAttributes() {
    }
    
    
    /**
     * Merges with another attribute set.
     */
    public ResourceAttributes(Attributes attributes) {
        this.attributes = attributes;
    }
    
    
	// ------------------- Instance Variables -------------------
    
    /**
     * External attributes.
     */
    protected Attributes attributes = null;


    /**
     * Strong ETag.
     */
    protected String strongETag = null;
    
    /**
     * Weak ETag.
     */
    protected String weakETag = null;
    
    
    /**
     * MIME type.
     */
    protected String mimeType = null;
    
    
    /**
     * Content length.
     */
    protected long contentLength = -1;
    
    /**
     * Creation time.
     */
    protected long creation = -1;
    
    
    /**
     * Last modified time.
     */
    protected long lastModified = -1;

    /**
     * Last modified date.
     */
    protected Date lastModifiedDate = null;
    
    
    /**
     * Last modified date in HTTP format.
     */
    protected String lastModifiedHttp = null;
    
    
    /**
     * Get last modified time.
     * 
     * @return lastModified time value
     */
    public long getLastModified() {
    	if (lastModified != -1L)
            return lastModified;
        if (lastModifiedDate != null)
            return lastModifiedDate.getTime();
        if (attributes != null) {
            Attribute attribute = attributes.get(LAST_MODIFIED);
            if (attribute != null) {
                try {
                    Object value = attribute.get();
                    if (value instanceof Long) {
                        lastModified = ((Long) value).longValue();
                    } else if (value instanceof Date) {
                        lastModified = ((Date) value).getTime();
                        lastModifiedDate = (Date) value;
                    } else {
                        String lastModifiedDateValue = value.toString();
                        Date result = null;
                        // Parsing the HTTP Date
                        for (int i = 0; (result == null) && 
                                 (i < formats.length); i++) {
                            try {
                                result = 
                                    formats[i].parse(lastModifiedDateValue);
                            } catch (ParseException e) {
                                ;
                            }
                        }
                        if (result != null) {
                            lastModified = result.getTime();
                            lastModifiedDate = result;
                        }
                    }
                } catch (NamingException e) {
                    ; // No value for the attribute
                }
            }
        }
        return lastModified;
    }
    
    
    /**
     * Get lastModified date.
     * 
     * @return LastModified date value
     */
    public Date getLastModifiedDate() {
    	if (lastModifiedDate != null)
    		return lastModifiedDate;
    	
    	if (lastModified != -1L) {
            lastModifiedDate = new Date(lastModified);
            return lastModifiedDate;
        }
    	
    	return null;
    }
    
    
    
    /**
     * @return Returns the lastModifiedHttp.
     */
    public String getLastModifiedHttp() {
        if (lastModifiedHttp != null)
            return lastModifiedHttp;
        Date modifiedDate = getLastModifiedDate();
        if (modifiedDate == null) {
          //  modifiedDate = getCreationDate();
        }
        if (modifiedDate == null) {
            modifiedDate = new Date();
        }
        synchronized (format) {
            lastModifiedHttp = format.format(modifiedDate);
        }
        return lastModifiedHttp;
    }
    
    
    /**
     * @param lastModifiedHttp The lastModifiedHttp to set.
     */
    public void setLastModifiedHttp(String lastModifiedHttp) {
        this.lastModifiedHttp = lastModifiedHttp;
    }
    
    
    
    /**
     * @return Returns the mimeType.
     */
    public String getMimeType() {
        return mimeType;
    }
    
    
    /**
     * @param mimeType The mimeType to set.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    
    
    
    /**
     * Get ETag.
     * 
     * @return strong ETag if available, else weak ETag. 
     */
    public String getETag() {
    	
    	String result = null;
    	if (attributes != null) {
    		Attribute attribute = attributes.get(ETAG);
            if (attribute != null) {
                try {
                    result = attribute.get().toString();
                } catch (NamingException e) {
                    ; // No value for the attribute
                }
            }
    	}
    	
    	if (result == null) {
    		
    		if (strongETag != null) {
    			// The strong ETag must always be calculated by the resources
                result = strongETag;
    		}
    		else {
    			// The weakETag is contentLength + lastModified
    			if (weakETag == null) {
    				 long contentLength = getContentLength();
    				 long lastModified = getLastModified();
    				 if ((contentLength >= 0) || (lastModified >= 0)) {
                         weakETag = "W/\"" + contentLength + "-" +
                                    lastModified + "\"";
                     }
    			}
    			result = weakETag;
    		}
    	}
    	
    	 return result;
    }
    
    
    /**
     * Get content length.
     * 
     * @return content length value
     */
    public long getContentLength() {
    	
    	if (contentLength != -1L)
            return contentLength;
    	
    	return 0;
    	
    }
    
    
    

	@Override
	public boolean isCaseIgnored() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Attribute get(String attrID) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public NamingEnumeration<? extends Attribute> getAll() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public NamingEnumeration<String> getIDs() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Attribute put(String attrID, Object val) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Attribute put(Attribute attr) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Attribute remove(String attrID) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
     * Clone the attributes object (WARNING: fake cloning).
     */
    public Object clone() {
        return this;
    }
    
}
