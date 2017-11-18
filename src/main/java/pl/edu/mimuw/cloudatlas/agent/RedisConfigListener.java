package pl.edu.mimuw.cloudatlas.agent;

import redis.clients.jedis.Jedis;

public class RedisConfigListener {
    private Jedis j;
    private static final String collectIntervalKey = "collect_interval";
    private static final String averagingPeriodKey = "averaging_period";
    private long listenInterval;
    private long lastTs;

    RedisConfigListener() {
        this.j = new Jedis("localhost");
        this.listenInterval = 1;
        this.lastTs = -1;
    }

    RedisConfigListener(long listenInterval) {
        this.j = new Jedis("localhost");
        this.listenInterval = listenInterval;
        this.lastTs = -1;
    }

    class ListenerThread extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(listenInterval * 1000);
                    System.out.println("collect_interval: " + RedisConfigListener.this.j.get(RedisConfigListener.collectIntervalKey));
                    System.out.println("averaging_period: " + RedisConfigListener.this.j.get(RedisConfigListener.averagingPeriodKey));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            }
        }
    }

    Thread getWorker() {
        return new ListenerThread();
    }
}
