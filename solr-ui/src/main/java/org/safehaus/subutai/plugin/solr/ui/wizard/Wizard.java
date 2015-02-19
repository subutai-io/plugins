/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{
    private final GridLayout grid;
    private final Solr solr;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private int step = 1;
    private SolrClusterConfig solrClusterConfig = new SolrClusterConfig();
    private EnvironmentManager environmentManager;
    private boolean installOverEnvironment;


    public Wizard( ExecutorService executorService, Solr solr, Tracker tracker, final EnvironmentManager manager )
            throws NamingException
    {
        this.executorService = executorService;
        this.solr = solr;
        this.tracker = tracker;
        this.environmentManager = manager;

        grid = new GridLayout( 1, 20 );
        grid.setMargin( true );
        grid.setSizeFull();

        putForm();
    }


    private void putForm()
    {
        grid.removeComponent( 0, 1 );
        Component component = null;
        switch ( step )
        {
            case 1:
            {
                installOverEnvironment = false;
                component = new WelcomeStep( this );
                break;
            }
            case 2:
            {
                component = installOverEnvironment ? new ConfigurationStepOverEnvironment( this ) :
                            new ConfigurationStep( this );
                break;
            }
            case 3:
            {
                component = new VerificationStep( solr, executorService, tracker, this );
                break;
            }
            default:
            {
                break;
            }
        }

        if ( component != null )
        {
            grid.addComponent( component, 0, 1, 0, 19 );
        }
    }


    public Component getContent()
    {
        return grid;
    }


    public void setInstallOverEnvironment( boolean installationType )
    {
        this.installOverEnvironment = installationType;
    }


    protected void next()
    {
        step++;
        putForm();
    }


    protected void back()
    {
        step--;
        putForm();
    }


    protected void init()
    {
        step = 1;
        solrClusterConfig = new SolrClusterConfig();
        putForm();
    }


    public SolrClusterConfig getSolrClusterConfig()
    {
        return solrClusterConfig;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public boolean getInstallOverEnvironment()
    {
        return installOverEnvironment;
    }


    public Solr getSolr()
    {
        return solr;
    }
}
