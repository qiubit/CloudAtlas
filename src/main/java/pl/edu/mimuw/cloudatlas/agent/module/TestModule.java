package pl.edu.mimuw.cloudatlas.agent.module;


import pl.edu.mimuw.cloudatlas.agent.message.*;
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

    @Override
    public Message handleMessage(WakeUpResponseMessage msg) {
        System.out.println("Woken up!");
        System.out.println(System.currentTimeMillis());
        return null;
    }

    public static void main(String[] args) throws Exception {

        TestModule testModule = new TestModule();
        ZMIHolderModule zmiHolderModule = new ZMIHolderModule(createTestHierarchy());
        TimerModule timerModule = new TimerModule();
        testModule.test();
    }

    public void test() throws Exception {
        System.out.println(System.currentTimeMillis());
        this.sendMsg(ZMIHolderModule.moduleID, "dupa", new GetAttributesRequestMessage(new PathName("/uw/violet07")));
        this.sendMsg(ZMIHolderModule.moduleID, "dupa", new GetAttributesResponseMessage(null));
        this.sendMsg(TimerModule.moduleID, "dupa", new WakeUpRequestMessage(System.currentTimeMillis() + 10000));
        this.sendMsg(TimerModule.moduleID, "dupa", new WakeUpRequestMessage(System.currentTimeMillis() + 2000));
    }

}
