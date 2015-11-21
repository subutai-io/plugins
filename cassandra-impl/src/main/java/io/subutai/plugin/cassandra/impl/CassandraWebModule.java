package io.subutai.plugin.cassandra.impl;


import io.subutai.webui.api.WebuiModule;


public class CassandraWebModule implements WebuiModule
{
    public static String NAME = "Cassandra";
    public static String IMG = "plugins/keshig/keshig.png";

    public CassandraWebModule()
    {

    }

    @Override
    public String getName()
    {
        return NAME;
    }


    @Override
    public String getModuleInfo()
    {
        return String.format( "{\"img\" : \"%s\", \"name\" : \"%s\"}", IMG, NAME );
    }


    @Override
    public String getAngularDependecyList()
    {
        return String.format( "{" +
                "name: 'subutai.blueprints', files: ["
                + "'subutai-app/blueprints/blueprints.js',"
                + "'subutai-app/blueprints/controller.js',"
                + "'subutai-app/environment/service.js'"
                + "]}" );
    }
}
