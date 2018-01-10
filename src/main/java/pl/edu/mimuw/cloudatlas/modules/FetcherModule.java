package pl.edu.mimuw.cloudatlas.modules;


import pl.edu.mimuw.cloudatlas.messages.FetcherMeasurementsMessage;
import pl.edu.mimuw.cloudatlas.messages.Message;
import pl.edu.mimuw.cloudatlas.messages.MessageHandler;
import pl.edu.mimuw.cloudatlas.messages.SetAttributeMessage;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueString;


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
