package org.safehaus.subutai.plugin.storm.ui.environment;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Sizeable;
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

    private EnvironmentManager environmentManager;
    private EnvironmentWizard environmentWizard;


    public ConfigurationStep( final EnvironmentWizard environmentWizard, final Zookeeper zookeeper )
    {
        environmentManager = environmentWizard.getEnvironmentManager();
        this.environmentWizard = environmentWizard;

        removeAllComponents();
        setSizeFull();

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
        clusterNameTxtFld.setId( "StormConfClusterName" );
        clusterNameTxtFld.setInputPrompt( "Cluster name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField domainNameTxtFld = new TextField( "Enter domain name" );
        domainNameTxtFld.setId( "domainNameTxtFld" );
        domainNameTxtFld.setInputPrompt( "intra.lan" );
        domainNameTxtFld.setRequired( true );
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
                getTwinSelect( "Nodes to be configured as Supervisor", "Available Nodes", "Selected Nodes", 4 );
        allNodesSelect.setId( "AllNodes" );
        allNodesSelect.setValue( null );
        allNodesSelect.addValueChangeListener( new Property.ValueChangeListener()
        {

            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<UUID> nodes = new HashSet<UUID>();
                    Set<ContainerHost> nodeList = ( Set<ContainerHost> ) event.getProperty().getValue();
                    for ( ContainerHost host : nodeList )
                    {
                        nodes.add( host.getId() );
                    }
                    environmentWizard.getConfig().setSupervisors( nodes );
                }
            }
        } );


        final ComboBox nimbusNode = new ComboBox( "Choose Nimbus Node" );
        nimbusNode.setId( "nimbusNode" );
        nimbusNode.setNullSelectionAllowed( false );
        nimbusNode.setTextInputAllowed( false );
        nimbusNode.setImmediate( true );
        nimbusNode.setRequired( true );
        nimbusNode.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                Environment env = environmentManager.getEnvironmentByUUID( environmentWizard.getConfig().getEnvironmentId() );
                fillUpComboBox( allNodesSelect, env );
                UUID uuid = ( UUID ) event.getProperty().getValue();
                ContainerHost containerHost = env.getContainerHostById( uuid );
                environmentWizard.getConfig().setNimbus( containerHost.getId() );
                allNodesSelect.removeItem( containerHost );

            }
        } );


        final ComboBox envCombo = new ComboBox( "Choose environment" );
        envCombo.setId( "envCombo" );
        BeanItemContainer<Environment> eBean = new BeanItemContainer<>( Environment.class );
        eBean.addAll( envList );
        envCombo.setContainerDataSource( eBean );
        envCombo.setNullSelectionAllowed( true );
        envCombo.setTextInputAllowed( false );
        envCombo.setItemCaptionPropertyId( "name" );
        envCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                Environment environment = ( Environment ) event.getProperty().getValue();
                if ( environment != null ){
                    environmentWizard.getConfig().setEnvironmentId( environment.getId() );
                    nimbusNode.removeAllItems();
                    nimbusNode.setValue( null );

                    for ( ContainerHost host : filterEnvironmentContainers( environment.getContainerHosts() ) ){
                        allNodesSelect.addItem( host.getId() );
                        allNodesSelect.setItemCaption( host.getId(),
                                ( host.getHostname() + " (" + host.getIpByInterfaceName( "eth0" ) + ")" ) );
                        nimbusNode.addItem( host.getId() );
                        nimbusNode.setItemCaption( host.getId(),
                                ( host.getHostname() + " (" + host.getIpByInterfaceName( "eth0" ) + ")" ) );
                    }
                }
                else{
                    allNodesSelect.removeAllItems();
                    nimbusNode.removeAllItems();
                }
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
        next.setId( "StormConfNext" );
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
        back.setId( "StormConfBack" );
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
        content.addComponent( nimbusNode );
        content.addComponent( nimbusElem );
        content.addComponent( allNodesSelect );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private void fillUpComboBox( TwinColSelect target, Environment environment ){
        if ( environment != null ){
            environmentWizard.getConfig().setEnvironmentId( environment.getId() );
            target.removeAllItems();
            target.setValue( null );

            for ( ContainerHost host : filterEnvironmentContainers( environment.getContainerHosts() ) ){
                target.addItem( host );
                target.setItemCaption( host,
                        ( host.getHostname() + " (" + host.getIpByInterfaceName( "eth0" ) + ")" ) );
            }
        }
        else{
            target.removeAllItems();
        }
    }


    private Set<ContainerHost> filterEnvironmentContainers( Set<ContainerHost> containerHosts ){
        Set<ContainerHost> filteredSet = new HashSet<>();
        for ( ContainerHost containerHost : containerHosts ){
            if ( containerHost.getTemplateName().equals( StormClusterConfiguration.TEMPLATE_NAME ) ){
                filteredSet.add( containerHost );
            }
        }
        return filteredSet;
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

    public static TwinColSelect getTwinSelect( String title, String leftTitle,
                                               String rightTitle, int rows )
    {
        TwinColSelect twinColSelect = new TwinColSelect( title );
        twinColSelect.setRows( rows );
        twinColSelect.setMultiSelect( true );
        twinColSelect.setImmediate( true );
        twinColSelect.setLeftColumnCaption( leftTitle );
        twinColSelect.setRightColumnCaption( rightTitle );
        twinColSelect.setWidth( 100, Sizeable.Unit.PERCENTAGE );
        twinColSelect.setRequired( true );
        return twinColSelect;
    }

    private void show( String notification )
    {
        Notification.show( notification );
    }
}
