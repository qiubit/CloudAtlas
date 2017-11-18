package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.agent.AgentApi;
import pl.edu.mimuw.cloudatlas.model.*;
import redis.clients.jedis.Jedis;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class RedisFetcher implements Runnable {
    private AgentApi agentApi;
    private Jedis redisClient;
    private PathName zone;
    private static final Map<String, Type> attributeToValueClass;
    static {
        attributeToValueClass = new HashMap<>();
        attributeToValueClass.put("total_swap", TypePrimitive.DOUBLE);
        attributeToValueClass.put("total_disk", TypePrimitive.DOUBLE);
        attributeToValueClass.put("logged_users", TypePrimitive.DOUBLE);
        attributeToValueClass.put("total_ram", TypePrimitive.DOUBLE);
        attributeToValueClass.put("free_disk", TypePrimitive.DOUBLE);
        attributeToValueClass.put("num_processes", TypePrimitive.DOUBLE);
        attributeToValueClass.put("free_swap", TypePrimitive.DOUBLE);
        attributeToValueClass.put("cpu_load", TypePrimitive.DOUBLE);
        attributeToValueClass.put("free_ram", TypePrimitive.DOUBLE);

    }

    RedisFetcher(AgentApi agentAPi, Jedis redisClient, PathName zone) {
        this.agentApi = agentAPi;
        this.redisClient = redisClient;
        this.zone = zone;
    }

    public void run() {
        try {

            for (String attribute : redisClient.keys("*_avg")) {
                String value = redisClient.lrange(attribute, 0, 0).get(0).split(":")[1];
                String attributeName = attribute.substring(0, attribute.length() - 4);
                Type valueType = attributeToValueClass.get(attributeName);
                agentApi.setAttribute(zone, new Attribute(attributeName), new ValueString(value).convertTo(valueType));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class Fetcher {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) {
        try {
            if (System.getSecurityManager() == null) {
                SecurityManager m = new SecurityManager();
                System.setSecurityManager(m);
            }
            Registry registry = LocateRegistry.getRegistry("localhost");
            AgentApi agentApi = (AgentApi) registry.lookup("AgentApi");
            Jedis redisClient = new Jedis("localhost");
            scheduler.scheduleAtFixedRate(new RedisFetcher(agentApi, redisClient, new PathName("/uw/violet07")), 10, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("RMI agentapi problem");
        }

    }
}
