package pl.edu.mimuw.cloudatlas.rest;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.rabbitmq.client.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.modules.Module;
import pl.edu.mimuw.cloudatlas.modules.SignerModule;
import pl.edu.mimuw.cloudatlas.modules.ZMIHolderModule;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static pl.edu.mimuw.cloudatlas.modules.Module.JSON_TYPE;
import static pl.edu.mimuw.cloudatlas.modules.Module.SERIALIZED_TYPE;


class AttributeResponse {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

class AttributeListResponse {
    private List<AttributeResponse> attributes;

    public List<AttributeResponse> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeResponse> attributes) {
        this.attributes = attributes;
    }
}

class QueryRequest {

    @NotNull
    private String query;

    @NotNull
    private String name;

    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class QueriesList {
    private List<QueryRequest> queries;

    public List<QueryRequest> getQueries() {
        return queries;
    }

    public void setQueries(List<QueryRequest> queries) {
        this.queries = queries;
    }
}


class ZoneRequest {

    @NotNull
    private String zoneName;

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }
}

class Contact {
    private String name;
    private InetAddress address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }
}

class FallbackContactsRequest {
    @NotNull
    private List<Contact> contacts;

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}

class ZoneList {
    List<String> zones;

    public List<String> getZones() {
        return zones;
    }

    public void setZones(List<String> zones) {
        this.zones = zones;
    }
}


@RestController
public class ZonesController {
    private final static String QUEUE_NAME = "SpringRest";

    private Connection agentConnection;
    private Connection signerConnection;
    private Consumer consumer;

    private Map<Long, BlockingQueue<Message>> responseQueue = new ConcurrentHashMap<>();
    private Object mapLock = new Object();

    private long allocFreeId(BlockingQueue<Message> queue) {
        long id;
        while (true) {
            synchronized (mapLock) {
                id = System.currentTimeMillis();
                if (responseQueue.get(id) == null) {
                    responseQueue.put(id, queue);
                    break;
                }
            }
        }
        return id;
    }

    private void sendMessage(Connection connection, String moduleID, Message msg, Long id) throws IOException {
        Channel channel = connection.createChannel();
        channel.queueDeclare(moduleID, false, false, false, null);
        msg.setSenderHostname(Application.localIP);
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .replyTo(QUEUE_NAME)
                .contentType(Module.SERIALIZED_TYPE)
                .correlationId(id.toString())
                .build();
        channel.basicPublish("", moduleID, props, msg.toBytes());
    }

