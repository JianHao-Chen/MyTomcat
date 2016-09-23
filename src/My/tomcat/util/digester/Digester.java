package My.tomcat.util.digester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;

/**
 * <p>A <strong>Digester</strong> processes an XML input stream by matching a
 * series of element nesting patterns to execute Rules that have been added
 * prior to the start of parsing.  This package was inspired by the
 * <code>XmlMapper</code> class that was part of Tomcat 3.0 and 3.1,
 * but is organized somewhat differently.</p>
 *
 * <p>See the <a href="package-summary.html#package_description">Digester
 * Developer Guide</a> for more information.</p>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - A single Digester instance may
 * only be used within the context of a single thread at a time, and a call
 * to <code>parse()</code> must be completed before another can be initiated
 * even from the same thread.</p>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - A bug in Xerces 2.0.2 prevents
 * the support of XML schema. You need Xerces 2.1/2.3 and up to make
 * this class working with XML schema</p>
 */

public class Digester extends DefaultHandler{

	// ----------------------- Static Fields -----------------------
	
	
	// ----------------------- Constructors -----------------------
	
	/**
     * Construct a new Digester with default properties.
     */
    public Digester() {
        super();
    }
    
    /**
     * Construct a new Digester, allowing a SAXParser to be passed in.
     */
    public Digester(SAXParser parser) {
        super();
        this.parser = parser;
    }
    
    /**
     * Construct a new Digester, allowing an XMLReader to be passed in. 
     */
    public Digester(XMLReader reader) {
        super();
        this.reader = reader;
    }
    
    
    
    // ------------------- Instance Variables -------------------
    
    /**
     * The body text of the current element.
     */
    protected StringBuffer bodyText = new StringBuffer();
    
    
    /**
     * The stack of body text string buffers for surrounding elements.
     */
    protected ArrayStack bodyTexts = new ArrayStack();
    
    
    /**
     * Stack whose elements are List objects, each containing a list of
     * Rule objects as returned from Rules.getMatch(). As each xml element
     * in the input is entered, the matching rules are pushed onto this
     * stack. After the end tag is reached, the matches are popped again.
     * The depth of is stack is therefore exactly the same as the current
     * "nesting" level of the input xml. 
     */
    protected ArrayStack matches = new ArrayStack(10);
    
    
    /**
     * The class loader to use for instantiating application objects.
     * If not specified, the context class loader, or the class loader
     * used to load Digester itself, is used, based on the value of the
     * <code>useContextClassLoader</code> variable.
     */
    protected ClassLoader classLoader = null;

    
    /**
     * Has this Digester been configured yet.
     */
    protected boolean configured = false;
    
    
    /**
     * The EntityResolver used by the SAX parser. By default it use this class
     */
    protected EntityResolver entityResolver;
    
    
    /**
     * The parameters stack being utilized by CallMethodRule and
     * CallParamRule rules.
     */
    protected ArrayStack params = new ArrayStack();
    
    
    
    /**
     * The Locator associated with our parser.
     */
    protected Locator locator = null;
    
    
    
    /**
     * The public identifier of the DTD we are currently parsing under
     * (if any).
     */
    protected String publicId = null;
    
    
    /**
     * The XMLReader used to parse digester rules.
     */
    protected XMLReader reader = null;
    
    
    /**
     * The URLs of entityValidator that have been registered, keyed by the public
     * identifier that corresponds.
     */
    protected HashMap entityValidator = new HashMap();
    
    
    /**
     * The application-supplied error handler that is notified when parsing
     * warnings, errors, or fatal errors occur.
     */
    protected ErrorHandler errorHandler = null;
    
    
    
    /**
     * Fake attributes map (attributes are often used for object creation).
     */
    protected Map<Class, List<String>> fakeAttributes = null;
    
    
    
    /**
     * The SAXParserFactory that is created the first time we need it.
     */
    protected SAXParserFactory factory = null;
    
    /**
     * The current match pattern for nested element processing.
     */
    protected String match = "";
    
    /**
     * The <code>Rules</code> implementation containing our collection of
     * <code>Rule</code> instances and associated matching policy.  If not
     * established before the first rule is added, a default implementation
     * will be provided.
     */
    protected Rules rules = null;
    
    
    /**
     * Do we want a "namespace aware" parser.
     */
    protected boolean namespaceAware = false;
    
    
    /**
     * Do we want to use a validating parser.
     */
    protected boolean validating = false;
    
    
    /**
     * Warn on missing attributes and elements.
     */
    protected boolean rulesValidation = false;
    
    
    
