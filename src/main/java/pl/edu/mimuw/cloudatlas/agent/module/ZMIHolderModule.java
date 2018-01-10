package pl.edu.mimuw.cloudatlas.agent.module;

import pl.edu.mimuw.cloudatlas.agent.message.*;
import pl.edu.mimuw.cloudatlas.agent.module.Module;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class ZMIHolderModule extends Module implements MessageHandler {

    public static final String moduleID = "ZMIHolder";

    private static ZMI root;

    public ZMIHolderModule(ZMI root) throws Exception {
        super(moduleID);
        this.root = root;
    }

    @Override
    public Message handleMessage(GetAttributesRequestMessage msg) {
        ZMI zmi = pathNameToZMI(msg.zonePath);
        return new GetAttributesResponseMessage(zmi.getAttributes().clone());
    }

    @Override
    public Message handleMessage(GetAvailableZonesRequestMessage msg) {
        ArrayList<PathName> zones = new ArrayList<>();
        _getAvailableZones(root, zones);
        return new GetAvailableZonesResponseMessage(zones);
    }

    private void _getAvailableZones(ZMI zmi, List<PathName> zones) {
        zones.add(getPathName(zmi));
        for (ZMI son : zmi.getSons()) {
            _getAvailableZones(son, zones);
        }
    }

    private static ZMI pathNameToZMI(PathName path) {
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

    private static PathName getPathName(ZMI zmi) {
        String name = ((ValueString) zmi.getAttributes().get("name")).getValue();
        return zmi.getFather() == null ? PathName.ROOT : getPathName(zmi.getFather()).levelDown(name);
    }

}
