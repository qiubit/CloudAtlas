package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.security.InvalidParameterException;
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

}
