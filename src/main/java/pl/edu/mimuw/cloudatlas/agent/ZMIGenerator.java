package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.util.List;

public class ZMIGenerator {
    private ZMI zmiRoot;
    private ZMI zmiSelf;

    public ZMIGenerator(PathName zonePath) {
        List<String> levels = zonePath.getComponents();

        ZMI root = new ZMI();
        root.getAttributes().add("name", new ValueString(null));

        this.zmiRoot = root;

        ZMI prev = root;
        for (String level : levels) {
            ZMI next = new ZMI(prev);
            next.getAttributes().add("name", new ValueString(level));
            prev.addSon(next);
            prev = next;
        }

        this.zmiSelf = prev;
    }

    public ZMI getRootZmi() {
        return this.zmiRoot;
    }

    public ZMI getSelfZmi() {
        return this.zmiSelf;
    }
}
