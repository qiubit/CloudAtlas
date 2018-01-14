package pl.edu.mimuw.cloudatlas.modules;

import com.rabbitmq.client.*;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GossipModule extends Module implements MessageHandler {
    private static final String moduleID = "Gossip";

    private static final String GOSSIP_RR = "round_robbin";
    private static final String GOSSIP_RANDOM = "random";
    private static final String GOSSIP_RANDOM_EXP = "random_exponential";

    private static final Long ZMI_TIMEOUT_DEFAULT = 30000L;
    private static final Long GOSSIP_INTERVAL = 5000L;

    private final String GOSSIP_MODE;
    private final Long zmiTimeout;
    private final Long gossipInterval;

    private Integer lastLevel = null;
    private String currentLevel = null;
    private HashMap<String, ZMI> currentLocal = null;
    private HashMap<String, ZMI> currentRemote = null;

    private Connection remoteConnection = null;
    private Channel remoteChannel = null;

    public GossipModule() throws Exception {
        super(moduleID);
        this.GOSSIP_MODE = GOSSIP_RANDOM;
        this.zmiTimeout = ZMI_TIMEOUT_DEFAULT;
        this.gossipInterval = GOSSIP_INTERVAL;
        startGossip();
    }

    public GossipModule(String gossipStrategy) throws Exception {
        super(moduleID);
        if (!gossipStrategy.equals(GOSSIP_RR)
                && !gossipStrategy.equals(GOSSIP_RANDOM)
                && !gossipStrategy.equals(GOSSIP_RANDOM_EXP)) {
            throw new IllegalArgumentException("Gossip: Invalid gossip strategy selected");
        }
        this.GOSSIP_MODE = gossipStrategy;
        this.zmiTimeout = ZMI_TIMEOUT_DEFAULT;
        this.gossipInterval = GOSSIP_INTERVAL;
        startGossip();
    }

    public GossipModule(String gossipStrategy, long zmiTimeout, long gossipInterval) throws Exception {
        super(moduleID);
        if (!gossipStrategy.equals(GOSSIP_RR)
                && !gossipStrategy.equals(GOSSIP_RANDOM)
                && !gossipStrategy.equals(GOSSIP_RANDOM_EXP)) {
            throw new IllegalArgumentException("Gossip: Invalid gossip strategy selected");
        }
        this.GOSSIP_MODE = gossipStrategy;
        this.zmiTimeout = zmiTimeout;
        this.gossipInterval = GOSSIP_INTERVAL;
        startGossip();
    }

    private void startGossip() throws IOException {
        sendMsg(GossipModule.moduleID, "", new InitiateGossipMessage(), Module.SERIALIZED_TYPE);
    }

    private class GossipProcessor {
        private boolean processed = false;

        private final HashMap<String, ZMI> zmiLocal;
        private final HashMap<String, ZMI> zmiRemote;
        private final long zmiTimeout;
        private final long processingBeginTime;

        private HashSet<String> zmiSeen = new HashSet<>();

        private HashMap<String, ZMI> zmiMerged = new HashMap<>();

        public GossipProcessor(HashMap<String, ZMI> local, HashMap<String, ZMI> remote, long zmiTimeout) {
            this.zmiLocal = local;
            this.zmiRemote = remote;
            this.zmiTimeout = zmiTimeout;
            this.processingBeginTime = System.currentTimeMillis();
        }

        private void process() {
            for (Map.Entry<String, ZMI> e : zmiLocal.entrySet()) {
                ZMI toMerge = e.getValue();
                if (this.processingBeginTime - toMerge.getTimestamp() > zmiTimeout) {
                    continue;
                }
                if (zmiSeen.contains(e.getKey())) {
                    ZMI currentlyMerged = zmiMerged.get(e.getKey());
                    if (currentlyMerged.getTimestamp() > toMerge.getTimestamp()) {
                        toMerge = currentlyMerged;
                    }
                    zmiMerged.put(e.getKey(), toMerge);
                } else {
                    zmiMerged.put(e.getKey(), e.getValue());
                    zmiSeen.add(e.getKey());
                }
            }
        }

        public HashMap<String, ZMI> getMergedZmi() {
            if (!processed)
                this.process();
            return this.zmiMerged;
        }
    }

    @Override
    public Message handleMessage(InitiateGossipMessage msg) {
        System.out.println("Gossip: Initializing gossip");
        if (this.remoteConnection != null) {
            try {
                this.remoteConnection.close();
            } catch (IOException e) {
                System.out.println("Gossip: could not close connection");
                e.printStackTrace();
            }
        }
        this.currentLocal = null;
        this.currentRemote = null;
        this.remoteConnection = null;
        this.remoteChannel = null;
        Message reqMsg = new GetZMILevelsRequestMessage();
        try {
            sendMsg(ZMIHolderModule.moduleID, null, reqMsg, Module.SERIALIZED_TYPE, GossipModule.moduleID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<String> getSiblingPaths(HashMap<String, ZMI> zmis, String pathStr) {
        ArrayList<String> ret = new ArrayList<>();

        PathName path = new PathName(pathStr);
        path = path.levelUp();
        for (Map.Entry<String, ZMI> e : zmis.entrySet()) {
            PathName curPath = new PathName(e.getKey());
            if (curPath.levelUp().equals(path)) {
                String curPathStr = curPath.toString();
                if (!curPathStr.equals(pathStr))
                    ret.add(curPathStr);
            }
        }

        return ret;
    }

    private void addContacts(ZMI zmi, List<ValueContact> contacts) {
        Type expectedType = new TypeCollection(Type.PrimaryType.SET, TypePrimitive.CONTACT);
        Value contactsVal = zmi.getAttributes().getOrNull("contacts");
        if (contactsVal != null) {
            ValueSet contactsSet = (ValueSet) contactsVal.convertTo(expectedType);
            for (Value v : contactsSet) {
                ValueContact contact = (ValueContact) v.convertTo(TypePrimitive.CONTACT);
                contacts.add(contact);
            }
        }
    }

    private Message localGossipInfoHandler(GetZMIGossipInfoResponseMessage msg) {
        HashMap<String, ZMI> localZmis = msg.relevantZMIs;
        ArrayList<ValueContact> fallbackContacts = msg.fallbackContacts;
        ArrayList<ValueContact> contacts = new ArrayList<>();
        this.currentLocal = localZmis;

        System.out.println("Gossip: Local gossip info " + localZmis);

        ArrayList<String> siblingPaths = getSiblingPaths(localZmis, this.currentLevel);
        for (String siblingPath : siblingPaths) {
            addContacts(localZmis.get(siblingPath), contacts);
        }

        Collections.shuffle(contacts, new Random());
        ArrayList<Address> addressesList = new ArrayList<>();
        for (ValueContact contact : contacts) {
            addressesList.add(new Address(contact.getAddress().getHostAddress()));
        }
        for (ValueContact contact : fallbackContacts) {
            addressesList.add(new Address(contact.getAddress().getHostAddress()));
        }

        Address[] addresses = new Address[addressesList.size()];
        addresses = addressesList.toArray(addresses);

        boolean communicationFail = false;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("cloudatlas");
        factory.setPassword("cloudatlas");
        try {
            Message toSend = new GetZMIGossipInfoRequestMessage(this.currentLevel);
            toSend.setReceiverQueueName(ZMIHolderModule.moduleID);

            this.remoteConnection = factory.newConnection(addresses);
            this.remoteChannel = remoteConnection.createChannel();
            String remoteAddress = this.remoteChannel.getConnection().getAddress().getHostAddress();
            System.out.println("Gossip: gossipping with " + remoteAddress);
            remoteChannel.queueDeclare(ZMIHolderModule.moduleID, false, false, false, null);

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .replyTo(GossipModule.moduleID)
                    .contentType(Module.SERIALIZED_TYPE)
                    .build();
            remoteChannel.basicPublish("", ZMIHolderModule.moduleID, props, toSend.toBytes());
        } catch (Exception e) {
            e.printStackTrace();
            communicationFail = true;
        } finally {
            if (communicationFail) {
                System.out.println("Gossip: Gossip fail, reinitializing...");
                reinitializeGossip();
                return null;
            }
        }
        return null;
    }

    private Message remoteGossipInfoHandler(GetZMIGossipInfoResponseMessage msg) {
        HashMap<String, ZMI> remoteZmis = msg.relevantZMIs;
        this.currentRemote = remoteZmis;

        System.out.println("Gossip: local ZMI info " + currentLocal);
        System.out.println("Gossip: remote ZMI info " + remoteZmis);

        GossipProcessor processor = new GossipProcessor(currentLocal, currentRemote, this.zmiTimeout);
        HashMap<String, ZMI> zmiMerged = processor.getMergedZmi();
        System.out.println("Gossip: merged ZMI info " + zmiMerged);

        Message ret = new GossippedZMIMessage(zmiMerged);
        ret.setReceiverQueueName(ZMIHolderModule.moduleID);

        Message nextGossipMsg = new ScheduledMessage(new InitiateGossipMessage(), GOSSIP_INTERVAL);
        nextGossipMsg.setReceiverQueueName(moduleID);
        try {
            sendMsg(TimerModule.moduleID, null, nextGossipMsg, Module.SERIALIZED_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }



    @Override
    public Message handleMessage(GetZMIGossipInfoResponseMessage msg) {
        Message ret;
        if (currentLocal == null)
            ret = localGossipInfoHandler(msg);
        else
            ret = remoteGossipInfoHandler(msg);
        return ret;
    }

    private void reinitializeGossip() {
        Message ret = new ScheduledMessage(new InitiateGossipMessage(), GOSSIP_INTERVAL);
        ret.setReceiverQueueName(moduleID);
        try {
            sendMsg(TimerModule.moduleID, null, ret, Module.SERIALIZED_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
            reinitializeGossip();
        }
    }

    @Override
    public Message handleMessage(GetZMILevelsResponseMessage msg) {
        System.out.println("Gossip: Received levels data from ZMI");
        HashSet<String> levels = msg.levels;

        if (levels.size() == 0) {
            throw new IllegalArgumentException("Gossip: Received empty level set");
        } else {
            System.out.println("Gossip: Levels - " + levels);
        }

        int idx;
        int levelLowerIdx = 0;
        int levelUpperIdx = levels.size();

        if (GOSSIP_MODE.equals(GOSSIP_RR)) {
            if (lastLevel == null) {
                levelLowerIdx = 0;
                levelUpperIdx = levelLowerIdx + 1;
            } else {
                levelLowerIdx = (lastLevel + 1) % levels.size();
                levelUpperIdx = levelLowerIdx + 1;
            }
        }

        idx = ThreadLocalRandom.current().nextInt(levelLowerIdx, levelUpperIdx);

        String[] arr = levels.toArray(new String[levels.size()]);
        String gossipLevel = arr[idx];

        System.out.println("Gossip: Gossiping for level " + gossipLevel + " [strategy " + GOSSIP_MODE + "]");
        this.currentLevel = gossipLevel;

        Message ret = new GetZMIGossipInfoRequestMessage(gossipLevel);
        ret.setReceiverQueueName(ZMIHolderModule.moduleID);

        try {
            sendMsg(ZMIHolderModule.moduleID, null, ret, Module.SERIALIZED_TYPE, GossipModule.moduleID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
