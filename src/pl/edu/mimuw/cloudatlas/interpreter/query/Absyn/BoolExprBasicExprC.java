package pl.edu.mimuw.cloudatlas.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public class BoolExprBasicExprC extends BoolExpr {
    public final BasicExpr basicexpr_;

    public BoolExprBasicExprC(BasicExpr p1) {
        basicexpr_ = p1;
    }

    public <R, A> R accept(pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExpr.Visitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprBasicExprC) {
            pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprBasicExprC x = (pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprBasicExprC) o;
            return this.basicexpr_.equals(x.basicexpr_);
        }
        return false;
    }

    public int hashCode() {
        return this.basicexpr_.hashCode();
    }


}
