package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.modules.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

public class NewAgent {
    public static void main(String[] args) throws Exception {
        final boolean DEBUG = false;
        ZMI rootZmi;
        ZMI selfZmi;
        Integer levels;

        if (!DEBUG) {
            Properties properties = new Properties();
            InputStream input = new FileInputStream(args[0]);

            properties.load(input);

            Config.readFromProps(properties);

            ZMIGenerator generator = new ZMIGenerator(Config.getZonePath());
            rootZmi = generator.getRootZmi();
            selfZmi = generator.getSelfZmi();

            levels = Config.getZonePath().getComponents().size() + 1;
        } else {
            ZMIGenerator generator = new ZMIGenerator(new PathName("/bruna/24/golas"));
            rootZmi = generator.getRootZmi();
            selfZmi = generator.getSelfZmi();
            levels = new PathName("/bruna/24/golas").getComponents().size() + 1;
        }

        System.out.println(Config.getLocalIp());

        Module ZMIHolder = new ZMIHolderModule(rootZmi, selfZmi);
        Module Timer = new TimerModule();
        Module Fetcher = new FetcherModule();
        // Module Gossip = new GossipModule();
        Module GossipSender = new GossipSenderModule(levels);
        Module GossipReceiver = new GossipReceiverModule();
    }
}
