package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class EvalExpressions implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;


    public EvalExpressions() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        evaluateStylesheet(ast.root);

    }

    private void evaluateStylesheet(Stylesheet sheet) {
        HashMap<String, Literal> hashMap = new HashMap<>();
        List<ASTNode> toBeDeleted = new ArrayList<>();

        for (ASTNode child : sheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) child, hashMap);
                toBeDeleted.add(child);
            }
        }
        variableValues.add(hashMap);
        for (ASTNode child : sheet.getChildren()) {
            if (child instanceof Stylerule) {
                evaluateStyleRule((Stylerule) child);
            }
        }
        deletedToBeDeleted(sheet, toBeDeleted);
    }

    private void deletedToBeDeleted(ASTNode node, List<ASTNode> toBeDeleted) {
        for (ASTNode n : toBeDeleted) {
            node.removeChild(n);
        }
    }

    private void evaluateStyleRule(Stylerule styleRule) {
        HashMap<String, Literal> hashMap = new HashMap<>();
        for (ASTNode child : styleRule.getChildren()) {
            if (child instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) child, hashMap);
                styleRule.removeChild(child);
            }
        }
        variableValues.add(hashMap);
        for (ASTNode child : styleRule.getChildren()) {
            if (child instanceof Declaration) {
                var l = getLiteral(((Declaration) child).expression);
                ((Declaration) child).expression = getLiteral(((Declaration) child).expression);
            } else if (child instanceof IfClause) {
                evaluateIfClause((IfClause) child);
                variableValues.removeLast();
                styleRule.removeChild(child);
            }
        }
        variableValues.removeLast();
    }

    private void evaluateIfClause(IfClause ifClause) {
        HashMap<String, Literal> hashMap = new HashMap<>();
        List<ASTNode> toBeDeleted = new ArrayList<>();
        ifClause.conditionalExpression = getLiteral(ifClause.conditionalExpression);
            for (ASTNode statement : ifClause.body) {
                if (statement instanceof VariableAssignment) {
                    evaluateVariableAssignment((VariableAssignment) statement, hashMap);
                    toBeDeleted.add(statement);
                }
            }
            variableValues.add(hashMap);
            for (ASTNode statement : ifClause.body) {
                if (statement instanceof Declaration) {
                    ((Declaration) statement).expression = getLiteral(((Declaration) statement).expression);
                } else if (statement instanceof IfClause) {
                    evaluateIfClause((IfClause) statement);
                    variableValues.removeLast();
                }
            }
        if (ifClause.elseClause != null) {
            evaluateElseClause(ifClause.elseClause);
            variableValues.removeLast();
        }
        deletedToBeDeleted(ifClause, toBeDeleted);
    }

    private void evaluateElseClause(ElseClause elseClause) {
        HashMap<String, Literal> hashMap = new HashMap<>();
        List<ASTNode> toBeDeleted = new ArrayList<>();
        for (ASTNode statement : elseClause.body) {
            if (statement instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) statement, hashMap);
                toBeDeleted.add(statement);
            }
        }
        variableValues.add(hashMap);
        for (ASTNode statement : elseClause.body) {
            if (statement instanceof Declaration) {
                ((Declaration) statement).expression = getLiteral(((Declaration) statement).expression);
            } else if (statement instanceof IfClause) {
                evaluateIfClause((IfClause) statement);
            }
        }
        deletedToBeDeleted(elseClause, toBeDeleted);
    }


    private void evaluateVariableAssignment(VariableAssignment variableAssignment, HashMap<String, Literal> hashMap) {
        if (variableAssignment.expression instanceof Literal) {
            addVariable(variableAssignment, hashMap);
            variableValues.add(hashMap);
        } else if (variableAssignment.expression instanceof Operation) {
            hashMap.put(variableAssignment.name.name, getLiteral(variableAssignment.expression));
            variableValues.add(hashMap);
        }
    }

    private Literal getLiteral(Expression expression) {
        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            if (operation instanceof MultiplyOperation) {
                return calcMultiplyOperation((MultiplyOperation) operation);
            } else if (operation instanceof AddOperation) {
                return calcAddOperation((AddOperation) operation);
            } else if (operation instanceof SubtractOperation) {
                return calcSubtractOperation((SubtractOperation) operation);
            }
        } else if (expression instanceof Literal) {
            if (expression instanceof BoolLiteral) {
                return new BoolLiteral(((BoolLiteral) expression).value);
            } else if (expression instanceof ColorLiteral) {
                return new ColorLiteral(((ColorLiteral) expression).value);
            } else if (expression instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) expression).value);
            } else if (expression instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) expression).value);
            } else if (expression instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) expression).value);
            }
        } else if (expression instanceof VariableReference) {
            return evaluateVariableReference(((VariableReference) expression));
        }
        return null;
    }

    private Literal evaluateVariableReference(VariableReference variableReference) {
        Literal value = null;
        for (int i = variableValues.size(); i >= 1; i--) {
            if (variableValues.get(i - 1).containsKey(variableReference.name)) {
                value = variableValues.get(i - 1).get(variableReference.name);
                break;
            }
        }
        return value;
    }

    private Literal calcSubtractOperation(SubtractOperation operation) {
        Literal result = null;
        int total;
        var lhs = getLiteral(operation.lhs);
        var rhs = getLiteral(operation.rhs);

        if ((lhs instanceof PixelLiteral) && (rhs instanceof PixelLiteral)) {
            total = ((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value;
            result = new PixelLiteral(total);
        } else if ((lhs instanceof PercentageLiteral) && (rhs instanceof PercentageLiteral)) {
            total = ((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value;
            result = new PercentageLiteral(total);
        }
        return result;
    }


    private Literal calcAddOperation(AddOperation operation) {
        Literal result = null;
        int total;
        var lhs = getLiteral(operation.lhs);
        var rhs = getLiteral(operation.rhs);

        if ((lhs instanceof PixelLiteral) && (rhs instanceof PixelLiteral)) {
            total = ((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value;
            result = new PixelLiteral(total);
        } else if ((lhs instanceof PercentageLiteral) && (rhs instanceof PercentageLiteral)) {
            total = ((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value;
            result = new PercentageLiteral(total);

        }
        return result;
    }

    private Literal calcMultiplyOperation(MultiplyOperation operation) {
        Literal result = null;
        int total;
        var lhs = getLiteral(operation.lhs);
        var rhs = getLiteral(operation.rhs);

        if ((lhs instanceof ScalarLiteral) && (rhs instanceof PixelLiteral)) {
            total = ((ScalarLiteral) lhs).value * ((PixelLiteral) rhs).value;
            result = new PixelLiteral(total);
        } else if ((lhs instanceof PixelLiteral) && (rhs instanceof ScalarLiteral)) {
            total = ((PixelLiteral) lhs).value * ((ScalarLiteral) rhs).value;
            result = new PixelLiteral(total);
        } else if ((lhs instanceof ScalarLiteral) && (rhs instanceof PercentageLiteral)) {
            total = ((ScalarLiteral) lhs).value * ((PercentageLiteral) rhs).value;
            result = new PercentageLiteral(total);
        } else if ((lhs instanceof PercentageLiteral) && (rhs instanceof ScalarLiteral)) {
            total = ((PercentageLiteral) lhs).value * ((ScalarLiteral) rhs).value;
            result = new PercentageLiteral(total);
        }
        return result;
    }

    private void addVariable(VariableAssignment variableAssignment, HashMap<String, Literal> hashMap) {
        if (variableAssignment.expression instanceof BoolLiteral) {
            hashMap.put(variableAssignment.name.name, new BoolLiteral(((BoolLiteral) variableAssignment.expression).value));
        } else if (variableAssignment.expression instanceof ColorLiteral) {
            hashMap.put(variableAssignment.name.name, new ColorLiteral(((ColorLiteral) variableAssignment.expression).value));
        } else if (variableAssignment.expression instanceof PercentageLiteral) {
            hashMap.put(variableAssignment.name.name, new PercentageLiteral(((PercentageLiteral) variableAssignment.expression).value));
        } else if (variableAssignment.expression instanceof PixelLiteral) {
            hashMap.put(variableAssignment.name.name, new PixelLiteral(((PixelLiteral) variableAssignment.expression).value));
        } else if (variableAssignment.expression instanceof ScalarLiteral) {
            hashMap.put(variableAssignment.name.name, new ScalarLiteral(((ScalarLiteral) variableAssignment.expression).value));
        }
    }


}
