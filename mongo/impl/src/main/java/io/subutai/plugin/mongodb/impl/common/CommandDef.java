package io.subutai.plugin.mongodb.impl.common;


import io.subutai.common.command.RequestBuilder;


public class CommandDef
{
    private String command;
    private String description;
    private int timeout;


    public CommandDef( String description, String command, int timeout )
    {
        this.description = description;
        this.command = command;
        this.timeout = timeout;
    }


    public RequestBuilder build( boolean daemon )
    {
        if ( daemon )
        {
            return new RequestBuilder( command ).withTimeout( timeout ).daemon();
        }
        else
        {
            return build();
        }
    }


    public RequestBuilder build()
    {
        return new RequestBuilder( command ).withTimeout( timeout );
    }


    public String getCommand()
    {
        return command;
    }


    public String getDescription()
    {
        return description;
    }


    public int getTimeout()
    {
        return timeout;
    }


    //    public void execute( Host host ) throws CommandException
    //    {
    //        host.execute( build() );
    //    }
}
