package pl.edu.mimuw.cloudatlas.agent;


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
        testModule.test();
    }

    public void test() throws Exception {

        this.sendMsg(ZMIHolderModule.moduleID, "dupa", new GetAttributesRequestMessage(new PathName("/uw/violet07")));
        this.sendMsg(ZMIHolderModule.moduleID, "dupa", new GetAttributesResponseMessage(null));

    }

}
