package My.coyote;

import java.util.Locale;

public final class Constants {
	// -------------------------------------------------------------- Constants


    public static final String DEFAULT_CHARACTER_ENCODING="ISO-8859-1";


    public static final String LOCALE_DEFAULT = "en";


    public static final Locale DEFAULT_LOCALE = new Locale(LOCALE_DEFAULT, "");


    public static final int MAX_NOTES = 32;


    // Request states
    public static final int STAGE_NEW = 0;
    public static final int STAGE_PARSE = 1;
    public static final int STAGE_PREPARE = 2;
    public static final int STAGE_SERVICE = 3;
    public static final int STAGE_ENDINPUT = 4;
    public static final int STAGE_ENDOUTPUT = 5;
    public static final int STAGE_KEEPALIVE = 6;
    public static final int STAGE_ENDED = 7;


    /**
     * Has security been turned on?
     */
    public static final boolean IS_SECURITY_ENABLED =
        (System.getSecurityManager() != null);


    /**
     * If true, custom HTTP status messages will be used in headers.
     */
    public static final boolean USE_CUSTOM_STATUS_MSG_IN_HEADER =
        Boolean.valueOf(System.getProperty(
                "org.apache.coyote.USE_CUSTOM_STATUS_MSG_IN_HEADER",
                "false")).booleanValue(); 

    /**
     * Limit on the total length of the trailer headers in
     * a chunked HTTP request.
     */
    public static final int MAX_TRAILER_SIZE =
        Integer.parseInt(System.getProperty(
                "org.apache.coyote.MAX_TRAILER_SIZE",
                "8192"));
}
