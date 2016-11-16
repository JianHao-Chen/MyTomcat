package My.catalina.ha;

import My.catalina.Manager;

public interface ClusterManager extends Manager{

	public String getName();
	
	public void setName(String name);
	
	public CatalinaCluster getCluster();
	
	public void setCluster(CatalinaCluster cluster);
	
	public ClusterManager cloneFromTemplate();
}
