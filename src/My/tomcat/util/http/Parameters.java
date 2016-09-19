package My.tomcat.util.http;

import My.tomcat.util.collections.MultiMap;

public final class Parameters extends MultiMap {

	public static final int INITIAL_SIZE=4;
	
	String encoding = null;
	String queryStringEncoding = null;
	
	public Parameters() {
        super( INITIAL_SIZE );
    }

	
	
	public void setEncoding( String s ) {
		encoding=s;
	        
	}
	public void setQueryStringEncoding( String s ) {
        queryStringEncoding=s;
    }
}
