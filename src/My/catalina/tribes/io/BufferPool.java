package My.catalina.tribes.io;

import My.juli.logging.Log;
import My.juli.logging.LogFactory;

public class BufferPool {
	
	protected static Log log = LogFactory.getLog(BufferPool.class);

    public static int DEFAULT_POOL_SIZE = 100*1024*1024; //100MB
    
    
    protected static volatile BufferPool instance = null;
    
    protected BufferPoolAPI pool = null;
    
    private BufferPool(BufferPoolAPI pool) {
        this.pool = pool;
    }
    
    public static BufferPool getBufferPool() {
    	if (  (instance == null) ) {
    		synchronized (BufferPool.class) {
    			if ( instance == null ) {
    				BufferPoolAPI pool = null;
                    Class clazz = null;
                    try {
                    	clazz = Class.forName("My.catalina.tribes.io.BufferPool15Impl");
                    	pool = (BufferPoolAPI)clazz.newInstance();
                    }catch ( Throwable x ) {
                    	try {
                    		clazz = Class.forName("My.catalina.tribes.io.BufferPool14Impl");
                    		pool = (BufferPoolAPI)clazz.newInstance();
                    	}
                    	catch ( Throwable e ) {
                            log.warn("Unable to initilize BufferPool, not pooling XByteBuffer objects:"+x.getMessage());
                            if ( log.isDebugEnabled() ) log.debug("Unable to initilize BufferPool, not pooling XByteBuffer objects:",x);
                        }
                    }
                    pool.setMaxSize(DEFAULT_POOL_SIZE);
                    log.info("Created a buffer pool with max size:"+DEFAULT_POOL_SIZE+" bytes of type:"+(clazz!=null?clazz.getName():"null"));
                    instance = new BufferPool(pool);
                    
    			}
    		}
    	}
    	
    	return instance;
    }
    
    public XByteBuffer getBuffer(int minSize, boolean discard) {
        if ( pool != null ) return pool.getBuffer(minSize, discard);
        else return new XByteBuffer(minSize,discard);
    }

    public void returnBuffer(XByteBuffer buffer) {
        if ( pool != null ) pool.returnBuffer(buffer);
    }

    public void clear() {
        if ( pool != null ) pool.clear();
    }
    
    
    public static interface BufferPoolAPI {
    	
    	public void setMaxSize(int bytes);

        public XByteBuffer getBuffer(int minSize, boolean discard);

        public void returnBuffer(XByteBuffer buffer);

        public void clear();
    }
}
