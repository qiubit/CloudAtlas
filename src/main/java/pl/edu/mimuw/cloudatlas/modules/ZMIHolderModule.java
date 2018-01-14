package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.signer.QuerySigner;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ZMIHolderModule extends Module implements MessageHandler {

    public static final String moduleID = "ZMIHolder";

    private ZMI root;
    private ArrayList<ValueContact> fallback_contacts = new ArrayList<>();
    private HashMap<Attribute, QueryInformation> queries = new HashMap<>();
    private PublicKey publicKey;

    public ZMIHolderModule(ZMI root, PublicKey publicKey) throws Exception {
        super(moduleID);
        this.root = root;
        this.publicKey = publicKey;
        System.out.println("ZMIHolder: starting");
        evaluateAllQueries();
    }

    public HashMap<Attribute, QueryInformation> getQueries() {
        return new HashMap<>(queries);
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
            } catch (InterpreterException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void evaluateAllQueries() throws Exception {
        for (QueryInformation query : getQueries().values()) {
            executeQueries(root, query.getQuery());
        }
        Message scheduleQueriesMsg = new ScheduledMessage(new ExecuteQueriesMessage(), 5000);
        scheduleQueriesMsg.setReceiverQueueName(ZMIHolderModule.moduleID);
        sendMsg(TimerModule.moduleID, "", scheduleQueriesMsg, Module.SERIALIZED_TYPE);
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

    public ArrayList<ValueContact> getFallbackContacts() {
        return fallback_contacts;
    }

    private void setFallbackContacts(ArrayList<ValueContact> new_contacts) {
        fallback_contacts = new ArrayList<>(new_contacts);
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
        ZMI zmi = pathNameToZMI(new PathName("/uw/violet07"));
        if (zmi.getSons().size() > 0) return;
        zmi.getAttributes().addOrChange(attr, value);
    }

    private void _getAvailableZones(ZMI zmi, List<PathName> zones) {
        zones.add(getPathName(zmi));
        for (ZMI son : zmi.getSons()) {
            _getAvailableZones(son, zones);
        }
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

    private void uninstallQuery(Attribute name, String query) {
        QueryInformation query_info = queries.get(name);
        if (query_info == null) {
            throw new InvalidParameterException("No query with such name");
        }
        if (!query_info.getQuery().equals(query)) {
            throw new InvalidParameterException("Uninstalled query does't match installed one");
        }
        for (Attribute attr : query_info.getAttributes()) {
            removeAttributeFromAllNonSingletonZMIs(root, attr);
        }
        queries.remove(name);
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

    @Override
    public Message handleMessage(GetAttributesRequestMessage msg) {
        ZMI zmi = pathNameToZMI(msg.zonePath);
        return new GetAttributesResponseMessage(zmi.getAttributes().clone());
    }

    @Override
    public Message handleMessage(GetFallbackContactsRequestMessage msg) {
        return new GetFallbackContactsResponseMessage(fallback_contacts);
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
        return null;
    }

    @Override
    public Message handleMessage(InstallQueryMessage msg) {
        StatusResponseMessage response = new StatusResponseMessage();
        try {
            if (!QuerySigner.verifyInstallSignature(msg.query, msg.signature, publicKey)) {
                System.out.println("Install query verification failed");
                response.setError();
                return response;
            }
            installQuery(msg.query.name, msg.query.query);
        } catch (Exception e) {
            e.printStackTrace();
            response.setError();
        }
        return response;
    }

    @Override
    public Message handleMessage(UninstallQueryMessage msg) {
        StatusResponseMessage response = new StatusResponseMessage();
        try {
            if (!QuerySigner.verifyUninstallSignature(msg.query, msg.signature, publicKey)) {
                System.out.println("Uninstall query verification failed");
                response.setError();
                return response;
            }
            uninstallQuery(msg.query.name, msg.query.query);
        } catch (Exception e) {
            e.printStackTrace();
            response.setError();
        }
        return response;
    }

    @Override
    public Message handleMessage(SetFallbackContactsMessage msg) {
        setFallbackContacts(msg.new_contacts);
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

    private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
            throws UnknownHostException {
        return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[]{
                ip1, ip2, ip3, ip4
        }));
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
