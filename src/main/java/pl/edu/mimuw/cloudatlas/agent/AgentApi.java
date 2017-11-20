package pl.edu.mimuw.cloudatlas.agent;

import org.w3c.dom.Attr;
import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public interface AgentApi extends Remote {
    List<PathName> getAvailableZones() throws RemoteException;
    void installQuery(Attribute name, String query) throws RemoteException, InvalidParameterException;
    void uninstallQuery(Attribute name) throws RemoteException;
    HashMap<Attribute, QueryInformation> getQueries() throws RemoteException;
    AttributesMap getAttributes(PathName zone) throws RemoteException;
    List<ValueContact> getFallbackContacts() throws RemoteException;
    void setFallbackContacts(ArrayList<ValueContact> new_contacts) throws RemoteException;
    void setAttribute(PathName zone, Attribute attr, Value value) throws RemoteException;
}