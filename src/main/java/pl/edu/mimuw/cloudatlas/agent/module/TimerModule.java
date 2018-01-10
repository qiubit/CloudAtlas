package pl.edu.mimuw.cloudatlas.agent.module;

import pl.edu.mimuw.cloudatlas.agent.message.Message;
import pl.edu.mimuw.cloudatlas.agent.message.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.message.WakeUpRequestMessage;
import pl.edu.mimuw.cloudatlas.agent.message.WakeUpResponseMessage;

import java.util.PriorityQueue;

public class TimerModule extends Module implements MessageHandler {
    public static final String moduleID = "Timer";

    private PriorityQueue<TimerElement> timerQueue = new PriorityQueue<>();
    private Thread sleeperThread;

    private class TimerElement implements Comparable<TimerElement> {
        public final Long notifyAt;
        public final String notifiedModuleID;
        public final String contextID;

        TimerElement(Long notifyAt, String notifiedModuleID, String contextID) {
            this.notifyAt = notifyAt;
            this.notifiedModuleID = notifiedModuleID;
            this.contextID = contextID;
        }

        public int compareTo(TimerElement t) {
            return this.notifyAt.compareTo(t.notifyAt);
        }
    }

    public TimerModule() throws Exception {
        super(moduleID);
        sleeperThread = new Thread(new Sleeper());
        sleeperThread.start();
    }

    @Override
    public Message handleMessage(WakeUpRequestMessage msg) {
        synchronized (timerQueue) {
            timerQueue.add(new TimerElement(msg.wakeUpAt, msg.getFromModuleID(), msg.getContextID()));
            timerQueue.notify();
        }
        return null;
    }

    private class Sleeper implements Runnable {

        @Override
        public void run() {
            while (true) {
                synchronized (timerQueue) {
                    try {
                        Long now = System.currentTimeMillis();
                        TimerElement elem = timerQueue.peek();
                        while (elem != null && elem.notifyAt <= now) {
                            sendMsg(elem.notifiedModuleID, elem.contextID, new WakeUpResponseMessage());
                            timerQueue.poll();
                            elem = timerQueue.peek();
                            now = System.currentTimeMillis();
                        }
                        if (elem == null) {
                            timerQueue.wait();
                        } else {
                            timerQueue.wait(elem.notifyAt - now);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
