package pl.edu.mimuw.cloudatlas.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public abstract class SelItem implements java.io.Serializable {
    public abstract <R, A> R accept(SelItem.Visitor<R, A> v, A arg);

    public interface Visitor<R, A> {
        public R visit(pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.SelItemC p, A arg);

        public R visit(pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.AliasedSelItemC p, A arg);

    }

}
