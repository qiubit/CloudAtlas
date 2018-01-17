package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.modules.GossipSenderModule;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Config {
    private static boolean initOk = false;

    public static final String ZONE_NAME = "zonepath";
    public static final String LOCAL_IP = "localip";
    public static final String PUBLIC_KEY_PATH = "publickey";
    public static final String SIGNER_IP = "signerip";
    public static final String GOSSIP_STRATEGY = "gossipstrategy";
    public static final String USE_GTP = "gtp";
    public static final String ZMI_TIMEOUT = "zmitimeout";
    public static final String QUERY_EVAL_FREQ = "queryevalfreq";
    public static final String GOSSIP_FREQ = "gossipfreq";

    private static PathName zonePath = new PathName("/bruna/24/golas");
    private static String localIp = "127.0.0.1";
    private static InetAddress address = null;
    private static String publicKeyPath = "";
    private static String signerIp = "localhost";
    private static GossipSenderModule.GossipStrategy gossipStrategy = null;
    private static boolean useGtp = false;
    private static long zmiTimeout = -1;
    private static long queryEvalFreq = -1;
    private static long gossipFreq = -1;

    public static void readFromProps(Properties prop) {
        initOk = true;
        System.out.println(prop.getProperty(ZONE_NAME));
        zonePath = new PathName(prop.getProperty(ZONE_NAME));
        localIp = prop.getProperty(LOCAL_IP);
        publicKeyPath = prop.getProperty(PUBLIC_KEY_PATH);
        signerIp = prop.getProperty(SIGNER_IP);
        String gossipStrategyStr = prop.getProperty(GOSSIP_STRATEGY);
        if (gossipStrategyStr.toLowerCase().equals("rr")) {
            gossipStrategy = GossipSenderModule.GossipStrategy.GOSSIP_RR;
        } else if (gossipStrategyStr.toLowerCase().equals("random")) {
            gossipStrategy = GossipSenderModule.GossipStrategy.GOSSIP_RANDOM;
        } else if (gossipStrategyStr.toLowerCase().equals("random_exp")) {
            gossipStrategy = GossipSenderModule.GossipStrategy.GOSSIP_RANDOM_EXP;
        }
        String gtpStr = prop.getProperty(USE_GTP);
        if (gtpStr.equals("0"))
            useGtp = false;
        else if (gtpStr.equals("1"))
            useGtp = true;
        zmiTimeout = Long.parseLong(prop.getProperty(ZMI_TIMEOUT));
        queryEvalFreq = Long.parseLong(prop.getProperty(QUERY_EVAL_FREQ));
        gossipFreq = Long.parseLong(prop.getProperty(GOSSIP_FREQ));

        if (zonePath == null) {
            System.out.println("INVALID " + ZONE_NAME + " parameter in Config");
            initOk = false;
        }
        if (localIp == null) {
            System.out.println("INVALID " + LOCAL_IP + " parameter in Config");
            initOk = false;
        }
        if (publicKeyPath == null) {
            System.out.println("INVALID " + PUBLIC_KEY_PATH + " parameter in Config");
            initOk = false;
        }
        if (signerIp == null) {
            System.out.println("INVALID " + SIGNER_IP + " parameter in Config");
            initOk = false;
        }
        if (gossipStrategy == null) {
            System.out.println("INVALID " + SIGNER_IP + " parameter in Config");
            initOk = false;
        }
        if (zmiTimeout == -1) {
            System.out.println("INVALID " + ZMI_TIMEOUT + " parameter in Config");
            initOk = false;
        }
        if (queryEvalFreq == -1) {
            System.out.println("INVALID " + QUERY_EVAL_FREQ + " parameter in Config");
            initOk = false;
        }
        if (gossipFreq == -1) {
            System.out.println("INVALID " + GOSSIP_FREQ + " parameter in Config");
            initOk = false;
        }
    }

    public static PathName getZonePath() {
        return zonePath;
    }

    public static String getLocalIp() {
        return localIp;
    }

    public static InetAddress getLocalIpInetAddr() {
        if (address == null) {
            try {
                InetAddress resolved = InetAddress.getByName(localIp);
                address = resolved;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("WARNING Config: Submitted local IP " + localIp + " could not be resolved");
            }
        }
        return address;
    }

    public static String getPublicKeyPath() { return publicKeyPath; }

    public static String getSignerIp() {
        return signerIp;
    }

    public static GossipSenderModule.GossipStrategy getGossipStrategy() {
        return gossipStrategy;
    }

    public static boolean getUseGtp() {
        return useGtp;
    }

    public static long getZmiTimeout() {
        return zmiTimeout;
    }

    public static long getQueryEvalFreq() {
        return queryEvalFreq;
    }

    public static long getGossipFreq() {
        return gossipFreq;
    }

    public static boolean getInitOk() {
        return initOk;
    }

    public static String printConfig() {
        String repr = "CONFIG\n";
        repr += ZONE_NAME + ": " + zonePath.toString() + "\n";
        repr += LOCAL_IP + ": " + localIp + "\n";
        repr += PUBLIC_KEY_PATH + ": " + publicKeyPath + "\n";
        repr += SIGNER_IP + ": " + signerIp + "\n";
        repr += GOSSIP_STRATEGY + ": " + gossipStrategy + "\n";
        repr += USE_GTP + ": " + useGtp + "\n";
        repr += ZMI_TIMEOUT + ": " + zmiTimeout + "\n";
        repr += QUERY_EVAL_FREQ + ": " + queryEvalFreq + "\n";
        repr += GOSSIP_FREQ + ": " + gossipFreq + "\n";
        repr += "\n";
        System.out.println(repr);
        return repr;
    }
}
