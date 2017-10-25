package pl.edu.mimuw.cloudatlas.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public class EIdentC extends BasicExpr {
    public final String qident_;

    public EIdentC(String p1) {
        qident_ = p1;
    }

    public <R, A> R accept(pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExpr.Visitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EIdentC) {
            pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EIdentC x = (pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EIdentC) o;
            return this.qident_.equals(x.qident_);
        }
        return false;
    }

    public int hashCode() {
        return this.qident_.hashCode();
    }


}
