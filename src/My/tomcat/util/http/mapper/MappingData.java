package My.tomcat.util.http.mapper;

import My.tomcat.util.buf.MessageBytes;

public class MappingData {
	
	 public Object host = null;
	 public Object context = null;
	 public Object wrapper = null;
	 public boolean jspWildCard = false;

	 public MessageBytes contextPath = MessageBytes.newInstance();
	 public MessageBytes requestPath = MessageBytes.newInstance();
	 public MessageBytes wrapperPath = MessageBytes.newInstance();
	 public MessageBytes pathInfo = MessageBytes.newInstance();

	 public MessageBytes redirectPath = MessageBytes.newInstance();

	 public void recycle() {
	     host = null;
	     context = null;
	     wrapper = null;
	     pathInfo.recycle();
	     requestPath.recycle();
	     wrapperPath.recycle();
	     contextPath.recycle();
	     redirectPath.recycle();
	     jspWildCard = false;
	 }

}
