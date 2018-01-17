package pl.edu.mimuw.cloudatlas.rest;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.edu.mimuw.cloudatlas.agent.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
public class Application {
    static Logger log = Logger.getLogger(Application.class.getName());
    static String signerIP;
    static String localIP;
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        InputStream input = new FileInputStream(args[0]);
        properties.load(input);
        Config.readFromProps(properties);
        signerIP = Config.getSignerIp();
        localIP = Config.getLocalIp();
        SpringApplication.run(Application.class, args);
    }
}