    /**
     * The SAXParser we will use to parse the input stream.
     */
    protected SAXParser parser = null;
    
    
    /**
     * Do we want to use the Context ClassLoader when loading classes
     * for instantiating new objects.  Default is <code>false</code>.
     */
    protected boolean useContextClassLoader = false;
    
    
    /**
     * The XML schema to use for validating an XML instance.
     */
    protected String schemaLocation = null;
    
    
    /**
     * The object stack being constructed.
     */
    protected ArrayStack stack = new ArrayStack();
    
    
    /**
     * The "root" element of the stack (in other words, the last object
     * that was popped.
     */
    protected Object root = null;
    
    
    /** Stacks used for interrule communication, 
     * indexed by name String */
    private HashMap stacksByName = new HashMap();
    
    
    /**
     * The Log to which most logging calls will be made.
     */
    protected Log log =
        LogFactory.getLog("My.tomcat.util.digester.Digester");


   
    
	// ---------------------------- Properties ----------------------------
    
    /**
     * Return the class loader to be used for instantiating application objects
     * when required.  This is determined based upon the following rules:
     * <ul>
     * <li>The class loader set by <code>setClassLoader()</code>, if any</li>
     * <li>The thread context class loader, if it exists and the
     *     <code>useContextClassLoader</code> property is set to true</li>
     * <li>The class loader used to load the Digester class itself.
     * </ul>
     */
    public ClassLoader getClassLoader() {
    	
    	 if (this.classLoader != null) {
             return (this.classLoader);
         }
         if (this.useContextClassLoader) {
             ClassLoader classLoader =
                     Thread.currentThread().getContextClassLoader();
             if (classLoader != null) {
                 return (classLoader);
             }
         }
         return (this.getClass().getClassLoader());
    }
    
    /**
     * Set the class loader to be used for instantiating application objects
     * when required.
     *
     * @param classLoader The new class loader to use, or <code>null</code>
     *  to revert to the standard rules
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    
    /**
     * Return the current depth of the element stack.
     */
    public int getCount() {
        return (stack.size());
    }
    
    
    
    /**
     * Return the validating parser flag.
     */
    public boolean getValidating() {
        return (this.validating);
    }

    /**
     * Set the validating parser flag.  This must be called before
     * <code>parse()</code> is called the first time.
     *
     * @param validating The new validating parser flag.
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    
    
    /**
     * Return the rules validation flag.
     */
    public boolean getRulesValidation() {
        return (this.rulesValidation);
    }

    /**
     * Set the rules validation flag.  This must be called before
     * <code>parse()</code> is called the first time.
     *
     * @param rulesValidation The new rules validation flag.
     */
    public void setRulesValidation(boolean rulesValidation) {
        this.rulesValidation = rulesValidation;
    }
    
    
    
    /**
     * Set the publid id of the current file being parse.
     * @param publicId the DTD/Schema public's id.
     */
    public void setPublicId(String publicId){
        this.publicId = publicId;
    }
    
    
    /**
     * Return the public identifier of the DTD we are currently
     * parsing under, if any.
     */
    public String getPublicId() {

        return (this.publicId);

    }
    
    
    
    /**
     * Return the "namespace aware" flag for parsers we create.
     */
    public boolean getNamespaceAware() {

        return (this.namespaceAware);

    }


    /**
     * Set the "namespace aware" flag for parsers we create.
     *
     * @param namespaceAware The new "namespace aware" flag
     */
    public void setNamespaceAware(boolean namespaceAware) {

        this.namespaceAware = namespaceAware;

    }
    
    
    
    /**
     * Return the boolean as to whether the context classloader should be used.
     */
    public boolean getUseContextClassLoader() {

        return useContextClassLoader;

    }


    /**
     * Determine whether to use the Context ClassLoader (the one found by
     * calling <code>Thread.currentThread().getContextClassLoader()</code>)
     * to resolve/load classes that are defined in various rules.  If not
     * using Context ClassLoader, then the class-loading defaults to
     * using the calling-class' ClassLoader.
     *
     * @param use determines whether to use Context ClassLoader.
     */
    public void setUseContextClassLoader(boolean use) {

        useContextClassLoader = use;

    }
    
    
    
