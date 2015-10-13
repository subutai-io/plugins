package io.subutai.plugin.flume.ui.wizard;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.plugin.common.ui.ConfigView;
import io.subutai.plugin.flume.api.Flume;
import io.subutai.plugin.flume.api.FlumeConfig;
import io.subutai.plugin.hadoop.api.Hadoop;
import io.subutai.plugin.hadoop.api.HadoopClusterConfig;
import io.subutai.server.ui.component.ProgressWindow;


public class VerificationStep extends VerticalLayout
{
    private final static Logger LOGGER = LoggerFactory.getLogger( VerificationStep.class );


    public VerificationStep( final Hadoop hadoop, final Flume flume, final ExecutorService executorService,
                             final Tracker tracker, final EnvironmentManager environmentManager, final Wizard wizard )
    {

        setSizeFull();

        GridLayout grid = new GridLayout( 1, 5 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label confirmationLbl = new Label( "<strong>Please verify the installation settings "
                + "(you may change them by clicking on Back button)</strong><br/>" );
        confirmationLbl.setContentMode( ContentMode.HTML );

        final FlumeConfig config = wizard.getConfig();
        final HadoopClusterConfig hadoopClusterConfig = hadoop.getCluster( wizard.getConfig().getHadoopClusterName() );

        ConfigView cfgView = new ConfigView( "Installation configuration" );
        cfgView.addStringCfg( "Installation Name", wizard.getConfig().getClusterName() );

        Environment hadoopEnvironment ;
        try
        {
            hadoopEnvironment = environmentManager.loadEnvironment( hadoopClusterConfig.getEnvironmentId() );
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOGGER.error( "Error getting environment by id: " + hadoopClusterConfig.getEnvironmentId(), e );
            return;
        }
        Set<EnvironmentContainerHost> nodes = null;
        try
        {
            nodes = hadoopEnvironment.getContainerHostsByIds( wizard.getConfig().getNodes() );
        }
        catch ( ContainerHostNotFoundException e )
        {
            LOGGER.error( "Container hosts not found", e );
        }
        if ( CollectionUtil.isCollectionEmpty( nodes ) )
        {
            throw new RuntimeException( "No containers found in environment" );
        }

        for ( EnvironmentContainerHost host : nodes )
        {
            cfgView.addStringCfg( "Node to install", host.getHostname() + "" );
        }


        Button install = new Button( "Install" );
        install.setId( "FluVerInstall" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                UUID trackId = flume.installCluster( config );

                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackId, FlumeConfig.PRODUCT_KEY );
                window.getWindow().addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        wizard.init();
                    }
                } );
                getUI().addWindow( window.getWindow() );
            }
        } );

        Button back = new Button( "Back" );
        back.setId( "FluVerBack" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                wizard.back();
            }
        } );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( install );

        grid.addComponent( confirmationLbl, 0, 0 );
        grid.addComponent( cfgView.getCfgTable(), 0, 1, 0, 3 );
        grid.addComponent( buttons, 0, 4 );

        addComponent( grid );
    }
}
