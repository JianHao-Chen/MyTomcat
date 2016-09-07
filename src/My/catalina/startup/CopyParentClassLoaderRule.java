package My.catalina.startup;

import java.lang.reflect.Method;

import org.xml.sax.Attributes;

import My.catalina.Container;
import My.tomcat.util.digester.Rule;

public class CopyParentClassLoaderRule extends Rule{
	 // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this Rule.
     */
    public CopyParentClassLoaderRule() {
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {

       
        Container child = (Container) digester.peek(0);
        Object parent = digester.peek(1);
        Method method =
            parent.getClass().getMethod("getParentClassLoader", new Class[0]);
        ClassLoader classLoader =
            (ClassLoader) method.invoke(parent, new Object[0]);
        child.setParentClassLoader(classLoader);

    }
}