    /**
     * Return the name of the XML element that is currently being processed.
     */
    public String getCurrentElementName() {

        String elementName = match;
        int lastSlash = elementName.lastIndexOf('/');
        if (lastSlash >= 0) {
            elementName = elementName.substring(lastSlash + 1);
        }
        return (elementName);
    }
    
    
    /**
     * Return the <code>Rules</code> implementation object containing our
     * rules collection and associated matching policy.  If none has been
     * established, a default implementation will be created and returned.
     */
    public Rules getRules() {

        if (this.rules == null) {
            this.rules = new RulesBase();
            this.rules.setDigester(this);
        }
        return (this.rules);

    }
    

    /**
     * Return the error handler for this Digester.
     */
    public ErrorHandler getErrorHandler() {
        return (this.errorHandler);
    }

    /**
     * Set the error handler for this Digester.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    
    /**
     * Return the SAXParserFactory we will use, creating one if necessary.
     */
    public SAXParserFactory getFactory() {

        if (factory == null) {
            factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(namespaceAware);
            factory.setValidating(validating);
        }
        return (factory);
    }
    
    
    
 // ----------------------- Object Stack Methods -----------------------
    
    /**
     * Clear the current contents of the object stack.
     */
    public void clear() {

        match = "";
        bodyTexts.clear();
        params.clear();
        publicId = null;
        stack.clear();
        log = null;
  //      saxLog = null;
        configured = false;
    }
    
    
    /**
     * Return the top object on the stack without removing it.  If there are
     * no objects on the stack, return <code>null</code>.
     */
    public Object peek() {

        try {
            return (stack.peek());
        } catch (EmptyStackException e) {
            log.warn("Empty stack (returning null)");
            return (null);
        }
    }
    
    
    /**
     * Return the n'th object down the stack, where 0 is the top element
     * and [getCount()-1] is the bottom element.  If the specified index
     * is out of range, return <code>null</code>.
     */
    public Object peek(int n) {

        try {
            return (stack.peek(n));
        } catch (EmptyStackException e) {
            log.warn("Empty stack (returning null)");
            return (null);
        }
    }
    
    
    /**
     * Pop the top object off of the stack, and return it.  If there are
     * no objects on the stack, return <code>null</code>.
     */
    public Object pop() {

        try {
            return (stack.pop());
        } catch (EmptyStackException e) {
            log.warn("Empty stack (returning null)");
            return (null);
        }

    }
    
    
    public void reset() {
        root = null;
        setErrorHandler(null);
        clear();
    }
    
    
    /**
     * Push a new object onto the top of the object stack.
     *
     * @param object The new object
     */
    public void push(Object object) {

        if (stack.size() == 0) {
            root = object;
        }
        stack.push(object);

    }

