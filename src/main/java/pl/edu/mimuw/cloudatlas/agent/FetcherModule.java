package pl.edu.mimuw.cloudatlas.agent;


public class FetcherModule extends Module implements MessageHandler {

    public static final String moduleID = "fetcher";

    public FetcherModule() throws Exception {
        super(moduleID);
    }

    @Override
    public Message handleMessage(FetcherMeasurementsMessage msg) {
        System.out.println("Measurements arrived: ");
        System.out.println(msg.getName() + ": " + msg.getValue());
        System.out.println("FetcherModule TODO: Save these measurements!");
        System.out.println();
        // TODO: Notify ZMI holder about changes needed
        return null;
    }
}
