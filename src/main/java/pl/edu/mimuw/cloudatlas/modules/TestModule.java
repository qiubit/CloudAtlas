package pl.edu.mimuw.cloudatlas.modules;


import org.json.simple.JSONObject;
import pl.edu.mimuw.cloudatlas.fetcher.Fetcher;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static pl.edu.mimuw.cloudatlas.agent.Agent.*;

public class TestModule extends Module {

    public static final String moduleID = "TestModule";

    private ZMIHolderModule zmiHolder = null;
    private FetcherModule fetcher = null;
    private TimerModule module = null;

    public TestModule() throws Exception {
        super(moduleID);
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
        List<ZMI> testZmis = createGossipTestZMI();
        ZMIHolderModule zmiHolderModule = new ZMIHolderModule(testZmis.get(0), testZmis.get(1));
        FetcherModule fetcherModule = new FetcherModule();
        TimerModule timerModule = new TimerModule();
        // testModule.test();
        // testModule.testGossipInfo();
    }

    private static List<ZMI> createGossipTestZMI() {
        ZMI root = new ZMI();
        root.getAttributes().add("name", new ValueString(null));

        ZMI uw = new ZMI(root);
        root.addSon(uw);
        uw.getAttributes().add("name", new ValueString("uw"));

        ZMI pw = new ZMI(root);
        root.addSon(pw);
        pw.getAttributes().add("name", new ValueString("pw"));

        ZMI pjwstk = new ZMI(root);
        root.addSon(pjwstk);
        pjwstk.getAttributes().add("name", new ValueString("pjwstk"));

        ZMI uw1 = new ZMI(uw);
        uw.addSon(uw1);
        uw1.getAttributes().add("name", new ValueString("uw1"));

        ZMI uw2 = new ZMI(uw);
        uw.addSon(uw2);
        uw2.getAttributes().add("name", new ValueString("uw2"));

        ZMI uw3 = new ZMI(uw);
        uw.addSon(uw3);
        uw3.getAttributes().add("name", new ValueString("uw3"));

        ZMI pjwstk1 = new ZMI(pjwstk);
        pjwstk.addSon(pjwstk1);
        pjwstk1.getAttributes().add("name", new ValueString("pjwstk1"));

        ZMI pjwstk2 = new ZMI(pjwstk);
        pjwstk.addSon(pjwstk2);
        pjwstk2.getAttributes().add("name", new ValueString("pjwstk2"));

        List<ZMI> res = new ArrayList<>();
        res.add(root);
        res.add(uw1);

        return res;
    }

    private void testGossipInfo() throws Exception {
        List<ZMI> testZMIs = createGossipTestZMI();

        this.zmiHolder = new ZMIHolderModule(testZMIs.get(0), testZMIs.get(1));

        if (this.zmiHolder == null) {
            throw new Exception("Test: ZMIHolder required for testGossipInfo test");
        }

        Long msgId = this.getFreeId();
        this.sendMsg(
                ZMIHolderModule.moduleID,
                msgId.toString(),
                new GetZMIGossipInfoRequestMessage("/uw/uw1"),
                Module.SERIALIZED_TYPE,
                TestModule.moduleID
        );

        // Jakiś reliable sposób na sendMsg, wait for response?

        // REMINDER: Master thread for consuming
        // Now probably testGossipModule is being called from
        // TestModule Master Thread

        Message response = null;
        response = this.responseQueue.get(msgId).poll(10000, TimeUnit.MILLISECONDS);
        if (response == null) {
            throw new Exception("Test: No response in testGossipInfo");
        }
        System.out.println(((GetZMIGossipInfoResponseMessage) response).relevantZMIs);
        System.out.println(((GetZMIGossipInfoResponseMessage) response).contacts);
    }

    @Override
    public Message handleMessage(GetZMIGossipInfoResponseMessage msg) {
        return null;
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
