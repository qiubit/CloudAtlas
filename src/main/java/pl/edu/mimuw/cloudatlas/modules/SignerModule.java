package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.interpreter.ProducedAttributesExtractor;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Query;
import pl.edu.mimuw.cloudatlas.signer.QuerySigner;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.util.*;

public class SignerModule extends Module implements MessageHandler {
    public static final String moduleID = "Signer";

    private Map<Query, List<Attribute>> queryToAttributes = new HashMap<>();
    private Set<Attribute> queryNames = new HashSet<>();
    private Set<Attribute> producedAttributes = new HashSet<>();
    private PrivateKey privateKey;

    public SignerModule(PrivateKey privateKey) throws Exception {
        super(moduleID);
        this.privateKey = privateKey;
        System.out.println("Starting signer module");
    }

    @Override
    public Message handleMessage(SignInstallQueryRequestMessage msg) {
        System.out.println("Handling sign install");
        if (!Attribute.isQuery(msg.query.name)) {
            System.out.println("Wrong query name " + msg.query.name);
            return getErrorResponse(msg);
        }
        if (queryNames.contains(msg.query.name)) {
            System.out.println("Signer already signed " + msg.query.name);
            return getErrorResponse(msg);
        }

        try {
            List<Attribute> attributes = getProducedAttributes(msg.query);
            for (Attribute attribute : attributes) {
                if (producedAttributes.contains(attribute)) {
                    return getErrorResponse(msg);
                }
            }
            Message response = new SignResponseMessage(QuerySigner.signInstallQuery(msg.query, privateKey));
            queryToAttributes.put(msg.query, attributes);
            queryNames.add(msg.query.name);
            producedAttributes.addAll(attributes);
            response.setReceiverHostname(msg.getSenderHostname());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return getErrorResponse(msg);
        }
    }

    @Override
    public Message handleMessage(SignUninstallQueryRequestMessage msg) {
        System.out.println("Handling sign uninstall");
        if (!Attribute.isQuery(msg.query.name)) {
            return getErrorResponse(msg);
        }
        if (!queryToAttributes.containsKey(msg.query)) {
            return getErrorResponse(msg);
        }

        try {
            Message response = new SignResponseMessage(QuerySigner.signUninstallQuery(msg.query, privateKey));
            List<Attribute> attributes = queryToAttributes.get(msg.query);
            queryToAttributes.remove(msg.query);
            queryNames.remove(msg.query.name);
            producedAttributes.removeAll(attributes);
            response.setReceiverHostname(msg.getSenderHostname());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return getErrorResponse(msg);
        }
    }

    private Message getErrorResponse(Message msg) {
        Message response = new SignResponseMessage(null);
        response.setError();
        response.setReceiverHostname(msg.getSenderHostname());
        return response;
    }

    private List<Attribute> getProducedAttributes(Query query) throws Exception {
        ProducedAttributesExtractor attributesExtractor = new ProducedAttributesExtractor();
        Yylex lex = new Yylex(new ByteArrayInputStream(query.query.getBytes()));
        return attributesExtractor.getProducedAttributes((new parser(lex)).pProgram());
    }

}
