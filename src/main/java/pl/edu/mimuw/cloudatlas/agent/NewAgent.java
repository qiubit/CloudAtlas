package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.modules.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class NewAgent {
    public static void main(String[] args) throws Exception {
        System.out.println("Reading config file: " + args[0]);

        Properties properties = new Properties();
        InputStream input = new FileInputStream(args[0]);

        properties.load(input);

        Config config = new Config(properties);

        ZMIGenerator generator = new ZMIGenerator(config.getZonePath());

        // ZMIGenerator generator = new ZMIGenerator(new PathName("/bruna/24/golas"));
        ZMI rootZmi = generator.getRootZmi();
        ZMI selfZmi = generator.getSelfZmi();

        Module ZMIHolder = new ZMIHolderModule(rootZmi, selfZmi);
        Module Timer = new TimerModule();
        Module Fetcher = new FetcherModule();
        Module Gossip = new GossipModule();
    }
}