    /**
     * Pushes the given object onto the stack with the given name.
     * If no stack already exists with the given name then one will be created.
     * 
     * @param stackName the name of the stack onto which the object should be pushed
     * @param value the Object to be pushed onto the named stack.
     *
     * @since 1.6
     */
    public void push(String stackName, Object value) {
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack == null) {
            namedStack = new ArrayStack();
            stacksByName.put(stackName, namedStack);
        }
        namedStack.push(value);
    }
    
    /**
     * <p>Pops (gets and removes) the top object from the stack with the given name.</p>
     *
     * <p><strong>Note:</strong> a stack is considered empty
     * if no objects have been pushed onto it yet.</p>
     * 
     * @param stackName the name of the stack from which the top value is to be popped
     * @return the top <code>Object</code> on the stack or or null if the stack is either 
     * empty or has not been created yet
     * @throws EmptyStackException if the named stack is empty
     *
     * @since 1.6
     */
    public Object pop(String stackName) {
        Object result = null;
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack == null) {
            if (log.isDebugEnabled()) {
                log.debug("Stack '" + stackName + "' is empty");
            }
            throw new EmptyStackException();
            
        } else {
        
            result = namedStack.pop();
        }
        return result;
    }
    
    /**
     * <p>Gets the top object from the stack with the given name.
     * This method does not remove the object from the stack.
     * </p>
     * <p><strong>Note:</strong> a stack is considered empty
     * if no objects have been pushed onto it yet.</p>
     *
     * @param stackName the name of the stack to be peeked
     * @return the top <code>Object</code> on the stack or null if the stack is either 
     * empty or has not been created yet
     * @throws EmptyStackException if the named stack is empty 
     *
     * @since 1.6
     */
    public Object peek(String stackName) {
        Object result = null;
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack == null ) {
            if (log.isDebugEnabled()) {
                log.debug("Stack '" + stackName + "' is empty");
            }        
            throw new EmptyStackException();
        
        } else {
        
            result = namedStack.peek();
        }
        return result;
    }
    
    
    /**
     * <p>Pop the top object off of the parameters stack, and return it.  If there are
     * no objects on the stack, return <code>null</code>.</p>
     *
     * <p>The parameters stack is used to store <code>CallMethodRule</code> parameters. 
     * See {@link #params}.</p>
     */
    public Object popParams() {

        try {
            if (log.isTraceEnabled()) {
                log.trace("Popping params");
            }
            return (params.pop());
        } catch (EmptyStackException e) {
            log.warn("Empty stack (returning null)");
            return (null);
        }

    }
    
    
    /**
     * <p>Return the top object on the parameters stack without removing it.  If there are
     * no objects on the stack, return <code>null</code>.</p>
     *
     * <p>The parameters stack is used to store <code>CallMethodRule</code> parameters. 
     * See {@link #params}.</p>
     */
    public Object peekParams() {

        try {
            return (params.peek());
        } catch (EmptyStackException e) {
            log.warn("Empty stack (returning null)");
            return (null);
        }

    }
    

    /**
     * <p>Is the stack with the given name empty?</p>
     * <p><strong>Note:</strong> a stack is considered empty
     * if no objects have been pushed onto it yet.</p>
     * @param stackName the name of the stack whose emptiness 
     * should be evaluated
     * @return true if the given stack if empty 
     *
     * @since 1.6
     */
    public boolean isEmpty(String stackName) {
        boolean result = true;
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack != null ) {
            result = namedStack.isEmpty();
        }
        return result;
    }
    
    /**
     * When the Digester is being used as a SAXContentHandler, 
     * this method allows you to access the root object that has been
     * created after parsing.
     * 
     * @return the root object that has been created after parsing
     *  or null if the digester has not parsed any XML yet.
     */
    public Object getRoot() {
        return root;
    }
    
    
    /**
     * Determine if an attribute is a fake attribute.
     */
    public boolean isFakeAttribute(Object object, String name) {

        if (fakeAttributes == null) {
            return false;
        }
        List<String> result = fakeAttributes.get(object.getClass());
        if (result == null) {
            result = fakeAttributes.get(Object.class);
        }
        if (result == null) {
            return false;
        } else {
            return result.contains(name);
        }

    }
    
    
    
    /**
     * Parse the content of the specified file using this Digester.  Returns
     * the root element from the object stack (if any).
     *
     * @param file File containing the XML data to be parsed
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXException if a parsing exception occurs
     */
    public Object parse(File file) throws IOException, SAXException {

        configure();
        InputSource input = new InputSource(new FileInputStream(file));
        input.setSystemId("file://" + file.getAbsolutePath());
        getXMLReader().parse(input);
        return (root);

    }   
    
    
    /**
     * Parse the content of the specified input source using this Digester.
     * Returns the root element from the object stack (if any).
     *
     * @param input Input source containing the XML data to be parsed
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXException if a parsing exception occurs
     */
    public Object parse(InputSource input) throws IOException, SAXException {
 
        configure();
        getXMLReader().parse(input);
        return (root);

    }
    
    
    /**
     * Return the XMLReader to be used for parsing the input document.
     *
     * FIX ME: there is a bug in JAXP/XERCES that prevent the use of a 
     * parser that contains a schema with a DTD.
     * @exception SAXException if no XMLReader can be instantiated
     */
    public XMLReader getXMLReader() throws SAXException {
        if (reader == null){
            reader = getParser().getXMLReader();
        }        
                               
        reader.setDTDHandler(this);           
        reader.setContentHandler(this);        
        
        if (entityResolver == null){
            reader.setEntityResolver(this);
        } else {
            reader.setEntityResolver(entityResolver);           
        }
        
        reader.setErrorHandler(this);
        return reader;
    }
    
    
    /**
     * Return the SAXParser we will use to parse the input stream.  If there
     * is a problem creating the parser, return <code>null</code>.
     */
    public SAXParser getParser() {

        // Return the parser we already created (if any)
        if (parser != null) {
            return (parser);
        }

        // Create a new parser
        try {
            
             parser = getFactory().newSAXParser();
            
        } catch (Exception e) {
            log.error("Digester.getParser: ", e);
            return (null);
        }

        return (parser);

    }
    
    
    
    // ---------------------- Rule Methods ----------------------
    
    
    /**
     * Add an "object create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param className Default Java class name to be created
     * @param attributeName Attribute name that optionally overrides
     *  the default Java class name to be created
     * @see ObjectCreateRule
     */
    public void addObjectCreate(String pattern, String className,
                                String attributeName) {

        addRule(pattern,
                new ObjectCreateRule(className, attributeName));

    }
    
    
    
    /**
     * Add an "object create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param className Java class name to be created
     * @see ObjectCreateRule
     */
    public void addObjectCreate(String pattern, String className) {

        addRule(pattern,
                new ObjectCreateRule(className));

    }

    
    
    /**
     * Add a "set properties" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @see SetPropertiesRule
     */
    public void addSetProperties(String pattern) {

        addRule(pattern,
                new SetPropertiesRule());

    }
    
    
    /**
     * Add a "set next" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the parent element
     * @param paramType Java class name of the expected parameter type
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter)
     * @see SetNextRule
     */
    public void addSetNext(String pattern, String methodName,
                           String paramType) {

        addRule(pattern,
                new SetNextRule(methodName, paramType));

    }
    
    
    
    /**
     * Register a set of Rule instances defined in a RuleSet.
     *
     * @param ruleSet The RuleSet instance to configure from
     */
    public void addRuleSet(RuleSet ruleSet) {

        ruleSet.addRuleInstances(this);

    }

    
    /**
     * <p>Register a new Rule matching the specified pattern.
     * This method sets the <code>Digester</code> property on the rule.</p>
     *
     * @param pattern Element matching pattern
     * @param rule Rule to be registered
     */
    public void addRule(String pattern, Rule rule) {

        rule.setDigester(this);
        getRules().add(pattern, rule);

    }
    
    
    protected void configure() {
    	configured = true;
    }
    
    
    
    /**
     * Add an "call method" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @param paramCount Number of expected parameters (or zero
     *  for a single parameter from the body of this element)
     * @see CallMethodRule
     */
    public void addCallMethod(String pattern, String methodName,
                              int paramCount) {

        addRule(pattern,
                new CallMethodRule(methodName, paramCount));

    }
    
    
    
    /**
     * Add a "call parameter" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param paramIndex Zero-relative parameter index to set
     *  (from the body of this element)
     * @see CallParamRule
     */
    public void addCallParam(String pattern, int paramIndex) {

        addRule(pattern,
                new CallParamRule(paramIndex));

    }
    
    
    
    
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes list)
    	throws SAXException {
    	
    	
    	if(qName.equals("Service")){
    		System.out.println("Service");
    	}
    	
    	if(qName.equals("Engine")){
    		System.out.println("Engine");
    	}
    	
    	
    	if(qName.equals("Connector")){
    		System.out.println("Connector");
    	}
    	
    	if(qName.equals("Context")){
    		System.out.println("Context");
    	}
    	
    	if(qName.equals("web-app")){
    		System.out.println("web-app");
    	}

    	if(qName.equals("servlet")){
    		System.out.println("servlet");
    	}
    	
    	if(qName.equals("servlet-name")){
    		System.out.println("servlet-name");
    	}
    	
    	if(qName.equals("servlet-name")){
    		System.out.println("servlet-name");
    	}
    	
    	if(qName.equals("init-param")){
    		System.out.println("init-param");
    	}
    	
    	
    	if(qName.equals("param-name")){
    		System.out.println("param-name");
    	}
    	
    	if(qName.equals("param-value")){
    		System.out.println("param-value");
    	}

    	if(qName.equals("load-on-startup")){
    		System.out.println("load-on-startup");
    	}
    	
    	
    	 // the actual element name is either in localName or qName, depending 
        // on whether the parser is namespace aware
        String name = localName;
        if ((name == null) || (name.length() < 1)) {
            name = qName;
        }
        
        
        // Compute the current matching rule
        StringBuffer sb = new StringBuffer(match);
        if (match.length() > 0) {
            sb.append('/');
        }
        
        sb.append(name);
        
        match = sb.toString();
        
     // Fire "begin" events for all relevant rules
        List rules = getRules().match(namespaceURI, match);
        matches.push(rules);
        if ((rules != null) && (rules.size() > 0)) {
        	for (int i = 0; i < rules.size(); i++) {
        		
        		try {
                    Rule rule = (Rule) rules.get(i);
                    
                    rule.begin(namespaceURI, name, list);
        		}
        		catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
        }else{
        	System.out.println("No rules found matching");
        }
        
    }
    
    
    
    public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
    	
    	// the actual element name is either in localName or qName, depending 
        // on whether the parser is namespace aware
        String name = localName;
        if ((name == null) || (name.length() < 1)) {
            name = qName;
        }
        
        List rules = (List) matches.pop();
        
        
        // Fire "end" events for all relevant rules in reverse order
        if (rules != null) {
        	
        	for (int i = 0; i < rules.size(); i++) {
                int j = (rules.size() - i) - 1;
                try {
                	
                    Rule rule = (Rule) rules.get(j);
                    
                    rule.end(namespaceURI, name);
                }catch (Exception e) {
                	
                }
        	}
        }
        
        
        // Recover the previous match expression
        int slash = match.lastIndexOf('/');
        if (slash >= 0) {
            match = match.substring(0, slash);
        } else {
            match = "";
        }
        
    }
    
    
    
    
 // ----------------------------------------------- EntityResolver Methods

    /**
     * Set the <code>EntityResolver</code> used by SAX when resolving
     * public id and system id.
     * This must be called before the first call to <code>parse()</code>.
     * @param entityResolver a class that implement the <code>EntityResolver</code> interface.
     */
    public void setEntityResolver(EntityResolver entityResolver){
        this.entityResolver = entityResolver;
    }
    
    
    /**
     * Return the Entity Resolver used by the SAX parser.
     * @return Return the Entity Resolver used by the SAX parser.
     */
    public EntityResolver getEntityResolver(){
        return entityResolver;
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
                
       
        
        if (publicId != null)
            this.publicId = publicId;
                                       
        // Has this system identifier been registered?
        String entityURL = null;
        if (publicId != null) {
            entityURL = (String) entityValidator.get(publicId);
        }
         
        // Redirect the schema location to a local destination
        if (schemaLocation != null && entityURL == null && systemId != null){
            entityURL = (String)entityValidator.get(systemId);
        } 

        if (entityURL == null) { 
            if (systemId == null) {
                // cannot resolve
                if (log.isDebugEnabled()) {
                    log.debug(" Cannot resolve entity: '" + entityURL + "'");
                }
                return (null);
                
            } else {
                // try to resolve using system ID
                if (log.isDebugEnabled()) {
                    log.debug(" Trying to resolve using system ID '" + systemId + "'");
                } 
                entityURL = systemId;
            }
        }
        
        // Return an input source to our alternative URL
        if (log.isDebugEnabled()) {
            log.debug(" Resolving to alternate DTD '" + entityURL + "'");
        }  
        
        try {
            return (new InputSource(entityURL));
        } catch (Exception e) {
            throw createSAXException(e);
        }
    }
    
    
    
    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs
     *
     * @return the new exception
     */
    public SAXException createSAXException(String message, Exception e) {
        if ((e != null) &&
            (e instanceof InvocationTargetException)) {
            Throwable t = ((InvocationTargetException) e).getTargetException();
            if ((t != null) && (t instanceof Exception)) {
                e = (Exception) t;
            }
        }
        if (locator != null) {
            String error = "Error at (" + locator.getLineNumber() + ", " +
                    locator.getColumnNumber() + ": " + message;
            if (e != null) {
                return new SAXParseException(error, locator, e);
            } else {
                return new SAXParseException(error, locator);
            }
        }
        log.error("No Locator!");
        if (e != null) {
            return new SAXException(message, e);
        } else {
            return new SAXException(message);
        }
    }

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs
     *
     * @return the new exception
     */
    public SAXException createSAXException(Exception e) {
        if (e instanceof InvocationTargetException) {
            Throwable t = ((InvocationTargetException) e).getTargetException();
            if ((t != null) && (t instanceof Exception)) {
                e = (Exception) t;
            }
        }
        return createSAXException(e.getMessage(), e);
    }

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs
     *
     * @return the new exception
     */
    public SAXException createSAXException(String message) {
        return createSAXException(message, null);
    }
    
    
    
    /**
     * Return the XML Schema URI used for validating an XML instance.
     */
    public String getSchema() {

        return (this.schemaLocation);

    }


    /**
     * Set the XML Schema URI used for validating a XML Instance.
     *
     * @param schemaLocation a URI to the schema.
     */
    public void setSchema(String schemaLocation){

        this.schemaLocation = schemaLocation;

    }   
 
    
    
    // ----------------------- Package Methods -----------------------
    
    /**
     * Return the set of rules that apply to the specified match position.
     * The selected rules are those that match exactly, or those rules
     * that specify a suffix match and the tail of the rule matches the
     * current match position.  Exact matches have precedence over
     * suffix matches, then (among suffix matches) the longest match
     * is preferred.
     */
    List getRules(String match) {

        return (getRules().match(match));

    }
    
}