    public ZonesController() {

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername("cloudatlas");
            factory.setPassword("cloudatlas");
            factory.setHost("localhost");
            agentConnection = factory.newConnection();
            Channel channel = agentConnection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            factory.setHost(Application.signerIP);
            signerConnection = factory.newConnection();

            Application.log.info("Running RabbitMQ consumer");
            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    try {
                        Application.log.info("RabbitMQ consumer: delivery");

                        Message message;
                        String contentType = properties.getContentType();

                        if (contentType == null) {
                            throw new IOException("Message without content_type property received");
                        }

                        if (contentType.equals(JSON_TYPE)) {
                            message = JsonMessage.fromBytes(body);
                        } else if (contentType.equals(SERIALIZED_TYPE)) {
                            message = SerializedMessage.fromBytes(body);
                        } else {
                            throw new IOException("Unexpected msg contentType: " + contentType);
                        }

                        long id = Long.parseLong(properties.getCorrelationId());
                        Application.log.info("Got RabbitMQ message with correlationId " + properties.getCorrelationId());
                        if (responseQueue.get(id) != null) {
                            responseQueue.get(id).put(message);
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Got Invalid msg");
                    } catch (InterruptedException e) {
                        System.out.println("BlockingQueue failure");
                    }
                }
            };
            channel.basicConsume(QUEUE_NAME, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value="/install_query",  method= RequestMethod.POST)
    public ResponseEntity<String> installQuery(@RequestBody @Valid QueryRequest queryRequest) {
        try {
            Application.log.info("Installing query " + queryRequest.getName() + ": " + queryRequest.getQuery());
            long msgId = allocFreeId(new ArrayBlockingQueue<Message>(1));
            Application.log.info("Sending SignInstallQuery to RabbitMQ, msgId " + msgId);
            sendMessage(signerConnection, SignerModule.moduleID, new SignInstallQueryRequestMessage(new Query(new Attribute(queryRequest.getName()), queryRequest.getQuery())), msgId);
            SignResponseMessage signResponse = (SignResponseMessage) this.responseQueue.get(msgId).poll(5000, TimeUnit.MILLISECONDS);
            responseQueue.remove(msgId);
            if (signResponse.isError()) {
                Application.log.info("Sign query failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            msgId = allocFreeId(new ArrayBlockingQueue<Message>(1));
            sendMessage(agentConnection, ZMIHolderModule.moduleID, new InstallQueryMessage(new Query(new Attribute(queryRequest.getName()), queryRequest.getQuery()), signResponse.signature), msgId);
            StatusResponseMessage installResponse = (StatusResponseMessage) this.responseQueue.get(msgId).poll(5000, TimeUnit.MILLISECONDS);
            if (installResponse == null || installResponse.isError()) {
                // rollback, we need to uninstall query from signer
                Application.log.info("Rollback of install");
                sendMessage(signerConnection, SignerModule.moduleID, new SignUninstallQueryRequestMessage(new Query(new Attribute(queryRequest.getName()), queryRequest.getQuery())), -1L);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok("ok");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value="/uninstall_query",  method= RequestMethod.POST)
    public ResponseEntity<String> uninstallQuery(@RequestBody @Valid QueryRequest queryRequest) {
        try {
            Application.log.info("Uninstalling query " + queryRequest.getName() + ": " + queryRequest.getQuery());
            long msgId = allocFreeId(new ArrayBlockingQueue<Message>(1));
            Application.log.info("Sending SignInstallQuery to RabbitMQ, msgId " + msgId);
            sendMessage(signerConnection, SignerModule.moduleID, new SignUninstallQueryRequestMessage(new Query(new Attribute(queryRequest.getName()), queryRequest.getQuery())), msgId);
            SignResponseMessage signResponse = (SignResponseMessage) this.responseQueue.get(msgId).poll(5000, TimeUnit.MILLISECONDS);
            responseQueue.remove(msgId);
            if (signResponse == null || signResponse.isError()) {
                Application.log.info("Sign query failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            msgId = allocFreeId(new ArrayBlockingQueue<Message>(1));
            sendMessage(agentConnection, ZMIHolderModule.moduleID, new UninstallQueryMessage(new Query(new Attribute(queryRequest.getName()), queryRequest.getQuery()), signResponse.signature), msgId);
            StatusResponseMessage uninstallResponse = (StatusResponseMessage) this.responseQueue.get(msgId).poll(5000, TimeUnit.MILLISECONDS);
            if (uninstallResponse == null || uninstallResponse.isError()) {
                // rollback, we need to install query in signer
                Application.log.info("Rollback of uninstall");
                sendMessage(signerConnection, SignerModule.moduleID, new SignInstallQueryRequestMessage(new Query(new Attribute(queryRequest.getName()), queryRequest.getQuery())), -1L);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok("ok");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value="/get_queries")
    public ResponseEntity<QueriesList> getQueries() {
        QueriesList result = new QueriesList();
        long msgId = allocFreeId(new ArrayBlockingQueue<Message>(1));
        try {
            Application.log.info("Sending /get_queries to RabbitMQ, msgId " + msgId);
            sendMessage(agentConnection, ZMIHolderModule.moduleID, new GetQueriesRequestMessage(), msgId);
            GetQueriesResponseMessage msg =
                    (GetQueriesResponseMessage) this.responseQueue.get(msgId).poll(5000, TimeUnit.MILLISECONDS);
            if (msg == null) {
                Application.log.warn("Response message is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            List<QueryRequest> result_list = new ArrayList<>();
            for (Map.Entry<Attribute, QueryInformation> entry : msg.queries.entrySet()) {
                QueryRequest query_request = new QueryRequest();
                query_request.setName(entry.getKey().toString());
                query_request.setQuery(entry.getValue().getQuery());
                result_list.add(query_request);
            }
            result.setQueries(result_list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(result);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value="/set_fallback",  method= RequestMethod.POST)
    public ResponseEntity<String> setFallback(@RequestBody @Valid FallbackContactsRequest fallbackRequest) {
        try {
            sendMessage(agentConnection, ZMIHolderModule.moduleID, new SetContactsMessage(new ArrayList<>(
                    fallbackRequest.getContacts().stream().map(
                            elem -> new ValueContact(new PathName(elem.getName()), elem.getAddress())
                    ).collect(Collectors.toList()))), -1L);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok("ok");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value="/get_fallback")
    public ResponseEntity<FallbackContactsRequest> getFallback() {
        FallbackContactsRequest result = new FallbackContactsRequest();
        long msgId = allocFreeId(new ArrayBlockingQueue<Message>(1));
        try {
            Application.log.info("Sending /get_fallback message to RabbitMQ");
            sendMessage(agentConnection, ZMIHolderModule.moduleID, new GetContactsRequestMessage(), msgId);
            GetContactsResponseMessage msg =
                    (GetContactsResponseMessage) this.responseQueue.get(msgId).poll(5000, TimeUnit.MILLISECONDS);
            this.responseQueue.remove(msgId);
            Application.log.info("Got /get_fallback RabbitMQ response");
            if (msg == null) {
                Application.log.warn("/get_fallback response message is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            List<ValueContact> contacts = msg.contactList;
            List<Contact> result_contacts = new ArrayList<>();
            for (ValueContact contact : contacts) {
                Contact result_contact = new Contact();
                result_contact.setAddress(contact.getAddress());
                result_contact.setName(contact.getName().toString());
                result_contacts.add(result_contact);
            }
            result.setContacts(result_contacts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(result);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value="/get_attributes",  method= RequestMethod.POST)
    public ResponseEntity<AttributeListResponse> getAttributes(@RequestBody @Valid ZoneRequest zoneRequest) {
        AttributeListResponse result = new AttributeListResponse();
        long msgId = allocFreeId(new ArrayBlockingQueue<Message>(1));
        try {
            Application.log.info("Sending /get_attributes message to RabbitMQ");
            sendMessage(agentConnection, ZMIHolderModule.moduleID, new GetAttributesRequestMessage(new PathName(zoneRequest.getZoneName())), msgId);
            GetAttributesResponseMessage msg =
                    (GetAttributesResponseMessage) this.responseQueue.get(msgId).poll(5000, TimeUnit.MILLISECONDS);
            this.responseQueue.remove(msgId);
            Application.log.info("Got RabbitMQ response");
            if (msg == null) {
                Application.log.warn("Response message is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            AttributesMap attributes = msg.attributesMap;
            List<AttributeResponse> result_list = new ArrayList<>();
            for (Map.Entry<Attribute, Value> entry : attributes) {
                AttributeResponse attribute_response = new AttributeResponse();
                attribute_response.setName(entry.getKey().toString());
                attribute_response.setValue(entry.getValue().toString());
                result_list.add(attribute_response);
            }
            result.setAttributes(result_list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(result);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping("/zones")
    public ResponseEntity<ZoneList> zones() {
        ZoneList result = new ZoneList();

        long msgId = allocFreeId(new ArrayBlockingQueue<Message>(1));
        try {
            Application.log.info("Sending /zones message to RabbitMQ");
            sendMessage(agentConnection, ZMIHolderModule.moduleID, new GetZonesRequestMessage(), msgId);
            GetZonesResponseMessage msg =
                    (GetZonesResponseMessage) this.responseQueue.get(msgId).poll(5000, TimeUnit.MILLISECONDS);
            this.responseQueue.remove(msgId);
            Application.log.info("Got RabbitMQ response");
            if (msg == null) {
                Application.log.warn("Response message is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            result.setZones(msg.zones.stream().map(elem -> elem.toString()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(result);
    }
}
