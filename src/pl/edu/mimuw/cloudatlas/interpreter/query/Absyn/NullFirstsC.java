package pl.edu.mimuw.cloudatlas.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public class NullFirstsC extends Nulls {

    public NullFirstsC() {
    }

    public <R, A> R accept(pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Nulls.Visitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.NullFirstsC) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return 37;
    }


}
