package pl.edu.mimuw.cloudatlas.modules;


import org.json.simple.JSONObject;
import pl.edu.mimuw.cloudatlas.messages.FetcherMeasurementsMessage;
import pl.edu.mimuw.cloudatlas.messages.GetAttributesRequestMessage;
import pl.edu.mimuw.cloudatlas.messages.GetAttributesResponseMessage;
import pl.edu.mimuw.cloudatlas.messages.Message;
import pl.edu.mimuw.cloudatlas.model.PathName;

import static pl.edu.mimuw.cloudatlas.agent.Agent.*;

public class TestModule extends Module {

    public static final String moduleID = "TestModule";

    public TestModule() throws Exception {
        super(moduleID);
    }


    @Override
    public Message handleMessage(GetAttributesResponseMessage msg) {
        System.out.println(msg.attributesMap.toString());
        return null;
    }

    public static void main(String[] args) throws Exception {

        TestModule testModule = new TestModule();
        ZMIHolderModule zmiHolderModule = new ZMIHolderModule(createTestHierarchy());
        FetcherModule fetcherModule = new FetcherModule();
        // testModule.test();
    }

    public void test() throws Exception {
        ;
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

    }

}
