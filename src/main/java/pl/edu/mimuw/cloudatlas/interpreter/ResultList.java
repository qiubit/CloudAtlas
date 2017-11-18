package pl.edu.mimuw.cloudatlas.interpreter;


import java.util.concurrent.ThreadLocalRandom;
import pl.edu.mimuw.cloudatlas.model.*;


class ResultList extends Result {
    private final Value value;
    private final ResultType resultType = ResultType.LIST;

    public ResultList(Value value) {
        if (value.getType().isCollection() || value.isNull())
            this.value = value;
        else
            throw new IllegalArgumentException("ResultList must be a collection");
    }

    @Override
    protected ResultList binaryOperationTyped(BinaryOperation operation, ResultSingle right) {
        Type resultElementType = null;
        if (operation == Result.DIVIDE) {
            resultElementType = TypePrimitive.DOUBLE;
        } else if (operation == Result.REG_EXPR || operation == Result.IS_EQUAL || operation == Result.IS_LOWER_THAN) {
            resultElementType = TypePrimitive.BOOLEAN;
        } else {
            resultElementType = right.getType();
        }
        ValueList inputList = (ValueList) value;

        ValueList resultList = new ValueList(resultElementType);
        for (Value v : inputList) {
            resultList.add(operation.perform(v, right.getValue()));
        }
        return new ResultList(resultList);
    }

    @Override
    protected ResultList binaryOperationTyped(BinaryOperation operation, ResultColumn right) {
        throw new IllegalArgumentException("Binary operations of ResultList and ResultColumn not supported.");
    }

    @Override
    protected ResultList binaryOperationTyped(BinaryOperation operation, ResultList right) {
        Type resultElementType = null;
        if (operation == Result.IS_LOWER_THAN || operation == Result.REG_EXPR || operation == Result.IS_EQUAL) {
            resultElementType = TypePrimitive.BOOLEAN;
        } else if (operation == Result.DIVIDE) {
            resultElementType = TypePrimitive.DOUBLE;
        } else {
            resultElementType = ((TypeCollection) value.getType()).getElementType();
        }
        if (((ValueList) value).size() != ((ValueList) right.getValue()).size()) {
            throw new IllegalArgumentException("Both ResultLists must have the same size");
        }
        ValueList inputList = (ValueList) value;
        ValueList resultList = new ValueList(resultElementType);
        for (int i = 0; i < ((ValueInt) value.valueSize()).getValue(); i++) {
            resultList.add(operation.perform(inputList.get(i), ((ValueList) right.getValue()).get(i)));
        }
        return new ResultList(resultList);
    }

    @Override
    public ResultList unaryOperation(UnaryOperation operation) {
        Type resultElementType = null;
        if (operation == Result.NEGATE) {
            resultElementType = ((TypeCollection) value.getType()).getElementType();
        } else if (operation == Result.VALUE_SIZE) {
            resultElementType = TypePrimitive.INTEGER;
        }
        ValueList column = (ValueList) value;
        ValueList result = new ValueList(resultElementType);
        for (Value v : column) {
            result.add(operation.perform(v));
        }
        return new ResultList(result);
    }

    @Override
    protected Result callMe(BinaryOperation operation, Result left) {
        return left.binaryOperationTyped(operation, this);
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public ValueList getList() {
        TypeCollection valueType = (TypeCollection) value.getType();
        return (ValueList) value.convertTo(new TypeCollection(Type.PrimaryType.LIST, valueType.getElementType()));
    }

    @Override
    public ValueList getColumn() {
        throw new UnsupportedOperationException("Not a ResultColumn.");
    }

    @Override
    public Result first(int size) {
        if (size >= 0) {
            TypeCollection valueType = (TypeCollection) value.getType();
            ValueList inputList =
                    (ValueList) value.convertTo(new TypeCollection(Type.PrimaryType.LIST, valueType.getElementType()));
            ValueList resultList = new ValueList(valueType.getElementType());
            for (int i = 0; i < Math.min(size, inputList.size()); i++) {
                resultList.add(((ValueList) value).get(i));
            }
            return new ResultSingle(resultList);
        }
        throw new IllegalArgumentException("Operation first requires non-negative size argument");
    }

    @Override
    public Result last(int size) {
        if (size >= 0) {
            TypeCollection valueType = (TypeCollection) value.getType();
            ValueList inputList =
                    (ValueList) value.convertTo(new TypeCollection(Type.PrimaryType.LIST, valueType.getElementType()));
            ValueList resultList = new ValueList(valueType.getElementType());
            int iLow = Math.max(0, inputList.size()-size);
            int iHigh = Math.min(iLow+size, inputList.size());
            for (int i = iLow; i < iHigh; i++) {
                resultList.add(((ValueList) value).get(i));
            }
            return new ResultSingle(resultList);
        }
        throw new IllegalArgumentException("Operation last requires non-negative size argument");
    }

    @Override
    public Result random(int size) {
        if (size >= 0) {
            TypeCollection valueType = (TypeCollection) value.getType();
            ValueList inputList =
                    (ValueList) value.convertTo(new TypeCollection(Type.PrimaryType.LIST, valueType.getElementType()));
            ValueList resultList = new ValueList(valueType.getElementType());
            int valsToChoose = Math.min(inputList.size(), size);
            int maxStartI = inputList.size() - valsToChoose;
            int lowI = ThreadLocalRandom.current().nextInt(0, maxStartI + 1);
            resultList.addAll(((ValueList) value).getValue().subList(lowI, lowI+valsToChoose));
            return new ResultSingle(resultList);
        }
        throw new IllegalArgumentException("Operation first requires non-negative size argument");
    }

    @Override
    public ResultList convertTo(Type to) {
        if (!to.isCollection()) {
            TypeCollection valueType = (TypeCollection) value.getType();
            ValueList resultList = new ValueList(to);
            for (Value v : (ValueList) value) {
                resultList.add(v.convertTo(to));
            }
            return new ResultList(resultList);
        }
        return new ResultList((ValueList) value.convertTo(to));
    }

    @Override
    public Result filterNulls() {
        throw new UnsupportedOperationException("Operation filterNulls not supported on ResultSingle.");
    }

    @Override
    public ResultSingle isNull() {
        return new ResultSingle(new ValueBoolean(value.isNull()));
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public ResultType getResultType() { return resultType; }
}
