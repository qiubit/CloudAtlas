package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Query;
import pl.edu.mimuw.cloudatlas.signer.QuerySigner;

import java.security.PrivateKey;
import java.util.*;

public class SignerModule extends Module implements MessageHandler {
    public static final String moduleID = "Signer";

    private Map<Query, List<Attribute>> producedAttributes = new HashMap<>();
    private Set<Attribute> queryNames = new HashSet<>();
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
            return getErrorResponse();
        }
        if (queryNames.contains(msg.query.name)) {
            return getErrorResponse();
        }

        try {
            Message response = new SignResponseMessage(QuerySigner.signInstallQuery(msg.query, privateKey));
            producedAttributes.put(msg.query, getProducedAttributes(msg.query));
            queryNames.add(msg.query.name);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return getErrorResponse();
        }
    }

    @Override
    public Message handleMessage(SignUninstallQueryRequestMessage msg) {
        System.out.println("Handling sign uninstall");
        if (!Attribute.isQuery(msg.query.name)) {
            return getErrorResponse();
        }
        if (!producedAttributes.containsKey(msg.query)) {
            return getErrorResponse();
        }

        try {
            Message response = new SignResponseMessage(QuerySigner.signUninstallQuery(msg.query, privateKey));
            producedAttributes.remove(msg.query);
            queryNames.remove(msg.query.name);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return getErrorResponse();
        }
    }

    private Message getErrorResponse() {
        Message response = new SignResponseMessage(null);
        response.setError();
        return response;
    }

    private List<Attribute> getProducedAttributes(Query query) {
        return new ArrayList<>();
    }

}
