package pl.edu.mimuw.cloudatlas;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.*;

import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.model.*;

public class InterpreterCMD {
    private static ZMI root;

    public static void main(String[] args) throws Exception {
        root = createTestHierarchy();
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\n");
        while (scanner.hasNext()) {
            String line = scanner.next();
            int i = 0;
            while (i < line.length() && line.charAt(i) != ':') {
                i += 1;
            }
            if (line.charAt(0) != '&' || line.charAt(i) != ':') {
                System.err.println("Error: Invalid query");
                return;
            } else {
                line = line.substring(i+1);
            }
            executeQueries(root, line);
        }
        scanner.close();
        printZone(root);
    }

    private static void printZone(ZMI zmi) {
        System.out.println(getPathName(zmi));
        AttributesMap attributes = zmi.getAttributes();
        Iterator attributesIterator = attributes.iterator();
        while (attributesIterator.hasNext()) {
            Map.Entry e = (Map.Entry) attributesIterator.next();
            System.out.print("\t");
            System.out.print(e.getKey());
            System.out.print(": ");
            System.out.print(e.getValue());
            System.out.print("\n");
        }
        if (!zmi.getSons().isEmpty()) {
            for (ZMI son : zmi.getSons()) {
                printZone(son);
            }
        }
    }

    private static PathName getPathName(ZMI zmi) {
        String name = ((ValueString) zmi.getAttributes().get("name")).getValue();
        return zmi.getFather() == null ? PathName.ROOT : getPathName(zmi.getFather()).levelDown(name);
    }

    private static void executeQueries(ZMI zmi, String query) throws Exception {
        if (!zmi.getSons().isEmpty()) {
            for (ZMI son : zmi.getSons())
                executeQueries(son, query);
            Interpreter interpreter = new Interpreter(zmi);
            Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
            try {
                List<QueryResult> result = interpreter.interpretProgram((new parser(lex)).pProgram());
                PathName zone = getPathName(zmi);
                for (QueryResult r : result) {
                    // System.out.println(zone + ": " + r);
                    zmi.getAttributes().addOrChange(r.getName(), r.getValue());
                }
            } catch (InterpreterException exception) {
                // Enable this to show debug output
                // exception.printStackTrace();
            }
        }
    }

    private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
            throws UnknownHostException {
        return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[]{
                ip1, ip2, ip3, ip4
        }));
    }

    private static ZMI createTestHierarchy() throws ParseException, UnknownHostException {
        List<Value> list;

        root = new ZMI();
        root.getAttributes().add("level", new ValueInt(0l));
        root.getAttributes().add("name", new ValueString(null));

        ZMI uw = new ZMI(root);
        root.addSon(uw);
        uw.getAttributes().add("level", new ValueInt(1l));
        uw.getAttributes().add("name", new ValueString("uw"));

        ZMI pjwstk = new ZMI(root);
        root.addSon(pjwstk);
        pjwstk.getAttributes().add("level", new ValueInt(1l));
        pjwstk.getAttributes().add("name", new ValueString("pjwstk"));

        ZMI violet07 = new ZMI(uw);
        uw.addSon(violet07);
        violet07.getAttributes().add("level", new ValueInt(2l));
        violet07.getAttributes().add("name", new ValueString("violet07"));
        violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
        violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
        list = Arrays.asList(new Value[]{
                new ValueString("UW1A"), new ValueString("UW1B"), new ValueString("UW1C")
        });
        violet07.getAttributes().add("contacts", new ValueSet(new HashSet<>(list), TypePrimitive.STRING));
        violet07.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                new ValueString("UW1")
        });
        violet07.getAttributes().add("members", new ValueSet(new HashSet<>(list), TypePrimitive.STRING));
        violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
        violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
        violet07.getAttributes().add("num_cores", new ValueInt(3l));
        violet07.getAttributes().add("num_processes", new ValueInt(131l));
        violet07.getAttributes().add("has_ups", new ValueBoolean(null));
        list = Arrays.asList(new Value[]{
                new ValueString("tola"), new ValueString("tosia"),
        });
        violet07.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
        violet07.getAttributes().add("expiry", new ValueDuration("+13 12:00:00.000"));

        ZMI khaki31 = new ZMI(uw);
        uw.addSon(khaki31);
        khaki31.getAttributes().add("level", new ValueInt(2l));
        khaki31.getAttributes().add("name", new ValueString("khaki31"));
        khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
        khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
        list = Arrays.asList(new Value[]{
                new ValueString("UW2A")
        });
        khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<>(list), TypePrimitive.STRING));
        khaki31.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                new ValueString("UW2A")
        });
        khaki31.getAttributes().add("members", new ValueSet(new HashSet<>(list), TypePrimitive.STRING));
        khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
        khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
        khaki31.getAttributes().add("num_cores", new ValueInt(3l));
        khaki31.getAttributes().add("num_processes", new ValueInt(124l));
        khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
        list = Arrays.asList(new Value[]{
                new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina"),
        });
        khaki31.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
        khaki31.getAttributes().add("expiry", new ValueDuration("-13 11:00:00.000"));

        ZMI khaki13 = new ZMI(uw);
        uw.addSon(khaki13);
        khaki13.getAttributes().add("level", new ValueInt(2l));
        khaki13.getAttributes().add("name", new ValueString("khaki13"));
        khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
        khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
        list = Arrays.asList(new Value[]{
                new ValueString("UW3A"), new ValueString("UW3B")
        });
        khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<>(list), TypePrimitive.STRING));
        khaki13.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
               new ValueString("UW3B")
        });
        khaki13.getAttributes().add("members", new ValueSet(new HashSet<>(list), TypePrimitive.STRING));
        khaki13.getAttributes().add("creation", new ValueTime((Long) null));
        khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
        khaki13.getAttributes().add("num_cores", new ValueInt(null));
        khaki13.getAttributes().add("num_processes", new ValueInt(107l));
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
                new ValueString("PJ1")
        });
        whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<>(list), TypePrimitive.STRING));
        whatever01.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                new ValueString("PJ1")
        });
        whatever01.getAttributes().add("members", new ValueSet(new HashSet<>(list), TypePrimitive.STRING));
        whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
        whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
        whatever01.getAttributes().add("num_cores", new ValueInt(7l));
        whatever01.getAttributes().add("num_processes", new ValueInt(215l));
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
                new ValueString("PJ2")
        });
        whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.STRING));
        whatever02.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[]{
                new ValueString("PJ2")
        });
        whatever02.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.STRING));
        whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
        whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
        whatever02.getAttributes().add("num_cores", new ValueInt(13l));
        whatever02.getAttributes().add("num_processes", new ValueInt(222l));
        list = Arrays.asList(new Value[]{
                new ValueString("odbc")
        });
        whatever02.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));

        return root;
    }
}
