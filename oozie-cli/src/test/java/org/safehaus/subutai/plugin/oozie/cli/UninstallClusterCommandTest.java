package org.safehaus.subutai.plugin.oozie.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UninstallClusterCommandTest
{
    private UninstallClusterCommand uninstallClusterCommand;
    @Mock
    TrackerOperationView trackerOperationView;
    @Mock
    Tracker tracker;
    @Mock
    Oozie oozie;
    @Mock
    OozieClusterConfig oozieClusterConfig;

    @Before
    public void setUp() throws Exception
    {
        uninstallClusterCommand = new UninstallClusterCommand();
        uninstallClusterCommand.setOozieManager( oozie );
        uninstallClusterCommand.setTracker(tracker);
    }

    @Test
    public void testGetTracker() throws Exception
    {
        uninstallClusterCommand.getTracker();

        // assertions
        assertNotNull(uninstallClusterCommand.getTracker());
        assertEquals( tracker,uninstallClusterCommand.getTracker() );
    }

    @Test
    public void testGetPrestoManager() throws Exception
    {
        uninstallClusterCommand.getOozieManager();

        // assertions
        assertNotNull(uninstallClusterCommand.getOozieManager());
        assertEquals( oozie,uninstallClusterCommand.getOozieManager() );
    }

    @Test
    public void testDoExecute() throws Exception
    {
        when(oozie.uninstallCluster(anyString())).thenReturn(UUID.randomUUID());
        when(tracker.getTrackerOperation(anyString(),any(UUID.class))).thenReturn(trackerOperationView);
        when(trackerOperationView.getLog()).thenReturn("test");

        uninstallClusterCommand.doExecute();

        // assertions
        assertNotNull( tracker.getTrackerOperation( anyString(),any(UUID.class) ) );
    }

    @Test
    public void testDoExecuteRunning() throws Exception
    {
        when(oozie.uninstallCluster(anyString())).thenReturn(UUID.randomUUID());
        when(tracker.getTrackerOperation(anyString(),any(UUID.class))).thenReturn(null);
        when(trackerOperationView.getLog()).thenReturn("test");

        uninstallClusterCommand.doExecute();

        //assertions
        assertNull( tracker.getTrackerOperation( anyString(),any(UUID.class) ) );
    }

}