package My.naming;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

public class NameParserImpl 
implements NameParser {


// ----------------------------------------------------- Instance Variables


// ----------------------------------------------------- NameParser Methods


/**
 * Parses a name into its components.
 * 
 * @param name The non-null string name to parse
 * @return A non-null parsed form of the name using the naming convention 
 * of this parser.
 */
public Name parse(String name)
    throws NamingException {
    return new CompositeName(name);
}


}

