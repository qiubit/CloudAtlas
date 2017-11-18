package pl.edu.mimuw.cloudatlas.rest;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.edu.mimuw.cloudatlas.agent.AgentApi;
import org.springframework.web.bind.annotation.*;
import pl.edu.mimuw.cloudatlas.model.*;


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
                System.err.println("RMI agentapi problem");
            }
        }
        return agentApi;
    }
}

@RestController
public class ZonesController {

    RmiClient rmiClient = new RmiClient("localhost", "AgentApi");

    @RequestMapping(value="/install_query",  method= RequestMethod.POST)
    public ResponseEntity<String> installQuery(@RequestBody QueryRequest queryRequest) {
        try {
            rmiClient.getAgentApi().installQuery(new Attribute(queryRequest.getName()), queryRequest.getQuery());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok("ok");
    }


    @RequestMapping(value="/uninstall_query",  method= RequestMethod.POST)
    public ResponseEntity<String> uninstallQuery(@RequestBody QueryRequest queryRequest) {
        try {
            rmiClient.getAgentApi().uninstallQuery(new Attribute(queryRequest.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok("ok");
    }

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


    @RequestMapping(value="/set_fallback",  method= RequestMethod.POST)
    public ResponseEntity<String> setFallback(@RequestBody FallbackContactsRequest fallbackRequest) {
        try {
            rmiClient.getAgentApi().setFallbackContacts(new ArrayList<>(
                    fallbackRequest.getContacts().stream().map(
                            elem -> new ValueContact(new PathName(elem.getName()), elem.getAddress())
                    ).collect(Collectors.toList())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok("ok");
    }

    @RequestMapping(value="/get_fallback")
    public ResponseEntity<FallbackContactsRequest> getFallback() {
        FallbackContactsRequest result = new FallbackContactsRequest();
        try {
            List<ValueContact> contacts = rmiClient.getAgentApi().getFallbackContacts();
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

    @RequestMapping(value="/get_attributes",  method= RequestMethod.POST)
    public ResponseEntity<AttributeListResponse> getAttributes(@RequestBody ZoneRequest zoneRequest) {
        AttributeListResponse result = new AttributeListResponse();
        try {

            AttributesMap attributes = rmiClient.getAgentApi().getAttributes(new PathName(zoneRequest.getZoneName()));
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

    @RequestMapping("/zones")
    public ResponseEntity<ZoneList> zones() {
        ZoneList result = new ZoneList();
        try {
            result.setZones(rmiClient.getAgentApi().getAvailableZones().stream().map(elem -> elem.toString()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(result);
    }
}
