package pl.edu.mimuw.cloudatlas.rest;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.rabbitmq.client.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.edu.mimuw.cloudatlas.agent.*;
import org.springframework.web.bind.annotation.*;
import pl.edu.mimuw.cloudatlas.agent.message.*;
import pl.edu.mimuw.cloudatlas.agent.module.ZMIHolderModule;
import pl.edu.mimuw.cloudatlas.model.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


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

class RmiClient {
    private String host;
    private String serviceName;
    private AgentApi agentApi;

    public RmiClient(String host, String serviceName) {
        this.host = host;
        this.serviceName = serviceName;
    }

    public AgentApi getAgentApi() {
        if (agentApi == null) {
            try {
                if (System.getSecurityManager() == null) {
                    SecurityManager m = new SecurityManager();
                    System.setSecurityManager(m);
                }
                Registry registry = LocateRegistry.getRegistry(this.host);
                this.agentApi = (AgentApi) registry.lookup(this.serviceName);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("RMI agentapi problem");
            }
        }
        return agentApi;
    }
}

class RabbitClient {
    public static final String queueName = "ApiQueue";
    private String host;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;

    private final Object request_lock = new Object(); // sorry

    RabbitClient(String host) {
        this.host = host;
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        channel = getChannel();
    }

    private Channel getChannel() {
        Channel channel = null;
        if (this.connection == null || !this.connection.isOpen()) {
            try {
                connection = connectionFactory.newConnection();
                channel = connection.createChannel();
                channel.queueDeclare(queueName, false, false, false, null);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("RabbitMQ problem");
            }
        }
        return channel;
    }

    private Message getResponse(Message request) throws Exception {
        synchronized (request_lock) {
            String corrId = UUID.randomUUID().toString();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(queueName)
                    .build();

            final BlockingQueue<Message> response_queue = new ArrayBlockingQueue<>(1);
            String tag = channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        Message response = null;
                        try {
                            response = Message.fromBytes(body);
                            System.out.println("got response");
                        } catch (ClassNotFoundException e) {
                            System.out.println("Class not found exception");
                        }
                        response_queue.offer(response);
                    }
                }
            });
            channel.basicPublish("", ZMIHolderModule.moduleID, props, request.toBytes());
            Message result = response_queue.poll(2, TimeUnit.SECONDS);
            channel.basicCancel(tag);
            return result;
        }
    }

    public AttributesMap getAttributes(String zoneName) throws Exception {
        Message msg = new GetAttributesRequestMessage(new PathName(zoneName));
        GetAttributesResponseMessage response = (GetAttributesResponseMessage) getResponse(msg);
        return response.attributesMap;
    }

    public List<PathName> getAvailableZones() throws Exception {
        Message msg = new GetAvailableZonesRequestMessage();
        GetAvailableZonesResponseMessage response = (GetAvailableZonesResponseMessage) getResponse(msg);
        return response.availableZones;
    }

    public List<ValueContact> getFallbackContacts() throws Exception {
        Message msg = new GetFallbackContactsRequestMessage();
        GetFallbackContactsResponseMessage response = (GetFallbackContactsResponseMessage) getResponse(msg);
        return response.fallbackContacts;
    }

    public boolean setFallbackContacts(ArrayList<ValueContact> fallbackConstacts) throws Exception {
        Message msg = new SetFallbackRequestMessage(fallbackConstacts);
        StatusResponseMessage response = (StatusResponseMessage) getResponse(msg);
        return response.status;
    }

}


@RestController
public class ZonesController {

    RmiClient rmiClient = new RmiClient("localhost", "AgentApi");
    RabbitClient rabbitClient = new RabbitClient("localhost");


    @CrossOrigin(origins = "*")
    @RequestMapping(value="/install_query",  method= RequestMethod.POST)
    public ResponseEntity<String> installQuery(@RequestBody @Valid QueryRequest queryRequest) {
        try {
            rmiClient.getAgentApi().installQuery(new Attribute(queryRequest.getName()), queryRequest.getQuery());
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
            rmiClient.getAgentApi().uninstallQuery(new Attribute(queryRequest.getName()));
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
        try {
            List<QueryRequest> result_list = new ArrayList<>();
            for (Map.Entry<Attribute, QueryInformation> entry : rmiClient.getAgentApi().getQueries().entrySet()) {
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
            boolean result = rabbitClient.setFallbackContacts(new ArrayList<>(
                                fallbackRequest.getContacts().stream().map(
                                    elem -> new ValueContact(new PathName(elem.getName()), elem.getAddress())
                                ).collect(Collectors.toList())));
            if (!result) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
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
        try {
            List<ValueContact> contacts = rabbitClient.getFallbackContacts();
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
        try {
            AttributesMap attributes = rabbitClient.getAttributes(zoneRequest.getZoneName());
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
        try {
            result.setZones(rabbitClient.getAvailableZones().stream().map(elem -> elem.toString()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(result);
    }
}
