package org.safehaus.subutai.plugin.spark.cli;


import java.io.IOException;
import java.util.UUID;

import org.safehaus.subutai.common.environment.ContainerHostNotFoundException;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * sample command :
 *      spark:stop-cluster test \ {cluster name}
 */
@Command(scope = "spark", name = "stop-cluster", description = "Command to stop spark cluster")
public class StopAllNodesCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false)
    String clusterName = null;
    private Spark sparkManager;
    private Tracker tracker;
    private EnvironmentManager environmentManager;
    private static final Logger LOG = LoggerFactory.getLogger( InstallClusterCommand.class.getName() );

    protected Object doExecute() throws IOException
    {
        System.out.println( "Stopping " + clusterName + " spark cluster..." );
        SparkClusterConfig config = sparkManager.getCluster( clusterName );
        UUID sparkMaster = config.getMasterNodeId();
        Environment environment;
        try
        {
            environment = environmentManager.findEnvironment( config.getEnvironmentId() );
            try
            {
                String sparkMasterHostname = environment.getContainerHostById( sparkMaster ).getHostname();
                UUID uuid = sparkManager.stopCluster( clusterName, sparkMasterHostname );
                System.out.println( "Stop cluster operation is " + StartAllNodesCommand.waitUntilOperationFinish(
                        tracker, uuid ) ) ;
            }
            catch ( ContainerHostNotFoundException e )
            {
                LOG.error( "Could not find container host !!!" );
                e.printStackTrace();
            }
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.error( "Could not find environment !!!" );
            e.printStackTrace();
        }
        return null;
    }


    public Spark getSparkManager()
    {
        return sparkManager;
    }


    public void setSparkManager( final Spark sparkManager )
    {
        this.sparkManager = sparkManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }
}
