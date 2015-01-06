package org.safehaus.subutai.plugin.storm.ui.environment;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.ui.wizard.Wizard;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;


public class ConfigurationStep extends VerticalLayout
{

    public ConfigurationStep( final EnvironmentWizard environmentWizard, final Zookeeper zookeeper )
    {
        removeAllComponents();
        setSizeFull();

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
        clusterNameTxtFld.setInputPrompt( "Cluster name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.setValue( environmentWizard.getConfig().getClusterName() );
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField domainNameTxtFld = new TextField( "Enter domain name" );
        domainNameTxtFld.setInputPrompt( "Domain name" );
        domainNameTxtFld.setInputPrompt( "intra.lan" );
        domainNameTxtFld.setRequired( true );
        domainNameTxtFld.setValue( environmentWizard.getConfig().getClusterName() );
        domainNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig().setDomainName( event.getProperty().getValue().toString().trim() );
            }
        } );


        final List<Environment> environmentList = environmentWizard.getEnvironmentManager().getEnvironments();
        List<Environment> envList = new ArrayList<>();
        for ( Environment anEnvironmentList : environmentList )
        {
            boolean exists = isTemplateExists( anEnvironmentList.getContainerHosts(), StormClusterConfiguration.TEMPLATE_NAME );
            if ( exists )
            {
                envList.add( anEnvironmentList );
            }
        }

        // all nodes
        final TwinColSelect allNodesSelect =
                getTwinSelect( "Nodes to be configured", "hostname", "Available Nodes", "Selected Nodes", 4 );
        allNodesSelect.setId( "AllNodes" );
        allNodesSelect.setValue( null );

        // seeds
        final TwinColSelect seedsSelect =
                getTwinSelect( "Seeds", "hostname", "Available Nodes", "Selected Nodes", 4 );
        seedsSelect.setId( "Seeds" );

        final ComboBox envCombo = new ComboBox( "Choose environment" );
        BeanItemContainer<Environment> eBean = new BeanItemContainer<>( Environment.class );
        eBean.addAll( envList );
        envCombo.setContainerDataSource( eBean );
        envCombo.setNullSelectionAllowed( false );
        envCombo.setTextInputAllowed( false );
        envCombo.setItemCaptionPropertyId( "name" );
        envCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                Environment e = ( Environment ) event.getProperty().getValue();
                environmentWizard.getConfig().setEnvironmentId( e.getId() );
                allNodesSelect.setContainerDataSource(
                        new BeanItemContainer<>( ContainerHost.class, e.getContainerHosts() ) );
            }
        } );


        Component nimbusElem;
        if ( environmentWizard.getConfig().isExternalZookeeper() )
        {
            ComboBox zkClustersCombo = new ComboBox( "Zookeeper cluster" );
            zkClustersCombo.setId( "StormConfClusterCombo" );
            final ComboBox masterNodeCombo = makeMasterNodeComboBox( environmentWizard );
            masterNodeCombo.setId( "StormMasterNodeCombo" );

            zkClustersCombo.setImmediate( true );
            zkClustersCombo.setTextInputAllowed( false );
            zkClustersCombo.setRequired( true );
            zkClustersCombo.setNullSelectionAllowed( false );
            zkClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent e )
                {
                    masterNodeCombo.removeAllItems();
                    if ( e.getProperty().getValue() != null )
                    {
                        ZookeeperClusterConfig zookeeperClusterConfig = ( ZookeeperClusterConfig ) e.getProperty().getValue();
                        Environment zookeeperEnvironment =
                                environmentWizard.getEnvironmentManager().getEnvironmentByUUID(
                                        zookeeperClusterConfig.getEnvironmentId() );
                        Set<ContainerHost> zookeeperNodes =
                                zookeeperEnvironment.getContainerHostsByIds( zookeeperClusterConfig.getNodes() );
                        for ( ContainerHost containerHost : zookeeperNodes )
                        {
                            masterNodeCombo.addItem( containerHost );
                            masterNodeCombo.setItemCaption( containerHost, containerHost.getHostname() );
                        }
                        // do select if values exist
                        if ( environmentWizard.getConfig().getNimbus() != null )
                        {
                            masterNodeCombo.setValue( environmentWizard.getConfig().getNimbus() );
                        }

                        environmentWizard.setZookeeperClusterConfig( zookeeperClusterConfig );

                        environmentWizard.getConfig().setZookeeperClusterName( zookeeperClusterConfig.getClusterName() );
                    }
                }
            } );
            List<ZookeeperClusterConfig> zk_list = zookeeper.getClusters();
            for ( ZookeeperClusterConfig zkc : zk_list )
            {
                zkClustersCombo.addItem( zkc );
                zkClustersCombo.setItemCaption( zkc, zkc.getClusterName() );
                if ( zkc.getClusterName().equals( environmentWizard.getConfig().getZookeeperClusterName() ) )
                {
                    zkClustersCombo.setValue( zkc );
                }
            }
            if ( environmentWizard.getConfig().getNimbus() != null )
            {
                masterNodeCombo.setValue( environmentWizard.getConfig().getNimbus() );
            }

            HorizontalLayout hl = new HorizontalLayout( zkClustersCombo, masterNodeCombo );
            nimbusElem = new Panel( "Nimbus node", hl );
            nimbusElem.setSizeUndefined();
            nimbusElem.setStyleName( "default" );
        }
        else
        {
            String s = "<b>A new nimbus node will be created with Zookeeper instance installed</b>";
            nimbusElem = new Label( s, ContentMode.HTML );
        }



        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( Strings.isNullOrEmpty( environmentWizard.getConfig().getClusterName() ) )
                {
                    show( "Please provide cluster name !" );
                }
                else if ( Strings.isNullOrEmpty( environmentWizard.getConfig().getDomainName() ) )
                {
                    show( "Please provide domain name !" );
                }
                else if ( environmentWizard.getConfig().getNimbus() == null )
                {
                    show( "Please select nimbus node!" );
                }
                else if ( environmentWizard.getConfig().getSupervisors().size() <= 0 )
                {
                    show( "Please select supervisor to be configured !" );
                }
                else
                {
                    environmentWizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                environmentWizard.back();
            }
        } );

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing( true );
        layout.addComponent( new Label( "Please, specify installation settings" ) );
        layout.addComponent( content );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( next );

        content.addComponent( clusterNameTxtFld );
        content.addComponent( domainNameTxtFld );
        content.addComponent( envCombo );
        content.addComponent( allNodesSelect );
        content.addComponent( seedsSelect );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private boolean isTemplateExists( Set<ContainerHost> containerHosts, String templateName ){
        for ( ContainerHost host: containerHosts ){
            if ( host.getTemplateName().equals( templateName ) ){
                return true;
            }
        }
        return  false;
    }

    private ComboBox makeMasterNodeComboBox( final EnvironmentWizard wizard )
    {
        ComboBox cb = new ComboBox( "Nodes" );

        cb.setId( "StormConfMasterNodes" );
        cb.setImmediate( true );
        cb.setTextInputAllowed( false );
        cb.setRequired( true );
        cb.setNullSelectionAllowed( false );
        cb.addValueChangeListener( new Property.ValueChangeListener()
        {

            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                ContainerHost serverNode = ( ContainerHost ) event.getProperty().getValue();
                wizard.getConfig().setNimbus( serverNode.getId() );
            }
        } );
        return cb;
    }

    public static TwinColSelect getTwinSelect( String title, String captionProperty, String leftTitle,
                                               String rightTitle, int rows )
    {
        TwinColSelect twinColSelect = new TwinColSelect( title );
        twinColSelect.setItemCaptionPropertyId( captionProperty );
        twinColSelect.setRows( rows );
        twinColSelect.setMultiSelect( true );
        twinColSelect.setImmediate( true );
        twinColSelect.setLeftColumnCaption( leftTitle );
        twinColSelect.setRightColumnCaption( rightTitle );
        twinColSelect.setWidth( 100, Unit.PERCENTAGE );
        twinColSelect.setRequired( true );
        return twinColSelect;
    }

    private void show( String notification )
    {
        Notification.show( notification );
    }
}
