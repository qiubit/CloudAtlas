package pl.edu.mimuw.cloudatlas.agent;

import redis.clients.jedis.Jedis;

public class RedisKeyListener {
    private Jedis j;
    private String key;
    private long listenInterval;
    private long lastTs;

    RedisKeyListener(String key, long listenInterval) {
        this.j = new Jedis("localhost");
        this.key = key;
        this.listenInterval = listenInterval;
        this.lastTs = -1;
    }

    RedisKeyListener(String key) {
        this.j = new Jedis("localhost");
        this.key = key;
        this.listenInterval = 60;
        this.lastTs = -1;
    }

    class ListenerThread extends Thread {
        public void run() {
            System.out.println(key + ": " + RedisKeyListener.this.j.lrange(key, 0, -1));
        }
    }

    Thread getWorker() {
        return new ListenerThread();
    }
}
