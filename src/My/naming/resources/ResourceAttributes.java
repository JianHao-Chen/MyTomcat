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
