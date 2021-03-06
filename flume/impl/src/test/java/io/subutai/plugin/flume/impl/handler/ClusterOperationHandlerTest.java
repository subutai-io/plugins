package io.subutai.plugin.flume.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.plugincommon.api.ClusterOperationType;
import io.subutai.core.plugincommon.api.ClusterSetupStrategy;
import io.subutai.core.plugincommon.api.PluginDAO;
import io.subutai.plugin.flume.api.FlumeConfig;
import io.subutai.plugin.flume.impl.FlumeImpl;
import io.subutai.plugin.hadoop.api.Hadoop;
import io.subutai.plugin.hadoop.api.HadoopClusterConfig;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ClusterOperationHandlerTest
{
    private ClusterOperationHandler clusterOperationHandler;
    private ClusterOperationHandler clusterOperationHandler2;
    @Mock
    Tracker tracker;
    @Mock
    FlumeImpl flumeImpl;
    @Mock
    FlumeConfig flumeConfig;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    Environment environment;
    @Mock
    EnvironmentContainerHost containerHost;
    @Mock
    CommandResult commandResult;
    @Mock
    ClusterSetupStrategy clusterSetupStrategy;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    Hadoop hadoop;
    @Mock
    PluginDAO pluginDAO;


    @Before
    public void setUp() throws CommandException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        // mock constructor
        final String id = UUID.randomUUID().toString();
        when( flumeImpl.getCluster( anyString() ) ).thenReturn( flumeConfig );
        when( flumeImpl.getTracker() ).thenReturn( tracker );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( trackerOperation.getId() ).thenReturn( UUID.randomUUID() );

        // mock runOperationOnContainers method
        when( flumeImpl.getEnvironmentManager() ).thenReturn( environmentManager );
        when( environmentManager.loadEnvironment( any( String.class ) ) ).thenReturn( environment );
        when( environment.getContainerHostById( any( String.class ) ) ).thenReturn( containerHost );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        clusterOperationHandler = new ClusterOperationHandler( flumeImpl, flumeConfig, ClusterOperationType.INSTALL );
        clusterOperationHandler2 = new ClusterOperationHandler( flumeImpl, flumeConfig, ClusterOperationType.DESTROY );

        when( flumeImpl.getPluginDao() ).thenReturn( pluginDAO );
        when( environment.getContainerHostById( any( String.class ) ) ).thenReturn( containerHost );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        Set<String> myUUID = new HashSet<>();
        myUUID.add( id );
        when( flumeConfig.getNodes() ).thenReturn( myUUID );
    }


    @Test
    public void testRunOperationOnContainers() throws Exception
    {
        clusterOperationHandler.runOperationOnContainers( ClusterOperationType.INSTALL );
    }


    @Test
    public void testRunOperationTypeInstall() throws EnvironmentNotFoundException
    {
        when( flumeImpl.getClusterSetupStrategy( flumeConfig, trackerOperation ) ).thenReturn( clusterSetupStrategy );

        clusterOperationHandler.run();

        // assertions
        assertNotNull( flumeImpl.getEnvironmentManager().loadEnvironment( any( String.class ) ) );
    }


    @Test
    public void testRunOperationTypeInstallHadoopConfigIsNull()
    {
        when( flumeImpl.getClusterSetupStrategy( flumeConfig, trackerOperation ) ).thenReturn( clusterSetupStrategy );

        clusterOperationHandler.run();
    }


    @Test
    public void testRunOperationTypeInstallCheckException2()
    {
        when( flumeImpl.getClusterSetupStrategy( flumeConfig, trackerOperation ) ).thenReturn( clusterSetupStrategy );

        clusterOperationHandler.run();
    }


    @Test
    public void testRunOperationTypeDestroyOverHadoop() throws CommandException
    {
        when( commandResult.hasSucceeded() ).thenReturn( true );

        clusterOperationHandler2.run();

        // assertions
        assertNotNull( flumeConfig.getNodes() );
        assertTrue( commandResult.hasSucceeded() );
    }


    @Test
    public void testRunOperationTypeDestroyOverHadoopHasNotSucceeded() throws CommandException
    {
        when( commandResult.hasSucceeded() ).thenReturn( false );

        clusterOperationHandler2.run();
    }
}