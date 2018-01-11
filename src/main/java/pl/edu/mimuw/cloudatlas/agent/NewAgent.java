package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.modules.FetcherModule;
import pl.edu.mimuw.cloudatlas.modules.Module;
import pl.edu.mimuw.cloudatlas.modules.TimerModule;
import pl.edu.mimuw.cloudatlas.modules.ZMIHolderModule;

public class NewAgent {
    public static void main(String[] args) throws Exception {
        Module ZMIHolder = new ZMIHolderModule(ZMIHolderModule.createTestHierarchy());
        Module Timer = new TimerModule();
        Module Fetcher = new FetcherModule();
    }
}
