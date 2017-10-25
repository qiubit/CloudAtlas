package pl.edu.mimuw.cloudatlas.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public class BoolExprRegExpC extends BoolExpr {
    public final BasicExpr basicexpr_;
    public final String string_;

    public BoolExprRegExpC(BasicExpr p1, String p2) {
        basicexpr_ = p1;
        string_ = p2;
    }

    public <R, A> R accept(pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExpr.Visitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprRegExpC) {
            pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprRegExpC x = (pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprRegExpC) o;
            return this.basicexpr_.equals(x.basicexpr_) && this.string_.equals(x.string_);
        }
        return false;
    }

    public int hashCode() {
        return 37 * (this.basicexpr_.hashCode()) + this.string_.hashCode();
    }


}
