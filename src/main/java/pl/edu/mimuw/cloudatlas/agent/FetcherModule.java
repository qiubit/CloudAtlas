package pl.edu.mimuw.cloudatlas.agent;


import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.Type;


public class FetcherModule extends Module implements MessageHandler {

    public static final String moduleID = "fetcher";

    public FetcherModule() throws Exception {
        super(moduleID);
        System.out.println("Fetcher: starting");
    }

    @Override
    public Message handleMessage(FetcherMeasurementsMessage msg) {
        System.out.println("FetcherModule: handling FetcherMeasurementsMessage");

        Attribute attr = new Attribute(msg.getName());
        Value val = new ValueString(msg.getName()).convertTo(msg.getMeasurementType());

        SetAttributeMessage msgRet = new SetAttributeMessage(attr, val);
        msgRet.setReceiverQueueName(ZMIHolderModule.moduleID);
        return msgRet;
    }
}
