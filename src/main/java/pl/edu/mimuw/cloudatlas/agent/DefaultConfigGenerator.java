package pl.edu.mimuw.cloudatlas.agent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class DefaultConfigGenerator {
    public static void main(String args[]) {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            output = new FileOutputStream("agentConfig.properties");

            // set the properties value
            prop.setProperty(Config.ZONE_NAME, "/bruna/24/golas");

            // save properties to project root folder
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
