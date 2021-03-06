package io.subutai.plugin.presto.api;


import java.util.UUID;

import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.plugincommon.api.CompleteEvent;
import io.subutai.core.plugincommon.api.NodeOperationType;
import io.subutai.core.plugincommon.impl.AbstractNodeOperationTask;


public class NodeOperationTask extends AbstractNodeOperationTask implements Runnable
{
    private final String clusterName;
    private final EnvironmentContainerHost containerHost;
    private final Presto presto;
    private final NodeOperationType operationType;


    public NodeOperationTask( Presto presto, Tracker tracker, String clusterName,
                              EnvironmentContainerHost containerHost, NodeOperationType operationType,
                              CompleteEvent completeEvent, UUID trackID )
    {
        super( tracker, presto.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.presto = presto;
        this.clusterName = clusterName;
        this.containerHost = containerHost;
        this.operationType = operationType;
    }


    @Override
    public UUID runTask()
    {
        UUID trackID = null;
        switch ( operationType )
        {
            case START:
                trackID = presto.startNode( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = presto.stopNode( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = presto.checkNode( clusterName, containerHost.getHostname() );
                break;
        }
        return trackID;
    }


    @Override
    public String getProductStoppedIdentifier()
    {
        return "Not running";
    }


    @Override
    public String getProductRunningIdentifier()
    {
        return "Running as";
    }
}
