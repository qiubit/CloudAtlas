package pl.edu.mimuw.cloudatlas.agent.module;

import pl.edu.mimuw.cloudatlas.agent.message.MessageHandler;

public class QueryHolderModule extends Module implements MessageHandler {
    public final static String moduleID = "QueryHolder";

    QueryHolderModule() throws Exception {
        super(moduleID);
    }



}
