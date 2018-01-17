package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.agent.Config;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ZMIHolderModule extends Module implements MessageHandler {

    public static final String moduleID = "ZMIHolder";
    private final long QUERY_EVAL_FREQ = 5000;
    private final long ZMI_TIMEOUT = 40000L;
    private final boolean USE_GTP = false;

    private ZMI root;
    private ZMI self;

    private HashMap<Attribute, QueryInformation> queries = new HashMap<>();
    private HashSet<String> gossipLevels = new HashSet<>();
    private HashMap<String, ZMI> pathToZmi = new HashMap<>();
    private HashMap<String, List<ZMI>> siblingZmis = new HashMap<>();
    private HashMap<ZMI, String> zmiFullPaths = new HashMap<>();
    private HashMap<String, HashSet<InetAddress>> contacts = new HashMap<>();
    private HashSet<ZMI> rootSelfPathZmis = new HashSet<>();

    // Indices leading from root to self
    private final ArrayList<Integer> rootSelfZmiIndices;

    private ArrayList<Integer> computeRootSelfPathNames(ZMI root, ZMI self) {
        ArrayList<String> zmiNames = new ArrayList<>();

        ZMI current = self;
        while (current != root) {
            String name = current.getAttributes().get("name").toString();
            if (name == null)
                throw new IllegalArgumentException("ZMI with no name attribute");
            zmiNames.add(name);
            current = current.getFather();
        }

        ArrayList<Integer> computed = new ArrayList<>();
        Collections.reverse(zmiNames);
        current = root;
        for (String name : zmiNames) {
            ZMI next = null;
            int idx;
            for (idx = 0; idx < current.getSons().size(); idx++) {
                ZMI son = current.getSons().get(idx);
                if (son.getAttributes().get("name").toString().equals(name)) {
                    next = son;
                    break;
                }
            }
            if (next == null)
                throw new IllegalArgumentException("No path from root to self");
            computed.add(idx);
            current = next;
        }
        return computed;
    }

    public ZMIHolderModule(ZMI root, ZMI self) throws Exception {
        super(moduleID);
        this.root = root;
        this.self = self;
        if (this.root.getFather() != null)
            throw new IllegalArgumentException("Root father should be null");

        HashMap<String, HashSet<InetAddress>> defaultContacts = new HashMap<>();
        this.rootSelfZmiIndices = computeRootSelfPathNames(root, self);
        int nextIdx = 0;

        // ZMI metadata computation
        ZMI current = root;
        PathName curPath = new PathName("/");
        while (current != null) {
            // current ZMI metadata update
            defaultContacts.put(curPath.toString(), new HashSet<>());
            defaultContacts.get(curPath.toString()).add(Config.getLocalIpInetAddr());
            this.pathToZmi.put(curPath.toString(), current);
            this.siblingZmis.put(curPath.toString(), new ArrayList<>());
            this.zmiFullPaths.put(current, curPath.toString());

            // current ZMI siblings metadata update
            ZMI father = current.getFather();
            if (father != null) {
                this.gossipLevels.add(curPath.toString());
                for (ZMI zmi : father.getSons()) {
                    if (!(zmi == current)) {
                        String siblingPath =
                                curPath.levelUp().levelDown(zmi.getAttributes().get("name").toString()).toString();
                        this.siblingZmis.get(curPath.toString()).add(zmi);
                        this.zmiFullPaths.put(
                                zmi,
                                siblingPath
                        );
                        this.pathToZmi.put(siblingPath, zmi);
                    }
                }
            }

            List<ZMI> currentSons = current.getSons();
            if (currentSons.size() == 0)
                current = null;
            else {
                current = currentSons.get(rootSelfZmiIndices.get(nextIdx));
                curPath = curPath.levelDown(current.getAttributes().get(new Attribute("name")).toString());
                nextIdx++;
            }
        }

        this.contacts = defaultContacts;

        computeRootSelfZmis();
        evaluateAllQueries();
    }

    private void computeRootSelfZmis() {
        rootSelfPathZmis.add(root);
        ZMI current = root;
        for (Integer idx : rootSelfZmiIndices) {
            current = current.getSons().get(idx);
            rootSelfPathZmis.add(current);
        }
    }

    public HashMap<Attribute, QueryInformation> getQueries() {
        return new HashMap<>(queries);
    }

    private HashSet<InetAddress> getFallbackContactsForPath(PathName path) {
        return contacts.get(path.toString());
    }

    private void executeQueries(ZMI zmi, String query) throws Exception {
        if (!zmi.getSons().isEmpty()) {
            for (ZMI son : zmi.getSons())
                executeQueries(son, query);
            Interpreter interpreter = new Interpreter(zmi);
            Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
            try {
                List<QueryResult> result = interpreter.interpretProgram((new parser(lex)).pProgram());
                for (QueryResult r : result) {
                    zmi.getAttributes().addOrChange(r.getName(), r.getValue());
                }
                if (result.size() > 0)
                    zmi.updateTimestamp();
            } catch (InterpreterException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void evaluateAllQueries() throws Exception {
        for (QueryInformation query : getQueries().values()) {
            executeQueries(root, query.getQuery());
        }
        Message scheduleQueriesMsg = new ScheduledMessage(new ExecuteQueriesMessage(), QUERY_EVAL_FREQ);
        scheduleQueriesMsg.setReceiverQueueName(ZMIHolderModule.moduleID);
        sendMsg(TimerModule.moduleID, "", scheduleQueriesMsg, Module.SERIALIZED_TYPE);
        deleteInactiveZmis();
    }

    // TODO: May break if root-self path is not left path in ZMI tree
    private void deleteInactiveZmis() {
        long currentTime = System.currentTimeMillis();
        ArrayList<String> pathsToDelete = new ArrayList<>();
        ArrayList<ZMI> zmisToDelete = new ArrayList<>();
        for (Map.Entry<String, ZMI> e : pathToZmi.entrySet()) {
            if (!rootSelfPathZmis.contains(e.getValue())
                    && currentTime - e.getValue().getTimestamp() > ZMI_TIMEOUT) {
                System.out.println(moduleID + ": will delete " + e.getKey());
                System.out.println(moduleID + ": timestamps " + currentTime + " " + e.getValue().getTimestamp());
                pathsToDelete.add(e.getKey());
                zmisToDelete.add(e.getValue());
            }
        }
        for (ZMI z : zmisToDelete) {
            z.getFather().removeSon(z);
            zmiFullPaths.remove(z);
            for (Map.Entry<String, List<ZMI>> e : siblingZmis.entrySet()) {
                e.getValue().remove(z);
            }
        }
        for (String s : pathsToDelete) {
            pathToZmi.remove(s);
        }
    }

    private void evaluateAllQueriesWithoutScheduling() throws Exception {
        for (QueryInformation query : getQueries().values()) {
            executeQueries(root, query.getQuery());
        }
        deleteInactiveZmis();
    }

    public static HashMap<ZMI, List<QueryResult>> getResultsForNonSingletonZMIs(ZMI zmi, String query) {
        HashMap<ZMI, List<QueryResult>> result = new HashMap<>();
        addNonSingletonZMIResults(zmi, query, result);
        return result;
    }

    public static List<QueryResult> evaluateQueryOnZMI(ZMI zmi, String query) throws Exception {
        Interpreter interpreter = new Interpreter(zmi);
        Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
        return interpreter.interpretProgram((new parser(lex)).pProgram());
    }

    private static void addNonSingletonZMIResults(ZMI zmi, String query, HashMap<ZMI, List<QueryResult>> results) {
        if (zmi.getSons().size() > 0) {
            try {
                results.put(zmi, evaluateQueryOnZMI(zmi, query));
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (ZMI son : zmi.getSons()) {
                addNonSingletonZMIResults(son, query, results);
            }
        }
    }

    public HashMap<String, HashSet<InetAddress>> getContacts() {
        return contacts;
    }

    private void setContactsFromRest(ArrayList<ValueContact> newContacts) {
        for (ValueContact contact : newContacts) {
            if (contacts.get(contact.getName().toString()) != null) {
                contacts.get(contact.getName().toString()).add(contact.getAddress());
            }
        }
    }

    private PathName getPathName(ZMI zmi) {
        String name = ((ValueString) zmi.getAttributes().get("name")).getValue();
        return zmi.getFather() == null ? PathName.ROOT : getPathName(zmi.getFather()).levelDown(name);
    }

    private ZMI pathNameToZMI(PathName path) {
        List<String> components = path.getComponents();
        ZMI zmi = root;
        for (int i = 0; i < components.size(); ++i) {
            ZMI newZmi = null;
            for (ZMI son : zmi.getSons()) {
                if (((ValueString) son.getAttributes().get("name")).getValue().equals(components.get(i))) {
                    newZmi = son;
                    break;
                }
            }
            if (newZmi == null) throw new InvalidParameterException("Not existent zone");
            zmi = newZmi;
        }
        return zmi;
    }

    private void setAttribute(Attribute attr, Value value) {
        ZMI zmi = self;
        if (zmi.getSons().size() > 0) return;
        zmi.getAttributes().addOrChange(attr, value);
    }

    private void _getAvailableZones(ZMI zmi, List<PathName> zones) {
        zones.add(getPathName(zmi));
        for (ZMI son : zmi.getSons()) {
            _getAvailableZones(son, zones);
        }
    }

    private PathName remoteToLocalPath(PathName remotePath) {
        if (pathToZmi.get(remotePath.toString()) != null)
            return remotePath;
        PathName fatherPath = remotePath.levelUp();
        if (pathToZmi.get(fatherPath.toString()) == null)
            return null;
        ZMI father = pathToZmi.get(fatherPath.toString());
        if (father.getSons().size() == 0)
            return null;
        ZMI child = father.getSons().get(rootSelfZmiIndices.get(fatherPath.getComponents().size()));
        String childName = child.getAttributes().get("name").toString();
        return fatherPath.levelDown(childName);
    }

    private void installQuery(Attribute name, String query) {
        if (!Attribute.isQuery(name)) throw new InvalidParameterException("Attribute not a query");
        if (queries.containsKey(name)) throw new InvalidParameterException("Attribute already exists");
        HashMap<ZMI, List<QueryResult>> query_results = getResultsForNonSingletonZMIs(root, query);
        if (query_results.size() == 0) throw new InvalidParameterException("Cannot evaluate query on any ZMI");
        ArrayList<Attribute> result_attributes = new ArrayList<>(query_results.values().iterator().next()
                .stream().map(result -> result.getName()).collect(Collectors.toList()));
        for (Attribute attr : result_attributes) {
            if (anyNonSingletonZMIContainsAttribute(root, attr))
                throw new InvalidParameterException("Query conflicts with existing parameter");
        }

        for (Map.Entry<ZMI, List<QueryResult>> zmi_results : query_results.entrySet()) {
            for (QueryResult result : zmi_results.getValue()) {
                zmi_results.getKey().getAttributes().add(result.getName(), result.getValue());
            }
        }
        queries.put(name, new QueryInformation(query, result_attributes));
    }

    private void uninstallQuery(Attribute name) {
        QueryInformation query_info = queries.get(name);
        if (query_info != null) {
            for (Attribute attr : query_info.getAttributes()) {
                removeAttributeFromAllNonSingletonZMIs(root, attr);
            }
            queries.remove(name);
        }
    }

    private static boolean anyNonSingletonZMIContainsAttribute(ZMI zmi, Attribute attr) {
        if (zmi.getSons().size() == 0) return false;
        if (zmi.getAttributes().getOrNull(attr) != null) return true;
        for (ZMI son : zmi.getSons()) {
            if (anyNonSingletonZMIContainsAttribute(son, attr)) return true;
        }
        return false;
    }

    private static void removeAttributeFromAllNonSingletonZMIs(ZMI zmi, Attribute attr) {
        if (zmi.getSons().size() > 0) {
            zmi.getAttributes().remove(attr);
            for (ZMI son : zmi.getSons()) {
                removeAttributeFromAllNonSingletonZMIs(son, attr);
            }
        }
    }

    private void updateQueries(HashMap<Attribute, QueryInformation> newQueries) {
        if (newQueries != null) {
            for (Map.Entry<Attribute, QueryInformation> e : newQueries.entrySet()) {
                if (queries.get(e.getKey()) == null) {
                    QueryInformation insertedQuery = e.getValue();
                    insertedQuery.updateTimestamp();
                    queries.put(e.getKey(), insertedQuery);
                } else {
                    if (queries.get(e.getKey()).getTimestamp() < e.getValue().getTimestamp()) {
                        QueryInformation insertedQuery = e.getValue();
                        insertedQuery.updateTimestamp();
                        queries.put(e.getKey(), insertedQuery);
                    }
                }
            }
        }
    }

    private void updateContacts(HashMap<String, HashSet<InetAddress>> newContacts) {
        for (Map.Entry<String, HashSet<InetAddress>> e : newContacts.entrySet()) {
            PathName localPath = remoteToLocalPath(new PathName(e.getKey()));
            if (localPath != null) {
                HashSet<InetAddress> localContacts =
                        contacts.computeIfAbsent(localPath.toString(), k -> new HashSet<>());
                localContacts.addAll(e.getValue());
            }
        }
    }

    @Override
    public Message handleMessage(GetAttributesRequestMessage msg) {
        ZMI zmi = pathNameToZMI(msg.zonePath);
        return new GetAttributesResponseMessage(zmi.getAttributes().clone());
    }

    @Override
    public Message handleMessage(GetContactsRequestMessage msg) {
        ArrayList<ValueContact> ret = new ArrayList<>();
        for (Map.Entry<String, HashSet<InetAddress>> e : contacts.entrySet()) {
            for (InetAddress addr : e.getValue()) {
                ret.add(new ValueContact(new PathName(e.getKey()), addr));
            }
        }
        return new GetContactsResponseMessage(ret);
    }

    @Override
    public Message handleMessage(GetZonesRequestMessage msg) {
        List<PathName> zones = new ArrayList<>();
        _getAvailableZones(root, zones);
        return new GetZonesResponseMessage(zones);
    }

    @Override
    public Message handleMessage(SetAttributeMessage msg) {
        setAttribute(msg.getAttribute(), msg.getValue());
        self.updateTimestamp();
        return null;
    }

    @Override
    public Message handleMessage(InstallQueryMessage msg) {
        try {
            installQuery(msg.name, msg.query);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(moduleID + ": Query couldn't be installed [" + msg.query + "]");
        }
        return null;
    }

    @Override
    public Message handleMessage(UninstallQueryMessage msg) {
        try {
            uninstallQuery(msg.name);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(moduleID + ": Query couldn't be uninstalled [" + msg.name + "]");
        }
        return null;
    }

    @Override
    public Message handleMessage(SetContactsMessage msg) {
        setContactsFromRest(msg.new_contacts);
        return null;
    }

    @Override
    public Message handleMessage(GetQueriesRequestMessage msg) {
        return new GetQueriesResponseMessage(new HashMap<>(queries));
    }

    @Override
    public Message handleMessage(ExecuteQueriesMessage msg) {
        try {
            evaluateAllQueries();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Message handleMessage(GetZMILevelsRequestMessage msg) {
        return new GetZMILevelsResponseMessage(this.gossipLevels);
    }

    @Override
    public Message handleMessage(GetGossipMetadataRequestMessage msg) {
        Integer requestedLevel = msg.requestedLevelNum;

        // Discard - invalid level num
        if (requestedLevel <= 0 || requestedLevel > rootSelfZmiIndices.size())
            return null;

        // Fetch requested level path
        Integer currentLevel = 0;
        PathName currentPath = new PathName("/");
        ZMI currentZmi = root;
        while (!currentLevel.equals(requestedLevel)) {
            currentZmi = currentZmi.getSons().get(rootSelfZmiIndices.get(currentLevel));
            String zmiName = currentZmi.getAttributes().get("name").toString();
            currentPath = currentPath.levelDown(zmiName);
            currentLevel += 1;
        }

        // Fetch contacts
        HashSet<InetAddress> contactsList = new HashSet<>(contacts.get(currentPath.toString()));

        Message ret = new GetGossipMetadataResponseMessage(currentLevel, currentPath, contactsList);
        ret.setReceiverQueueName(GossipSenderModule.moduleID);
        return ret;
    }

    @Override
    public Message handleMessage(GetZMIGossipInfoRequestMessage msg) {
        // Fetch requested gossip level from message
        String gossipLevel = msg.requestedLevel;

        // This will collect all relevant ZMIs (requested level siblings + all nodes up the tree except root)
        HashMap<String, ZMI> relevantZmis = new HashMap<>();

        PathName fatherPath = new PathName(gossipLevel).levelUp();
        ZMI father = pathToZmi.get(fatherPath.toString());
        if (father == null) {
            System.out.println(moduleID + ": Invalid data for GetZMIGossipInfoRequestMessage - parent not present in tree");
            return null;
        }
        Integer fatherLevel = fatherPath.getComponents().size();
        if (fatherLevel >= rootSelfZmiIndices.size()) {
            System.out.println(moduleID + ": Invalid data for GetZMIGossipInfoRequestMessage - sibling not present in tree");
            return null;
        }
        ZMI current = father.getSons().get(rootSelfZmiIndices.get(fatherLevel));
        if (current == null) {
            System.out.println(moduleID + ": Invalid data for GetZMIGossipInfoRequestMessage - sibling not present in tree");
            return null;
        }
        while (current != root) {
            String curPath = zmiFullPaths.get(current);
            List<ZMI> curLevelSiblings = new ArrayList<>();
            curLevelSiblings.add(current);
            if (siblingZmis.get(curPath) != null)
                curLevelSiblings.addAll(siblingZmis.get(curPath));
            for (ZMI sibling : curLevelSiblings) {
                relevantZmis.put(this.zmiFullPaths.get(sibling), sibling);
            }
            current = current.getFather();
        }

        Message ret = new GetZMIGossipInfoResponseMessage(
                gossipLevel,
                relevantZmis,
                contacts,
                queries
        );
        ret.setReceiverHostname(msg.getSenderHostname());
        ret.setReceiverQueueName(msg.getSenderQueueName());

        return ret;
    }

    // "Leader" sibling is a sibling at fixed level, which is on path from self to root
    // Node's father must already be set to correct one from inside holded ZMI tree
    private ZMI getLeaderSiblingForZmi(ZMI node) {
        int nextIdx = 0;
        ZMI father = node.getFather();
        ZMI current = root;
        while (current != father) {
            current = current.getSons().get(this.rootSelfZmiIndices.get(nextIdx));
            nextIdx++;
        }
        return current.getSons().get(this.rootSelfZmiIndices.get(nextIdx));
    }

    private void adjustTimestamps(HashMap<String, ZMI> gosippedZmi,
                                  HashMap<String, Long> timestamps,
                                  boolean localIsSender) {
        System.out.println(moduleID + ": Adjusting timestamps using GTP");
        System.out.println(moduleID + ": tsa " + timestamps.get("tsa") + " tsb: " + timestamps.get("tsb") +
        " tra: " + timestamps.get("tra") + " trb: " + timestamps.get("trb"));
        long rtd = (timestamps.get("tsb") - timestamps.get("tsa")) - (timestamps.get("trb") - timestamps.get("tra"));
        System.out.println(moduleID + ": rtd " + rtd);
        long dT = (long) Math.ceil(timestamps.get("trb") + 0.5 * rtd - timestamps.get("tsb"));
        System.out.println(moduleID + ": dT " + dT);
        if (localIsSender) {
            System.out.println(moduleID + ": local is sender");
            dT = -dT;
        }
        for (Map.Entry<String, ZMI> e : gosippedZmi.entrySet()) {
            e.getValue().setTimestamp(e.getValue().getTimestamp() + dT);
        }
    }

    @Override
    public Message handleMessage(GossippedZMIMessage msg) {
        // We need to rebuild entire ZMI structure based on gossippedZmi
        // and the ZMI info we've got saved currently

        // Update info
        HashMap<String, ZMI> gossippedZmi = msg.gossippedZmi;
        if (USE_GTP)
            adjustTimestamps(gossippedZmi, msg.getTimestamps(), msg.getLocalIsSender());
        System.out.println(moduleID + ": Gossipped ZMI " + gossippedZmi);
        filterGossipedZmi(gossippedZmi);

        for (Map.Entry<String, ZMI> e : gossippedZmi.entrySet()) {
            // Always add ZMI in that case
            if (this.pathToZmi.get(e.getKey()) == null) {
                PathName fatherPath = new PathName(e.getKey()).levelUp();
                if (this.pathToZmi.get(fatherPath.toString()) != null) {
                    ZMI father = this.pathToZmi.get(fatherPath.toString());
                    ZMI currentChild = e.getValue();

                    // Merge currentChild into ZMI structure
                    currentChild.removeSons();
                    currentChild.setFather(father);
                    father.addSon(currentChild);

                    // Insert it as new sibling for some ZMI
                    this.pathToZmi.put(e.getKey(), currentChild);
                    ZMI leaderSibling = getLeaderSiblingForZmi(currentChild);
                    String leaderSiblingPathStr = this.zmiFullPaths.get(leaderSibling);
                    this.siblingZmis.get(leaderSiblingPathStr).add(currentChild);
                    this.zmiFullPaths.put(currentChild, e.getKey());
                }
            } else {
                // Based on timestamps, decide which ZMI attributes to use
                ZMI currentChild = this.pathToZmi.get(e.getKey());
                ZMI gossippedChild = e.getValue();
                if (gossippedChild.getTimestamp() > currentChild.getTimestamp()) {
                    currentChild.cloneAttributes(gossippedChild);
                    currentChild.setTimestamp(gossippedChild.getTimestamp());
                }
            }
        }

        System.out.println(moduleID + ": Updated ZMI " + pathToZmi);

        updateQueries(msg.queries);
        updateContacts(msg.contacts);

        // Update current ZMI info
        try {
            evaluateAllQueriesWithoutScheduling();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ZMIHolder: Error during query evaluation");
        }

        return null;
    }

    // We are only interested in nodes that have a parent in currently held ZMI
    private void filterGossipedZmi(HashMap<String, ZMI> gossippedZmi) {
        ArrayList<String> toDelete = new ArrayList<>();
        for (Map.Entry<String, ZMI> e : gossippedZmi.entrySet()) {
            if (!e.getKey().equals("/")) {
                String parentPathNameStr = new PathName(e.getKey()).levelUp().toString();
                if (pathToZmi.get(parentPathNameStr) == null)
                    toDelete.add(e.getKey());
            }
        }
        for (String s : toDelete)
            gossippedZmi.remove(s);
    }

    // Root is level 0
    private Integer pathToLevel(String pathStr) {
        int level = 0;
        PathName path = new PathName(pathStr);
        while (!path.toString().equals("/")) {
            level++;
            path = path.levelUp();
        }
        return level;
    }

    private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
            throws UnknownHostException {
        return new ValueContact(new PathName(path), Config.getLocalIpInetAddr());
    }

    public static ZMI createTestHierarchy() throws ParseException, UnknownHostException {
        ValueContact violet07Contact = createContact("/uw/violet07", (byte) 10, (byte) 1, (byte) 1, (byte) 10);
        ValueContact khaki13Contact = createContact("/uw/khaki13", (byte) 10, (byte) 1, (byte) 1, (byte) 38);
        ValueContact khaki31Contact = createContact("/uw/khaki31", (byte) 10, (byte) 1, (byte) 1, (byte) 39);
        ValueContact whatever01Contact = createContact("/uw/whatever01", (byte) 82, (byte) 111, (byte) 52, (byte) 56);
        ValueContact whatever02Contact = createContact("/uw/whatever02", (byte) 82, (byte) 111, (byte) 52, (byte) 57);

        List<Value> list;

        ZMI testRoot = new ZMI();
        testRoot.getAttributes().add("level", new ValueInt(0l));
        testRoot.getAttributes().add("name", new ValueString(null));
        testRoot.getAttributes().add("owner", new ValueString("/uw/violet07"));
        testRoot.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:10:17.342"));
        testRoot.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
        testRoot.getAttributes().add("cardinality", new ValueInt(0l));

        ZMI uw = new ZMI(testRoot);
        testRoot.addSon(uw);
        uw.getAttributes().add("level", new ValueInt(1l));
        uw.getAttributes().add("name", new ValueString("uw"));
        uw.getAttributes().add("owner", new ValueString("/uw/violet07"));
        uw.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
        uw.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
        uw.getAttributes().add("cardinality", new ValueInt(0l));

        ZMI pjwstk = new ZMI(testRoot);
        testRoot.addSon(pjwstk);
        pjwstk.getAttributes().add("level", new ValueInt(1l));
        pjwstk.getAttributes().add("name", new ValueString("pjwstk"));
        pjwstk.getAttributes().add("owner", new ValueString("/pjwstk/whatever01"));
        pjwstk.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
        pjwstk.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
        pjwstk.getAttributes().add("cardinality", new ValueInt(0l));

        ZMI violet07 = new ZMI(uw);
        uw.addSon(violet07);
        violet07.getAttributes().add("level", new ValueInt(2l));
        violet07.getAttributes().add("name", new ValueString("violet07"));
        violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
        violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
        list = Arrays.asList(new Value[]{
                khaki31Contact, whatever01Contact
        });
        violet07.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        violet07.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                violet07Contact,
        });
        violet07.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
        violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
        violet07.getAttributes().add("num_cores", new ValueInt(3l));
        violet07.getAttributes().add("has_ups", new ValueBoolean(null));
        list = Arrays.asList(new Value[]{
                new ValueString("tola"), new ValueString("tosia"),
        });
        violet07.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
        violet07.getAttributes().add("expiry", new ValueDuration(13l, 12l, 0l, 0l, 0l));

        ZMI khaki31 = new ZMI(uw);
        uw.addSon(khaki31);
        khaki31.getAttributes().add("level", new ValueInt(2l));
        khaki31.getAttributes().add("name", new ValueString("khaki31"));
        khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
        khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
        list = Arrays.asList(new Value[]{
                violet07Contact, whatever02Contact,
        });
        khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        khaki31.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                khaki31Contact
        });
        khaki31.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
        khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
        khaki31.getAttributes().add("num_cores", new ValueInt(3l));
        khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
        list = Arrays.asList(new Value[]{
                new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina"),
        });
        khaki31.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
        khaki31.getAttributes().add("expiry", new ValueDuration(-13l, -11l, 0l, 0l, 0l));

        ZMI khaki13 = new ZMI(uw);
        uw.addSon(khaki13);
        khaki13.getAttributes().add("level", new ValueInt(2l));
        khaki13.getAttributes().add("name", new ValueString("khaki13"));
        khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
        khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
        list = Arrays.asList(new Value[]{});
        khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        khaki13.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                khaki13Contact,
        });
        khaki13.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        khaki13.getAttributes().add("creation", new ValueTime((Long) null));
        khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
        khaki13.getAttributes().add("num_cores", new ValueInt(null));
        khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
        list = Arrays.asList(new Value[]{});
        khaki13.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
        khaki13.getAttributes().add("expiry", new ValueDuration((Long) null));

        ZMI whatever01 = new ZMI(pjwstk);
        pjwstk.addSon(whatever01);
        whatever01.getAttributes().add("level", new ValueInt(2l));
        whatever01.getAttributes().add("name", new ValueString("whatever01"));
        whatever01.getAttributes().add("owner", new ValueString("/uw/whatever01"));
        whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
        list = Arrays.asList(new Value[]{
                violet07Contact, whatever02Contact,
        });
        whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        whatever01.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                whatever01Contact,
        });
        whatever01.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
        whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
        whatever01.getAttributes().add("num_cores", new ValueInt(7l));
        list = Arrays.asList(new Value[]{
                new ValueString("rewrite")
        });
        whatever01.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));

        ZMI whatever02 = new ZMI(pjwstk);
        pjwstk.addSon(whatever02);
        whatever02.getAttributes().add("level", new ValueInt(2l));
        whatever02.getAttributes().add("name", new ValueString("whatever02"));
        whatever02.getAttributes().add("owner", new ValueString("/uw/whatever02"));
        whatever02.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:13:00.000"));
        list = Arrays.asList(new Value[]{
                khaki31Contact, whatever01Contact,
        });
        whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        whatever02.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                whatever02Contact,
        });
        whatever02.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
        whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
        whatever02.getAttributes().add("num_cores", new ValueInt(13l));
        list = Arrays.asList(new Value[]{
                new ValueString("odbc")
        });
        whatever02.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));

        return testRoot;
    }
}
