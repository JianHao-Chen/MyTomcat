package My.catalina.util;

import java.util.HashMap;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import My.tomcat.util.digester.Digester;

public class SchemaResolver implements EntityResolver{
	 /**
     * The disgester instance for which this class is the entity resolver.
     */
    protected Digester digester;


    /**
     * The URLs of dtds and schemas that have been registered, keyed by the
     * public identifier that corresponds.
     */
    protected HashMap entityValidator = new HashMap();


    /**
     * The public identifier of the DTD we are currently parsing under
     * (if any).
     */
    protected String publicId = null;


    /**
     * Extension to make the difference between DTD and Schema.
     */
    protected String schemaExtension = "xsd";


    /**
     * Create a new <code>EntityResolver</code> that will redirect
     * all remote dtds and schema to a locat destination.
     * @param digester The digester instance.
     */
    public SchemaResolver(Digester digester) {
        this.digester = digester;
    }


    /**
     * Register the specified DTD/Schema URL for the specified public
     * identifier. This must be called before the first call to
     * <code>parse()</code>.
     *
     * When adding a schema file (*.xsd), only the name of the file
     * will get added. If two schemas with the same name are added,
     * only the last one will be stored.
     *
     * @param publicId Public identifier of the DTD to be resolved
     * @param entityURL The URL to use for reading this DTD
     */
     public void register(String publicId, String entityURL) {
         String key = publicId;
         if (publicId.indexOf(schemaExtension) != -1)
             key = publicId.substring(publicId.lastIndexOf('/')+1);
         entityValidator.put(key, entityURL);
     }


    /**
     * Resolve the requested external entity.
     *
     * @param publicId The public identifier of the entity being referenced
     * @param systemId The system identifier of the entity being referenced
     *
     * @exception SAXException if a parsing exception occurs
     *
     */
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {

        if (publicId != null) {
            this.publicId = publicId;
            digester.setPublicId(publicId);
        }

        // Has this system identifier been registered?
        String entityURL = null;
        if (publicId != null) {
            entityURL = (String) entityValidator.get(publicId);
        }

        // Redirect the schema location to a local destination
        String key = null;
        if (entityURL == null && systemId != null) {
            key = systemId.substring(systemId.lastIndexOf('/')+1);
            entityURL = (String)entityValidator.get(key);
        }

        if (entityURL == null) {
           return (null);
        }

        try {
            return (new InputSource(entityURL));
        } catch (Exception e) {
            throw new SAXException(e);
        }

    }

}
