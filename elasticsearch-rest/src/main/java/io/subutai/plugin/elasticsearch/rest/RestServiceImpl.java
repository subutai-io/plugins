package io.subutai.plugin.elasticsearch.rest;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.tracker.OperationState;
import io.subutai.common.tracker.TrackerOperationView;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.plugin.common.api.ClusterException;
import io.subutai.plugin.elasticsearch.api.Elasticsearch;
import io.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;


public class RestServiceImpl implements RestService
{

    private Elasticsearch elasticsearch;
    private Tracker tracker;
    private EnvironmentManager environmentManager;


    public RestServiceImpl( final Elasticsearch elasticsearch )
    {
        this.elasticsearch = elasticsearch;
    }


    @Override
    public Response listClusters()
    {
        List<ElasticsearchClusterConfiguration> configList = elasticsearch.getClusters();
        List<String> clusterNames = Lists.newArrayList();

        for ( ElasticsearchClusterConfiguration config : configList )
        {
            clusterNames.add( config.getClusterName() );
        }

        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @Override
    public Response getCluster( final String clusterName )
    {
        ElasticsearchClusterConfiguration config = elasticsearch.getCluster( clusterName );
        if ( config == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found" ).build();
        }
        String cluster = JsonUtil.GSON.toJson( elasticsearch.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @Override
    public Response checkCluster( final String clusterName )
    {
        Preconditions.checkNotNull( clusterName );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.checkCluster( clusterName );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response startCluster( final String clusterName )
    {
        Preconditions.checkNotNull( clusterName );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.startCluster( clusterName );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response stopCluster( final String clusterName )
    {
        Preconditions.checkNotNull( clusterName );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.stopCluster( clusterName );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response configureCluster( final String environmentId, final String clusterName, final String nodes )
    {
        Preconditions.checkNotNull( environmentId );
        Preconditions.checkNotNull( clusterName );
        Preconditions.checkNotNull( nodes );
        Environment environment = null;
        try
        {
            environment = environmentManager.loadEnvironment( environmentId );
        }
        catch ( EnvironmentNotFoundException e )
        {
            e.printStackTrace();
        }

        if ( environment == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( "Could not find environment with id : " + environmentId ).build();
        }

        if ( elasticsearch.getCluster( clusterName ) != null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( "There is already a cluster with same name !" ).build();
        }

        ElasticsearchClusterConfiguration config = new ElasticsearchClusterConfiguration();
        config.setEnvironmentId( environmentId );
        config.setClusterName( clusterName );
        Set<String> allNodes = new HashSet<>();
        String[] configNodes = nodes.replaceAll( "\\s+", "" ).split( "," );
        Collections.addAll( allNodes, configNodes );
        config.getNodes().addAll( allNodes );


        UUID uuid = elasticsearch.installCluster( config );
        waitUntilOperationFinish( uuid );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response removeCluster( final String clusterName )
    {
        Preconditions.checkNotNull( clusterName );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.removeCluster( clusterName );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response checkNode( final String clusterName, final String hostname )
    {
        Preconditions.checkNotNull( clusterName );
        Preconditions.checkNotNull( hostname );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.checkNode( clusterName, hostname );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response startNode( final String clusterName, final String hostname )
    {
        Preconditions.checkNotNull( clusterName );
        Preconditions.checkNotNull( hostname );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.startNode( clusterName, hostname );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response stopNode( final String clusterName, final String hostname )
    {
        Preconditions.checkNotNull( clusterName );
        Preconditions.checkNotNull( hostname );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.stopNode( clusterName, hostname );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response addNode( final String clusterName )
    {
        Preconditions.checkNotNull( clusterName );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.addNode( clusterName, null );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response destroyNode( final String clusterName, final String hostname )
    {
        Preconditions.checkNotNull( clusterName );
        Preconditions.checkNotNull( hostname );
        if ( elasticsearch.getCluster( clusterName ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( clusterName + " cluster not found." ).build();
        }
        UUID uuid = elasticsearch.destroyNode( clusterName, hostname );
        OperationState state = waitUntilOperationFinish( uuid );
        return createResponse( uuid, state );
    }


    @Override
    public Response autoScaleCluster( final String clusterName, final boolean scale )
    {
        String message = "enabled";
        ElasticsearchClusterConfiguration config = elasticsearch.getCluster( clusterName );
        config.setAutoScaling( scale );
        try
        {
            elasticsearch.saveConfig( config );
        }
        catch ( ClusterException e )
        {
            e.printStackTrace();
        }
        if ( !scale )
        {
            message = "disabled";
        }

        return Response.status( Response.Status.OK ).entity( "Auto scale is " + message + " successfully" ).build();
    }


    private Response createResponse( UUID uuid, OperationState state )
    {
        TrackerOperationView po = tracker.getTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY, uuid );
        if ( state == OperationState.FAILED )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( po.getLog() ).build();
        }
        else if ( state == OperationState.SUCCEEDED )
        {
            return Response.status( Response.Status.OK ).entity( po.getLog() ).build();
        }
        else
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( "Timeout" ).build();
        }
    }


    private OperationState waitUntilOperationFinish( UUID uuid )
    {
        OperationState state = null;
        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po =
                    tracker.getTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY, uuid );
            if ( po != null )
            {
                if ( po.getState() != OperationState.RUNNING )
                {
                    state = po.getState();
                    break;
                }
            }
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
            if ( System.currentTimeMillis() - start > ( 90 * 1000 ) )
            {
                break;
            }
        }
        return state;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }
}
