package My.tomcat.util.http.mapper;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.omg.CORBA.Context;

import My.catalina.Host;
import My.tomcat.util.buf.Ascii;
import My.tomcat.util.buf.CharChunk;
import My.tomcat.util.buf.MessageBytes;

public final class Mapper {

	// ------------------- Instance Variables -------------------
	
	/**
     * Array containing the virtual hosts definitions.
     */
    protected Host[] hosts = new Host[0];
    
    /**
     * Default host name.
     */
    protected String defaultHostName = null;
    
    
    /**
     * Context associated with this wrapper, used for wrapper mapping.
     */
    protected Context context = new Context();
    
    
    public Mapper(){
        System.out.println("");
    }
    
	// --------------------- Public Methods ---------------------
    
    /**
     * Get default host.
     *
     * @return Default host name
     */
    public String getDefaultHostName() {
        return defaultHostName;
    }


    /**
     * Set default host.
     *
     * @param defaultHostName Default host name
     */
    public void setDefaultHostName(String defaultHostName) {
        this.defaultHostName = defaultHostName;
    }
    
    /**
     * Find a map elemnt given its name in a sorted array of map elements.
     * This will return the index for the closest inferior or equal item in the
     * given array.
     */
    private static final int findIgnoreCase(MapElement[] map, CharChunk name) {
        return findIgnoreCase(map, name, name.getStart(), name.getEnd());
    }

    
    /**
     * Compare given char chunk with String ignoring case.
     * Return -1, 0 or +1 if inferior, equal, or superior to the String.
     */
    private static final int compareIgnoreCase(CharChunk name, int start, int end,
                                     String compareTo) {
        int result = 0;
        char[] c = name.getBuffer();
        int len = compareTo.length();
        if ((end - start) < len) {
            len = end - start;
        }
        for (int i = 0; (i < len) && (result == 0); i++) {
            if (Ascii.toLower(c[i + start]) > Ascii.toLower(compareTo.charAt(i))) {
                result = 1;
            } else if (Ascii.toLower(c[i + start]) < Ascii.toLower(compareTo.charAt(i))) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length() > (end - start)) {
                result = -1;
            } else if (compareTo.length() < (end - start)) {
                result = 1;
            }
        }
        return result;
    }
    
    

    /**
     * Find a map elemnt given its name in a sorted array of map elements.
     * This will return the index for the closest inferior or equal item in the
     * given array.
     */
    private static final int findIgnoreCase(MapElement[] map, CharChunk name,
                                  int start, int end) {

        int a = 0;
        int b = map.length - 1;

        // Special cases: -1 and 0
        if (b == -1) {
            return -1;
        }
        if (compareIgnoreCase(name, start, end, map[0].name) < 0 ) {
            return -1;
        }         
        if (b == 0) {
            return 0;
        }

        int i = 0;
        while (true) {
            i = (b + a) / 2;
            int result = compareIgnoreCase(name, start, end, map[i].name);
            if (result == 1) {
                a = i;
            } else if (result == 0) {
                return i;
            } else {
                b = i;
            }
            if ((b - a) == 1) {
                int result2 = compareIgnoreCase(name, start, end, map[b].name);
                if (result2 < 0) {
                    return a;
                } else {
                    return b;
                }
            }
        }

    }

    
    
    
    /**
     * Map the specified URI.
     */
    private final void internalMap(CharChunk host, CharChunk uri,
                                   MappingData mappingData)
        throws Exception {

        uri.setLimit(-1);

        Context[] contexts = null;
        Context context = null;
        int nesting = 0;

        // Virtual host mapping
        if (mappingData.host == null) {
            Host[] hosts = this.hosts;
            int pos = findIgnoreCase(hosts, host);
            if ((pos != -1) && (host.equalsIgnoreCase(hosts[pos].name))) {
                mappingData.host = hosts[pos].object;
                contexts = hosts[pos].contextList.contexts;
                nesting = hosts[pos].contextList.nesting;
            } else {
                if (defaultHostName == null) {
                    return;
                }
                pos = find(hosts, defaultHostName);
                if ((pos != -1) && (defaultHostName.equals(hosts[pos].name))) {
                    mappingData.host = hosts[pos].object;
                    contexts = hosts[pos].contextList.contexts;
                    nesting = hosts[pos].contextList.nesting;
                } else {
                    return;
                }
            }
        }

        // Context mapping
        if (mappingData.context == null) {
            int pos = find(contexts, uri);
            if (pos == -1) {
                return;
            }

            int lastSlash = -1;
            int uriEnd = uri.getEnd();
            int length = -1;
            boolean found = false;
            while (pos >= 0) {
                if (uri.startsWith(contexts[pos].name)) {
                    length = contexts[pos].name.length();
                    if (uri.getLength() == length) {
                        found = true;
                        break;
                    } else if (uri.startsWithIgnoreCase("/", length)) {
                        found = true;
                        break;
                    }
                }
                if (lastSlash == -1) {
                    lastSlash = nthSlash(uri, nesting + 1);
                } else {
                    lastSlash = lastSlash(uri);
                }
                uri.setEnd(lastSlash);
                pos = find(contexts, uri);
            }
            uri.setEnd(uriEnd);

            if (!found) {
                if (contexts[0].name.equals("")) {
                    context = contexts[0];
                }
            } else {
                context = contexts[pos];
            }
            if (context != null) {
                mappingData.context = context.object;
                mappingData.contextPath.setString(context.name);
            }
        }

        // Wrapper mapping
        if ((context != null) && (mappingData.wrapper == null)) {
            internalMapWrapper(context, uri, mappingData);
        }

    }
    
    
    
    /**
     * Extension mappings.
     */
    private final void internalMapExtensionWrapper
        (Wrapper[] wrappers, CharChunk path, MappingData mappingData) {
        char[] buf = path.getBuffer();
        int pathEnd = path.getEnd();
        int servletPath = path.getOffset();
        int slash = -1;
        for (int i = pathEnd - 1; i >= servletPath; i--) {
            if (buf[i] == '/') {
                slash = i;
                break;
            }
        }
        if (slash >= 0) {
            int period = -1;
            for (int i = pathEnd - 1; i > slash; i--) {
                if (buf[i] == '.') {
                    period = i;
                    break;
                }
            }
            if (period >= 0) {
                path.setOffset(period + 1);
                path.setEnd(pathEnd);
                int pos = find(wrappers, path);
                if ((pos != -1)
                    && (path.equals(wrappers[pos].name))) {
                    mappingData.wrapperPath.setChars
                        (buf, servletPath, pathEnd - servletPath);
                    mappingData.requestPath.setChars
                        (buf, servletPath, pathEnd - servletPath);
                    mappingData.wrapper = wrappers[pos].object;
                }
                path.setOffset(servletPath);
                path.setEnd(pathEnd);
            }
        }
    }
    
    
    /**
     * Exact mapping.
     */
    private final void internalMapExactWrapper
        (Wrapper[] wrappers, CharChunk path, MappingData mappingData) {
        int pos = find(wrappers, path);
        if ((pos != -1) && (path.equals(wrappers[pos].name))) {
            mappingData.requestPath.setString(wrappers[pos].name);
            mappingData.wrapperPath.setString(wrappers[pos].name);
            mappingData.wrapper = wrappers[pos].object;
        }
    }
    
    
    /**
     * Wildcard mapping.
     */
    private final void internalMapWildcardWrapper
        (Wrapper[] wrappers, int nesting, CharChunk path, 
         MappingData mappingData) {

        int pathEnd = path.getEnd();
        int pathOffset = path.getOffset();

        int lastSlash = -1;
        int length = -1;
        int pos = find(wrappers, path);
        if (pos != -1) {
            boolean found = false;
            while (pos >= 0) {
                if (path.startsWith(wrappers[pos].name)) {
                    length = wrappers[pos].name.length();
                    if (path.getLength() == length) {
                        found = true;
                        break;
                    } else if (path.startsWithIgnoreCase("/", length)) {
                        found = true;
                        break;
                    }
                }
                if (lastSlash == -1) {
                    lastSlash = nthSlash(path, nesting + 1);
                } else {
                    lastSlash = lastSlash(path);
                }
                path.setEnd(lastSlash);
                pos = find(wrappers, path);
            }
            path.setEnd(pathEnd);
            if (found) {
                mappingData.wrapperPath.setString(wrappers[pos].name);
                if (path.getLength() > length) {
                    mappingData.pathInfo.setChars
                        (path.getBuffer(),
                         path.getOffset() + length,
                         path.getLength() - length);
                }
                mappingData.requestPath.setChars
                    (path.getBuffer(), path.getOffset(), path.getLength());
                mappingData.wrapper = wrappers[pos].object;
                mappingData.jspWildCard = wrappers[pos].jspWildCard;
            }
        }
    }
    
    
    /**
     * Wrapper mapping.
     */
    private final void internalMapWrapper(Context context, CharChunk path,
                                          MappingData mappingData)
        throws Exception {

        int pathOffset = path.getOffset();
        int pathEnd = path.getEnd();
        int servletPath = pathOffset;
        boolean noServletPath = false;

        int length = context.name.length();
        if (length != (pathEnd - pathOffset)) {
            servletPath = pathOffset + length;
        } else {
            noServletPath = true;
            path.append('/');
            pathOffset = path.getOffset();
            pathEnd = path.getEnd();
            servletPath = pathOffset+length;
        }

        path.setOffset(servletPath);

        // Rule 1 -- Exact Match
        Wrapper[] exactWrappers = context.exactWrappers;
        internalMapExactWrapper(exactWrappers, path, mappingData);

        // Rule 2 -- Prefix Match
        boolean checkJspWelcomeFiles = false;
        Wrapper[] wildcardWrappers = context.wildcardWrappers;
        if (mappingData.wrapper == null) {
            internalMapWildcardWrapper(wildcardWrappers, context.nesting, 
                                       path, mappingData);
            if (mappingData.wrapper != null && mappingData.jspWildCard) {
                char[] buf = path.getBuffer();
                if (buf[pathEnd - 1] == '/') {
                    /*
                     * Path ending in '/' was mapped to JSP servlet based on
                     * wildcard match (e.g., as specified in url-pattern of a
                     * jsp-property-group.
                     * Force the context's welcome files, which are interpreted
                     * as JSP files (since they match the url-pattern), to be
                     * considered. See Bugzilla 27664.
                     */ 
                    mappingData.wrapper = null;
                    checkJspWelcomeFiles = true;
                } else {
                    // See Bugzilla 27704
                    mappingData.wrapperPath.setChars(buf, path.getStart(),
                                                     path.getLength());
                    mappingData.pathInfo.recycle();
                }
            }
        }

        if(mappingData.wrapper == null && noServletPath) {
            // The path is empty, redirect to "/"
            mappingData.redirectPath.setChars
                (path.getBuffer(), pathOffset, pathEnd-pathOffset);
            path.setEnd(pathEnd - 1);
            return;
        }

        // Rule 3 -- Extension Match
        Wrapper[] extensionWrappers = context.extensionWrappers;
        if (mappingData.wrapper == null && !checkJspWelcomeFiles) {
            internalMapExtensionWrapper(extensionWrappers, path, mappingData);
        }

        // Rule 4 -- Welcome resources processing for servlets
        if (mappingData.wrapper == null) {
            boolean checkWelcomeFiles = checkJspWelcomeFiles;
            if (!checkWelcomeFiles) {
                char[] buf = path.getBuffer();
                checkWelcomeFiles = (buf[pathEnd - 1] == '/');
            }
            if (checkWelcomeFiles) {
                for (int i = 0; (i < context.welcomeResources.length)
                         && (mappingData.wrapper == null); i++) {
                    path.setOffset(pathOffset);
                    path.setEnd(pathEnd);
                    path.append(context.welcomeResources[i], 0,
                                context.welcomeResources[i].length());
                    path.setOffset(servletPath);

                    // Rule 4a -- Welcome resources processing for exact macth
                    internalMapExactWrapper(exactWrappers, path, mappingData);

                    // Rule 4b -- Welcome resources processing for prefix match
                    if (mappingData.wrapper == null) {
                        internalMapWildcardWrapper
                            (wildcardWrappers, context.nesting, 
                             path, mappingData);
                    }

                    // Rule 4c -- Welcome resources processing
                    //            for physical folder
                    if (mappingData.wrapper == null
                        && context.resources != null) {
                        Object file = null;
                        String pathStr = path.toString();
                        try {
                            file = context.resources.lookup(pathStr);
                        } catch(NamingException nex) {
                            // Swallow not found, since this is normal
                        }
                        if (file != null && !(file instanceof DirContext) ) {
                            internalMapExtensionWrapper(extensionWrappers,
                                                        path, mappingData);
                            if (mappingData.wrapper == null
                                && context.defaultWrapper != null) {
                                mappingData.wrapper =
                                    context.defaultWrapper.object;
                                mappingData.requestPath.setChars
                                    (path.getBuffer(), path.getStart(), 
                                     path.getLength());
                                mappingData.wrapperPath.setChars
                                    (path.getBuffer(), path.getStart(), 
                                     path.getLength());
                                mappingData.requestPath.setString(pathStr);
                                mappingData.wrapperPath.setString(pathStr);
                            }
                        }
                    }
                }

                path.setOffset(servletPath);
                path.setEnd(pathEnd);
            }
                                        
        }


        // Rule 7 -- Default servlet
        if (mappingData.wrapper == null && !checkJspWelcomeFiles) {
            if (context.defaultWrapper != null) {
                mappingData.wrapper = context.defaultWrapper.object;
                mappingData.requestPath.setChars
                    (path.getBuffer(), path.getStart(), path.getLength());
                mappingData.wrapperPath.setChars
                    (path.getBuffer(), path.getStart(), path.getLength());
            }
            // Redirection to a folder
            char[] buf = path.getBuffer();
            if (context.resources != null && buf[pathEnd -1 ] != '/') {
                Object file = null;
                String pathStr = path.toString();
                try {
                    file = context.resources.lookup(pathStr);
                } catch(NamingException nex) {
                    // Swallow, since someone else handles the 404
                }
                if (file != null && file instanceof DirContext) {
                    // Note: this mutates the path: do not do any processing 
                    // after this (since we set the redirectPath, there 
                    // shouldn't be any)
                    path.setOffset(pathOffset);
                    path.append('/');
                    mappingData.redirectPath.setChars
                        (path.getBuffer(), path.getStart(), path.getLength());
                } else {
                    mappingData.requestPath.setString(pathStr);
                    mappingData.wrapperPath.setString(pathStr);
                }
            }
        }

        path.setOffset(pathOffset);
        path.setEnd(pathEnd);

    }
    
    
    /**
     * Find the position of the last slash in the given char chunk.
     */
    private static final int lastSlash(CharChunk name) {

        char[] c = name.getBuffer();
        int end = name.getEnd();
        int start = name.getStart();
        int pos = end;

        while (pos > start) {
            if (c[--pos] == '/') {
                break;
            }
        }

        return (pos);

    }
    
    
    /**
     * Find the position of the nth slash, in the given char chunk.
     */
    private static final int nthSlash(CharChunk name, int n) {

        char[] c = name.getBuffer();
        int end = name.getEnd();
        int start = name.getStart();
        int pos = start;
        int count = 0;

        while (pos < end) {
            if ((c[pos++] == '/') && ((++count) == n)) {
                pos--;
                break;
            }
        }

        return (pos);

    }
    
    
    /**
     * Map the specified host name and URI, mutating the given mapping data.
     *
     * @param host Virtual host name
     * @param uri URI
     * @param mappingData This structure will contain the result of the mapping
     *                    operation
     */
    public void map(MessageBytes host, MessageBytes uri,
                    MappingData mappingData)
        throws Exception {

        if (host.isNull()) {
            host.getCharChunk().append(defaultHostName);
        }
        host.toChars();
        uri.toChars();
        internalMap(host.getCharChunk(), uri.getCharChunk(), mappingData);

    }
    
    
    
    /**
     * Find a map element given its name in a sorted array of map elements.
     * This will return the index for the closest inferior or equal item in the
     * given array.
     */
    private static final int find(MapElement[] map, String name) {

        int a = 0;
        int b = map.length - 1;

        // Special cases: -1 and 0
        if (b == -1) {
            return -1;
        }
        
        if (name.compareTo(map[0].name) < 0) {
            return -1;
        } 
        if (b == 0) {
            return 0;
        }

        int i = 0;
        while (true) {
            i = (b + a) / 2;
            int result = name.compareTo(map[i].name);
            if (result > 0) {
                a = i;
            } else if (result == 0) {
                return i;
            } else {
                b = i;
            }
            if ((b - a) == 1) {
                int result2 = name.compareTo(map[b].name);
                if (result2 < 0) {
                    return a;
                } else {
                    return b;
                }
            }
        }

    }
    
    
    /**
     * Insert into the right place in a sorted MapElement array, and prevent
     * duplicates.
     */
    private static final boolean insertMap
        (MapElement[] oldMap, MapElement[] newMap, MapElement newElement) {
        int pos = find(oldMap, newElement.name);
        if ((pos != -1) && (newElement.name.equals(oldMap[pos].name))) {
            return false;
        }
        System.arraycopy(oldMap, 0, newMap, 0, pos + 1);
        newMap[pos + 1] = newElement;
        System.arraycopy
            (oldMap, pos + 1, newMap, pos + 2, oldMap.length - pos - 1);
        return true;
    }
    
    
    /**
     * Insert into the right place in a sorted MapElement array.
     */
    private static final boolean removeMap
        (MapElement[] oldMap, MapElement[] newMap, String name) {
        int pos = find(oldMap, name);
        if ((pos != -1) && (name.equals(oldMap[pos].name))) {
            System.arraycopy(oldMap, 0, newMap, 0, pos);
            System.arraycopy(oldMap, pos + 1, newMap, pos,
                             oldMap.length - pos - 1);
            return true;
        }
        return false;
    }
    
    
    
    /**
     * Find a map elemnt given its name in a sorted array of map elements.
     * This will return the index for the closest inferior or equal item in the
     * given array.
     */
    private static final int find(MapElement[] map, CharChunk name) {
        return find(map, name, name.getStart(), name.getEnd());
    }


    /**
     * Find a map elemnt given its name in a sorted array of map elements.
     * This will return the index for the closest inferior or equal item in the
     * given array.
     */
    private static final int find(MapElement[] map, CharChunk name,
                                  int start, int end) {

        int a = 0;
        int b = map.length - 1;

        // Special cases: -1 and 0
        if (b == -1) {
            return -1;
        }
        
        if (compare(name, start, end, map[0].name) < 0 ) {
            return -1;
        }         
        if (b == 0) {
            return 0;
        }

        int i = 0;
        while (true) {
            i = (b + a) / 2;
            int result = compare(name, start, end, map[i].name);
            if (result == 1) {
                a = i;
            } else if (result == 0) {
                return i;
            } else {
                b = i;
            }
            if ((b - a) == 1) {
                int result2 = compare(name, start, end, map[b].name);
                if (result2 < 0) {
                    return a;
                } else {
                    return b;
                }
            }
        }

    }
    
    
    /**
     * Compare given char chunk with String.
     * Return -1, 0 or +1 if inferior, equal, or superior to the String.
     */
    private static final int compare(CharChunk name, int start, int end,
                                     String compareTo) {
        int result = 0;
        char[] c = name.getBuffer();
        int len = compareTo.length();
        if ((end - start) < len) {
            len = end - start;
        }
        for (int i = 0; (i < len) && (result == 0); i++) {
            if (c[i + start] > compareTo.charAt(i)) {
                result = 1;
            } else if (c[i + start] < compareTo.charAt(i)) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length() > (end - start)) {
                result = -1;
            } else if (compareTo.length() < (end - start)) {
                result = 1;
            }
        }
        return result;
    }

    /**
     * Return the slash count in a given string.
     */
    private static final int slashCount(String name) {
        int pos = -1;
        int count = 0;
        while ((pos = name.indexOf('/', pos + 1)) != -1) {
            count++;
        }
        return count;
    }
    
    
    /**
     * Add a new host to the mapper.
     *
     * @param name Virtual host name
     * @param host Host object
     */
    public synchronized void addHost(String name, String[] aliases,
                                     Object host) {
        Host[] newHosts = new Host[hosts.length + 1];
        Host newHost = new Host();
        ContextList contextList = new ContextList();
        newHost.name = name;
        newHost.contextList = contextList;
        newHost.object = host;
        if (insertMap(hosts, newHosts, newHost)) {
            hosts = newHosts;
        }
        for (int i = 0; i < aliases.length; i++) {
            newHosts = new Host[hosts.length + 1];
            newHost = new Host();
            newHost.name = aliases[i];
            newHost.contextList = contextList;
            newHost.object = host;
            if (insertMap(hosts, newHosts, newHost)) {
                hosts = newHosts;
            }
        }
    }
    
    
    
    /**
     * Remove a host from the mapper.
     *
     * @param name Virtual host name
     */
    public synchronized void removeHost(String name) {
        // Find and remove the old host
        int pos = find(hosts, name);
        if (pos < 0) {
            return;
        }
        Object host = hosts[pos].object;
        Host[] newHosts = new Host[hosts.length - 1];
        if (removeMap(hosts, newHosts, name)) {
            hosts = newHosts;
        }
        // Remove all aliases (they will map to the same host object)
        for (int i = 0; i < newHosts.length; i++) {
            if (newHosts[i].object == host) {
                Host[] newHosts2 = new Host[hosts.length - 1];
                if (removeMap(hosts, newHosts2, newHosts[i].name)) {
                    hosts = newHosts2;
                }
            }
        }
    }
    
    
    /**
     * Set context, used for wrapper mapping (request dispatcher).
     *
     * @param welcomeResources Welcome files defined for this context
     * @param resources Static resources of the context
     */
    public void setContext(String path, String[] welcomeResources,
                           javax.naming.Context resources) {
        context.name = path;
        context.welcomeResources = welcomeResources;
        context.resources = resources;
    }
    
    
    
    
    
    /**
     * Add a new Context to an existing Host.
     *
     * @param hostName Virtual host name this context belongs to
     * @param path Context path
     * @param context Context object
     * @param welcomeResources Welcome files defined for this context
     * @param resources Static resources of the context
     */
    public void addContext
        (String hostName, String path, Object context,
         String[] welcomeResources, javax.naming.Context resources) {

        Host[] hosts = this.hosts;
        int pos = find(hosts, hostName);
        if( pos <0 ) {
            addHost(hostName, new String[0], "");
            hosts = this.hosts;
            pos = find(hosts, hostName);
        }
        if (pos < 0) {
            ;
        }
        Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            int slashCount = slashCount(path);
            synchronized (host) {
                Context[] contexts = host.contextList.contexts;
                // Update nesting
                if (slashCount > host.contextList.nesting) {
                    host.contextList.nesting = slashCount;
                }
                Context[] newContexts = new Context[contexts.length + 1];
                Context newContext = new Context();
                newContext.name = path;
                newContext.object = context;
                newContext.welcomeResources = welcomeResources;
                newContext.resources = resources;
                if (insertMap(contexts, newContexts, newContext)) {
                    host.contextList.contexts = newContexts;
                }
            }
        }

    }
    
    
    /**
     * Remove a context from an existing host.
     *
     * @param hostName Virtual host name this context belongs to
     * @param path Context path
     */
    public void removeContext(String hostName, String path) {
        Host[] hosts = this.hosts;
        int pos = find(hosts, hostName);
        if (pos < 0) {
            return;
        }
        
        Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            synchronized (host) {
                Context[] contexts = host.contextList.contexts;
                if( contexts.length == 0 ){
                    return;
                }
                Context[] newContexts = new Context[contexts.length - 1];
                if (removeMap(contexts, newContexts, path)) {
                    host.contextList.contexts = newContexts;
                    // Recalculate nesting
                    host.contextList.nesting = 0;
                    for (int i = 0; i < newContexts.length; i++) {
                        int slashCount = slashCount(newContexts[i].name);
                        if (slashCount > host.contextList.nesting) {
                            host.contextList.nesting = slashCount;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Adds a wrapper to the given context.
     *
     * @param context The context to which to add the wrapper
     * @param path Wrapper mapping
     * @param wrapper The Wrapper object
     * @param jspWildCard true if the wrapper corresponds to the JspServlet
     * and the mapping path contains a wildcard; false otherwise
     */
    protected void addWrapper(Context context, String path, Object wrapper,
                              boolean jspWildCard) {

        synchronized (context) {
            Wrapper newWrapper = new Wrapper();
            newWrapper.object = wrapper;
            newWrapper.jspWildCard = jspWildCard;
            if (path.endsWith("/*")) {  //如果path的map是通配符类型的 
                // Wildcard wrapper
                newWrapper.name = path.substring(0, path.length() - 2);
                Wrapper[] oldWrappers = context.wildcardWrappers;
                Wrapper[] newWrappers =
                    new Wrapper[oldWrappers.length + 1];
                if (insertMap(oldWrappers, newWrappers, newWrapper)) {
                    context.wildcardWrappers = newWrappers;
                    int slashCount = slashCount(newWrapper.name);
                    if (slashCount > context.nesting) {
                        context.nesting = slashCount;
                    }
                }
            } else if (path.startsWith("*.")) { //表示是扩展名的mapper
                // Extension wrapper
                newWrapper.name = path.substring(2);
                Wrapper[] oldWrappers = context.extensionWrappers;
                Wrapper[] newWrappers =
                    new Wrapper[oldWrappers.length + 1];
                if (insertMap(oldWrappers, newWrappers, newWrapper)) {
                    context.extensionWrappers = newWrappers;
                }
            } else if (path.equals("/")) {     // 表示是默认的wrapper
                // Default wrapper
                newWrapper.name = "";
                context.defaultWrapper = newWrapper;
            } else {                        //最后就是精确的map了 
                // Exact wrapper
                newWrapper.name = path;
                Wrapper[] oldWrappers = context.exactWrappers;
                Wrapper[] newWrappers =
                    new Wrapper[oldWrappers.length + 1];
                if (insertMap(oldWrappers, newWrappers, newWrapper)) {
                    context.exactWrappers = newWrappers;
                }
            }
        }
    }
    
    
    public void addWrapper(String hostName, String contextPath, String path,
            Object wrapper, boolean jspWildCard) {
		Host[] hosts = this.hosts;
		int pos = find(hosts, hostName);
		if (pos < 0) {
			return;
		}
		
		Host host = hosts[pos];
		if (host.name.equals(hostName)) {
		Context[] contexts = host.contextList.contexts;
		
		int pos2 = find(contexts, contextPath);
		
		if( pos2<0 ) {
		
		 return;
		}
		
		Context context = contexts[pos2];
		if (context.name.equals(contextPath)) {
		 addWrapper(context, path, wrapper, jspWildCard);
		}
		}
    }
    
    
    public void addWrapper(String path, Object wrapper, boolean jspWildCard) {
        addWrapper(context, path, wrapper, jspWildCard);
    }
    
    
    
    
    
    /**
     * Remove a wrapper from the context associated with this wrapper.
     *
     * @param path Wrapper mapping
     */
    public void removeWrapper(String path) {
        removeWrapper(context, path);
    }


    /**
     * Remove a wrapper from an existing context.
     *
     * @param hostName Virtual host name this wrapper belongs to
     * @param contextPath Context path this wrapper belongs to
     * @param path Wrapper mapping
     */
    public void removeWrapper
        (String hostName, String contextPath, String path) {
        Host[] hosts = this.hosts;
        int pos = find(hosts, hostName);
        if (pos < 0) {
            return;
        }
        Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            Context[] contexts = host.contextList.contexts;
            int pos2 = find(contexts, contextPath);
            if (pos2 < 0) {
                return;
            }
            Context context = contexts[pos2];
            if (context.name.equals(contextPath)) {
                removeWrapper(context, path);
            }
        }
    }

    protected void removeWrapper(Context context, String path) {
        synchronized (context) {
            if (path.endsWith("/*")) {
                // Wildcard wrapper
                String name = path.substring(0, path.length() - 2);
                Wrapper[] oldWrappers = context.wildcardWrappers;
                Wrapper[] newWrappers =
                    new Wrapper[oldWrappers.length - 1];
                if (removeMap(oldWrappers, newWrappers, name)) {
                    // Recalculate nesting
                    context.nesting = 0;
                    for (int i = 0; i < newWrappers.length; i++) {
                        int slashCount = slashCount(newWrappers[i].name);
                        if (slashCount > context.nesting) {
                            context.nesting = slashCount;
                        }
                    }
                    context.wildcardWrappers = newWrappers;
                }
            } else if (path.startsWith("*.")) {
                // Extension wrapper
                String name = path.substring(2);
                Wrapper[] oldWrappers = context.extensionWrappers;
                Wrapper[] newWrappers =
                    new Wrapper[oldWrappers.length - 1];
                if (removeMap(oldWrappers, newWrappers, name)) {
                    context.extensionWrappers = newWrappers;
                }
            } else if (path.equals("/")) {
                // Default wrapper
                context.defaultWrapper = null;
            } else {
                // Exact wrapper
                String name = path;
                Wrapper[] oldWrappers = context.exactWrappers;
                Wrapper[] newWrappers =
                    new Wrapper[oldWrappers.length - 1];
                if (removeMap(oldWrappers, newWrappers, name)) {
                    context.exactWrappers = newWrappers;
                }
            }
        }
    }
    
    
    
	// ---------------------- MapElement Inner Class -----------
    
    protected static abstract class MapElement {
    	public String name = null;
        public Object object = null;
    }
    
    
	// ------------------ Host Inner Class ------------------
    
    protected static final class Host
    extends MapElement {
    	public ContextList contextList = null;
    }
    
    
    
    
	// ---------------- ContextList Inner Class ----------------
    
    protected static final class ContextList {

        public Context[] contexts = new Context[0];
        public int nesting = 0;

    }
    
    
	// ----------------- Context Inner Class -----------------
    
    protected static final class Context
    extends MapElement {
    	
    	 public String path = null;
         public String[] welcomeResources = new String[0];
         public javax.naming.Context resources = null;
         public Wrapper defaultWrapper = null;
         public Wrapper[] exactWrappers = new Wrapper[0];
         public Wrapper[] wildcardWrappers = new Wrapper[0];
         public Wrapper[] extensionWrappers = new Wrapper[0];
         public int nesting = 0;
    }
    
    
	// ---------------- Wrapper Inner Class ----------------
    
    protected static class Wrapper
    extends MapElement {

    	public String path = null;
    	public boolean jspWildCard = false;
    }
    
	
}
