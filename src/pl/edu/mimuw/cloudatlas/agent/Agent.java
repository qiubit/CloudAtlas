package pl.edu.mimuw.cloudatlas.agent;


public class Agent {
    public static void main(String[] args) {
        RedisKeyListener rkl = new RedisKeyListener("cpu_load");
        rkl.getWorker().start();
    }
}
