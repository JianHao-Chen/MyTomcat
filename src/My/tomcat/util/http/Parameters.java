package My.tomcat.util.http;

import java.io.IOException;
import java.util.Hashtable;

import My.tomcat.util.buf.ByteChunk;
import My.tomcat.util.buf.MessageBytes;
import My.tomcat.util.collections.MultiMap;

public final class Parameters extends MultiMap {

	public static final int INITIAL_SIZE=4;
	
	
	private Hashtable<String,String[]> paramHashStringArray =
        new Hashtable<String,String[]>();
	
	private boolean didQueryParameters=false;
	private boolean didMerge=false;
	
	MessageBytes queryMB;
	
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
	
	
	
	public String[] getParameterValues(String name) {
		handleQueryParameters();
		
		String values[]=(String[])paramHashStringArray.get(name);
        return values;
	}
	
	
	
	public String getParameter(String name ) {
		String[] values = getParameterValues(name);
		
		if (values != null) {
			if( values.length==0 ) return "";
            return values[0];
		}
		else {
            return null;
        }
	}
	
	
	// -------------------- Processing --------------------
	
	
	/** Process the query string into parameters
     */
    public void handleQueryParameters() {
    	
    	if( didQueryParameters ) return;
    	
    	didQueryParameters=true;
    	
    	if( queryMB==null || queryMB.isNull() )
            return;
    	
    }
    
    
    
    
    
    
    
    
	// ----------------- Parameter parsing ------------------
    
    ByteChunk tmpName=new ByteChunk();
    ByteChunk tmpValue=new ByteChunk();
    
    
    public void processParameters( byte bytes[], int start, int len ) {
        processParameters(bytes, start, len, encoding);
    }
    
    
    public void processParameters( byte bytes[], int start, int len, 
            String enc ) {
    	
    	int end=start+len;
        int pos=start;
        
        
        do {
        	 boolean noEq=false;
             int valStart=-1;
             int valEnd=-1;
             
             int nameStart=pos;
             int nameEnd=ByteChunk.indexOf(bytes, nameStart, end, '=' );
             
             // Workaround for a&b&c encoding
             int nameEnd2=ByteChunk.indexOf(bytes, nameStart, end, '&' );
             
             if( (nameEnd2!=-1 ) &&
                     ( nameEnd==-1 || nameEnd > nameEnd2) ) {
            	 
            	 nameEnd=nameEnd2;
                 noEq=true;
                 valStart=nameEnd;
                 valEnd=nameEnd;
             }
             
             if( nameEnd== -1 ) 
            	 nameEnd=end;
             
             if( ! noEq ) {
                 valStart= (nameEnd < end) ? nameEnd+1 : end;
                 valEnd=ByteChunk.indexOf(bytes, valStart, end, '&');
                 if( valEnd== -1 ) valEnd = (valStart < end) ? end : valStart;
             }
             
             pos=valEnd+1;
             
             if( nameEnd<=nameStart ) {
            	 
            	 continue;
                 // invalid chunk - it's better to ignore
             }
             
             tmpName.setBytes( bytes, nameStart, nameEnd-nameStart );
             tmpValue.setBytes( bytes, valStart, valEnd-valStart );
        
             
             try {
            	 addParam( urlDecode(tmpName, enc), urlDecode(tmpValue, enc) );
             }
             catch (IOException e) {
            	 
             }
             
             tmpName.recycle();
             tmpValue.recycle();
             
        }while( pos<end );
    	
    }
    
    
    
    // incredibly inefficient data representation for parameters,
    // until we test the new one
    private void addParam( String key, String value ) {
    	if( key==null ) return;
    	
    	String values[];
    	
    	if (paramHashStringArray.containsKey(key)) {
    		String oldValues[] = (String[])paramHashStringArray.
            get(key);
    		
    		values = new String[oldValues.length + 1];
            for (int i = 0; i < oldValues.length; i++) {
                values[i] = oldValues[i];
            }
            values[oldValues.length] = value;
    	}
    	else {
            values = new String[1];
            values[0] = value;
        }
    	
    	paramHashStringArray.put(key, values);
    }
    
    
    private String urlDecode(ByteChunk bc, String enc)
    throws IOException {
    	
    	return bc.toString();
    }
    
    
}
