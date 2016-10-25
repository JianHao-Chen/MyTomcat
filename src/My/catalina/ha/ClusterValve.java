package My.catalina.ha;

import My.catalina.Valve;

/**
 * Cluster valves are a simple extension to the Tomcat valve architecture
 * with a small addition of being able to reference the cluster component in the container it sits in.
 */

public interface ClusterValve extends Valve {

	/**
     * Returns the cluster the cluster deployer is associated with
     * @return CatalinaCluster
     */
    public CatalinaCluster getCluster();

    /**
     * Associates the cluster deployer with a cluster
     * @param cluster CatalinaCluster
     */
    public void setCluster(CatalinaCluster cluster);
}
