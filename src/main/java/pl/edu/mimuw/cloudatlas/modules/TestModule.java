package pl.edu.mimuw.cloudatlas.modules;


import org.json.simple.JSONObject;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.PathName;

import static pl.edu.mimuw.cloudatlas.agent.Agent.*;

public class TestModule extends Module {

    public static final String moduleID = "TestModule";

    public TestModule() throws Exception {
        super(moduleID);
        System.out.println("TestModule: Starting");
    }


    @Override
    public Message handleMessage(GetAttributesResponseMessage msg) {
        System.out.println(msg.attributesMap.toString());
        return null;
    }

    @Override
    public Message handleMessage(Message msg) {
        System.out.println("Test: Received message");
        return null;
    }

    public static void main(String[] args) throws Exception {

        TestModule testModule = new TestModule();
        ZMIHolderModule zmiHolderModule = new ZMIHolderModule(ZMIHolderModule.createTestHierarchy());
        FetcherModule fetcherModule = new FetcherModule();
        TimerModule timerModule = new TimerModule();
        // testModule.test();
    }

    public void test() throws Exception {
        Message msg1 = new ScheduledMessage(new GetAttributesRequestMessage(new PathName("/uw/violet07")), 1000);
        msg1.setReceiverQueueName(TestModule.moduleID);

        Message msg2 = new ScheduledMessage(new GetAttributesRequestMessage(new PathName("/uw/violet07")), 2000);
        msg2.setReceiverQueueName(TestModule.moduleID);

        Message msg3 = new ScheduledMessage(new GetAttributesRequestMessage(new PathName("/uw/violet07")), 3000);
        msg3.setReceiverQueueName(TestModule.moduleID);

        this.sendMsg(
                TimerModule.moduleID,
                "test",
                msg1,
                Module.SERIALIZED_TYPE
        );
        this.sendMsg(
                TimerModule.moduleID,
                "test",
                msg2,
                Module.SERIALIZED_TYPE
        );
        this.sendMsg(
                TimerModule.moduleID,
                "test",
                msg3,
                Module.SERIALIZED_TYPE
        );
        /*
        this.sendMsg(
                ZMIHolderModule.moduleID,
                "dupa",
                new GetAttributesRequestMessage(new PathName("/uw/violet07")),
                Module.SERIALIZED_TYPE
        );
        this.sendMsg(
                ZMIHolderModule.moduleID,
                "dupa",
                new GetAttributesResponseMessage(null),
                Module.SERIALIZED_TYPE
        );

        JSONObject measurementObj = new JSONObject();
        measurementObj.put("topic", "measurements");
        measurementObj.put("name", "foo");
        measurementObj.put("value", "bar");
        measurementObj.put("timestamp", 42);
        this.sendMsg(
                FetcherModule.moduleID,
                "dupa",
                new FetcherMeasurementsMessage(measurementObj),
                Module.JSON_TYPE
        );
        */

    }

}
