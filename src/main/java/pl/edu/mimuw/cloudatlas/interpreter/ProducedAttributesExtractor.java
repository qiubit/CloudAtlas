package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.*;
import pl.edu.mimuw.cloudatlas.interpreter.query.PrettyPrinter;
import pl.edu.mimuw.cloudatlas.model.Attribute;

import java.util.ArrayList;
import java.util.List;

public class ProducedAttributesExtractor {

    public List<Attribute> getProducedAttributes(Program program) {
        List<Attribute> res = new ArrayList<>();
        List<QueryResult> queryResults = program.accept(new ProgramInterpreter(), null);
        for (QueryResult queryResult : queryResults) {
            res.add(queryResult.getName());
        }
        return res;
    }

    public class ProgramInterpreter implements Program.Visitor<List<QueryResult>, Void> {
        public List<QueryResult> visit(ProgramC program, Void v) {
            List<QueryResult> results = new ArrayList<QueryResult>();
            for (Statement s : program.liststatement_) {
                try {
                    List<QueryResult> l = s.accept(new StatementInterpreter(), null);
                    for (QueryResult qr : l)
                        if (qr.getName() == null)
                            throw new IllegalArgumentException("All items in top-level SELECT must be aliased.");
                    results.addAll(l);
                } catch (Exception exception) {
                    throw new InsideQueryException(PrettyPrinter.print(s), exception);
                }
            }
            return results;
        }
    }

    public class StatementInterpreter implements Statement.Visitor<List<QueryResult>, Void> {
        public List<QueryResult> visit(StatementC statement, Void v) {
            List<QueryResult> ret = new ArrayList<QueryResult>();
            for (SelItem selItem : statement.listselitem_) {
                try {
                    QueryResult qr = selItem.accept(new SelItemInterpreter(), null);
                    if (qr.getName() != null) {
                        for (QueryResult qrRet : ret)
                            if (qr.getName().getName().equals(qrRet.getName().getName()))
                                throw new IllegalArgumentException("Alias collision.");
                    }
                    ret.add(qr);
                } catch (Exception exception) {
                    throw new InsideQueryException(PrettyPrinter.print(selItem), exception);
                }
            }

            return ret;
        }
    }

    public class SelItemInterpreter implements SelItem.Visitor<QueryResult, Void> {
        public QueryResult visit(SelItemC selItem, Void v) {
            return new QueryResult(null);
        }

        public QueryResult visit(AliasedSelItemC selItem, Void v) {
            return new QueryResult(new Attribute(selItem.qident_), null);
        }
    }

}
