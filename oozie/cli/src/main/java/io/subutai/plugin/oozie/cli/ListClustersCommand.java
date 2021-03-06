package io.subutai.plugin.oozie.cli;


import java.util.List;

import io.subutai.plugin.oozie.api.Oozie;
import io.subutai.plugin.oozie.api.OozieClusterConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * sample command : oozie:list-clusters
 */
@Command( scope = "oozie", name = "list-clusters", description = "Lists Oozie clusters" )
public class ListClustersCommand extends OsgiCommandSupport
{

    private Oozie oozieManager;


    public Oozie getOozieManager()
    {
        return oozieManager;
    }


    public void setOozieManager( Oozie oozieManager )
    {
        this.oozieManager = oozieManager;
    }


    protected Object doExecute()
    {
        List<OozieClusterConfig> configList = oozieManager.getClusters();
        if ( !configList.isEmpty() )
        {
            for ( OozieClusterConfig config : configList )
            {
                System.out.println( config.getClusterName() );
            }
        }
        else
        {
            System.out.println( "No Oozie cluster" );
        }

        return null;
    }
}
