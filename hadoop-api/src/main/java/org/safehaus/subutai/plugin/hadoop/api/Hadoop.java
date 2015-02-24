package org.safehaus.subutai.plugin.hadoop.api;


import java.util.UUID;

import org.safehaus.subutai.common.environment.Blueprint;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.common.api.ApiBase;
import org.safehaus.subutai.plugin.common.api.ClusterSetupException;
import org.safehaus.subutai.plugin.common.api.ClusterSetupStrategy;


public interface Hadoop extends ApiBase<HadoopClusterConfig>
{

    public UUID uninstallCluster( HadoopClusterConfig config );

    /**
     * This just removes cluster configuration from DB,
     * NOT destroys hadoop containers.
     * @param clusterName cluster name
     * @return uuid of operation
     */
    public UUID removeCluster( String clusterName );

    public UUID startNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID stopNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusSecondaryNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID startDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID stopDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID statusDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID startJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID stopJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID startTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID stopTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID statusTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID addNode( String clusterName, int nodeCount );

    public UUID addNode( String clusterName );

    public UUID destroyNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID checkDecomissionStatus( HadoopClusterConfig hadoopClusterConfig );

    public UUID excludeNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID includeNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public Blueprint getDefaultEnvironmentBlueprint( final HadoopClusterConfig config ) throws ClusterSetupException;
}
