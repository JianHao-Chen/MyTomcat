package My.catalina.ha.session;

import My.catalina.ha.ClusterMessage;

/**
*
* <B>Class Description:</B><BR>
* The SessionMessage class is a class that is used when a session has been
* created, modified, expired in a Tomcat cluster node.<BR>
*
* The following events are currently available:
* <ul>
*   <li><pre>public static final int EVT_SESSION_CREATED</pre><li>
*   <li><pre>public static final int EVT_SESSION_EXPIRED</pre><li>
*   <li><pre>public static final int EVT_SESSION_ACCESSED</pre><li>
*   <li><pre>public static final int EVT_GET_ALL_SESSIONS</pre><li>
*   <li><pre>public static final int EVT_SESSION_DELTA</pre><li>
*   <li><pre>public static final int EVT_ALL_SESSION_DATA</pre><li>
*   <li><pre>public static final int EVT_ALL_SESSION_TRANSFERCOMPLETE</pre><li>
*   <li><pre>public static final int EVT_CHANGE_SESSION_ID</pre><li>
* </ul>
*
*/

public interface SessionMessage extends ClusterMessage ,java.io.Serializable{
	
	/**
     * Event type used when a session has been created on a node
     */
    public static final int EVT_SESSION_CREATED = 1;
    /**
     * Event type used when a session has expired
     */
    public static final int EVT_SESSION_EXPIRED = 2;

    /**
     * Event type used when a session has been accessed (ie, last access time
     * has been updated. This is used so that the replicated sessions will not expire
     * on the network
     */
    public static final int EVT_SESSION_ACCESSED = 3;
    /**
     * Event type used when a server comes online for the first time.
     * The first thing the newly started server wants to do is to grab the
     * all the sessions from one of the nodes and keep the same state in there
     */
    public static final int EVT_GET_ALL_SESSIONS = 4;
    
    
    
}
