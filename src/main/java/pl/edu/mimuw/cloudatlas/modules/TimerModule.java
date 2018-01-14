package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.messages.Message;
import pl.edu.mimuw.cloudatlas.messages.ScheduledMessage;

import java.io.IOException;

public class TimerModule extends Module {

    public static final String moduleID = "Timer";

    public TimerModule() throws Exception {
        super(moduleID);
    }

    private class MessageSender implements Runnable {
        private final Message msg;
        private final long delayMillis;
        private final String receiverQueue;

        public MessageSender(Message msg, long delayMillis, String receiverQueue) {
            this.msg = msg;
            this.delayMillis = delayMillis;
            this.receiverQueue = receiverQueue;
        }

        @Override
        public void run() {

                try {
                    System.out.println("Timer: Sending message in " + this.delayMillis + " ms");
                    System.out.println("Timer: Current time: " + System.currentTimeMillis() + " ms");
                    Thread.sleep(this.delayMillis);
                    System.out.println("Timer: Sending message to " + this.receiverQueue);
                    System.out.println("Timer: Current time: " + System.currentTimeMillis() + " ms");
                    synchronized (TimerModule.class) {
                        sendMsg(this.receiverQueue, "", msg, Module.SERIALIZED_TYPE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }
    @Override
    public Message handleMessage(ScheduledMessage msg) throws IllegalArgumentException {
        if (msg.getReceiverQueueName() == null) {
            throw new IllegalArgumentException("Timer: ScheduledMessage should have receiver queue name");
        }
        new Thread(new MessageSender(msg.msg, msg.delayInMillis, msg.getReceiverQueueName())).start();
        return null;
    }
}
