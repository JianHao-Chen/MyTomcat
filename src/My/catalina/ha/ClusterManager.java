package My.catalina.ha;

import My.catalina.Manager;

public interface ClusterManager extends Manager{

	public String getName();
	
	public void setName(String name);
	
	public CatalinaCluster getCluster();
	
	public void setCluster(CatalinaCluster cluster);
	
	public void setDefaultMode(boolean mode);
	
	public boolean isDefaultMode();
	
	public ClusterManager cloneFromTemplate();
	
	
	
	/**
	* A message was received from another node, this
	* is the callback method to implement if you are interested in
	* receiving replication messages.
	* @param msg - the message received.
	*/
	public void messageDataReceived(ClusterMessage msg);
}
