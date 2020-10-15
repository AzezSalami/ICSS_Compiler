package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.LinkedList;

import static nl.han.ica.icss.ast.types.ExpressionType.*;


public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet sheet) {
        HashMap<String, ExpressionType> hashMap = new HashMap<>();
        for (ASTNode child : sheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child, hashMap);
            }
        }
        variableTypes.add(hashMap);
        for (ASTNode child : sheet.getChildren()) {
            if (child instanceof Stylerule) {
                checkStyleRule((Stylerule) child);
            }
        }
    }

    private void checkStyleRule(Stylerule styleRule) {
        HashMap<String, ExpressionType> hashMap = new HashMap<>();
        for (ASTNode child : styleRule.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child, hashMap);
            }
        }
        variableTypes.add(hashMap);
        for (ASTNode child : styleRule.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
                variableTypes.removeLast();
            }
        }
        variableTypes.removeLast();
    }

    private void checkIfClause(IfClause ifClause) {
        HashMap<String, ExpressionType> hashMap = new HashMap<>();
        if ((checkVariableValue((VariableReference) ifClause.conditionalExpression)) != BOOL) {
            ifClause.setError("conditionalExpression of the ifClause has invalid type");
        }
        for (ASTNode statement : ifClause.body) {
            if (statement instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) statement, hashMap);
            }
        }
        variableTypes.add(hashMap);
        for (ASTNode statement : ifClause.body) {
            if (statement instanceof Declaration) {
                checkDeclaration((Declaration) statement);
            } else if (statement instanceof IfClause) {
                checkIfClause((IfClause) statement);
                variableTypes.removeLast();
            }
        }
        if (ifClause.elseClause != null) {
            checkElseClause(ifClause.elseClause);
            variableTypes.removeLast();
        }

    }

    private void checkElseClause(ElseClause elseClause) {
        HashMap<String, ExpressionType> hashMap = new HashMap<>();
        for (ASTNode statement : elseClause.body) {
            if (statement instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) statement, hashMap);
            }
        }
        variableTypes.add(hashMap);
        for (ASTNode statement : elseClause.body) {
            if (statement instanceof Declaration) {
                checkDeclaration((Declaration) statement);
            } else if (statement instanceof IfClause) {
                checkIfClause((IfClause) statement);
            }
        }
    }

    private void checkDeclaration(Declaration declaration) {

        if (declaration.expression instanceof Operation) {
            checkOperation((Operation) declaration.expression);
        } else {
            switch (declaration.property.name) {
                case "width":
                case "height":
                    if (!checkMeasurement(declaration.expression)) {
                        declaration.expression.setError("Property '" + declaration.property.name + "' has invalid type");
                    }
                    break;
                case "color":
                case "background-color":
                    if (!checkColor(declaration.expression)) {
                        declaration.expression.setError("Property '" + declaration.property.name + "' has invalid type");
                    }
                    break;
            }
        }
    }

    private boolean checkColor(Expression expression) {
        if (expression instanceof Literal) {
            return expression instanceof ColorLiteral;
        } else if (expression instanceof VariableReference) {
            return (checkVariableValue((VariableReference) expression)) == COLOR;
        } else return false;
    }

    private boolean checkMeasurement(Expression expression) {
        if (expression instanceof Literal) {
            return expression instanceof PixelLiteral
                    || expression instanceof PercentageLiteral;
        } else if (expression instanceof VariableReference) {
            return (checkVariableValue((VariableReference) expression)) == PIXEL
                    || (checkVariableValue((VariableReference) expression)) == PERCENTAGE;
        } else return false;
    }

    private ExpressionType checkVariableValue(VariableReference variableReference) {
        ExpressionType type = null;
        for (int i = variableTypes.size(); i >= 1; i--) {
            if (variableTypes.get(i - 1).containsKey(variableReference.name)) {
                type = variableTypes.get(i - 1).get(variableReference.name);
                break;
            }
        }
        return type;
    }


    private void checkVariableAssignment(VariableAssignment variableAssignment, HashMap<String, ExpressionType> hashMap) {
        if (variableAssignment.expression instanceof Literal) {
            addVariable(variableAssignment, hashMap);
        } else if (variableAssignment.expression instanceof Operation) {
            checkOperation((Operation) variableAssignment.expression);
            hashMap.put(variableAssignment.name.name, GetExpressionType(variableAssignment.expression));
            variableTypes.add(hashMap);
        }
    }

    private void addVariable(VariableAssignment variableAssignment, HashMap<String, ExpressionType> hashMap) {
        if (variableAssignment.expression instanceof BoolLiteral) {
            hashMap.put(variableAssignment.name.name, BOOL);
        } else if (variableAssignment.expression instanceof ColorLiteral) {
            hashMap.put(variableAssignment.name.name, COLOR);
        } else if (variableAssignment.expression instanceof PercentageLiteral) {
            hashMap.put(variableAssignment.name.name, PERCENTAGE);
        } else if (variableAssignment.expression instanceof PixelLiteral) {
            hashMap.put(variableAssignment.name.name, PIXEL);
        } else if (variableAssignment.expression instanceof ScalarLiteral) {
            hashMap.put(variableAssignment.name.name, SCALAR);
        } else {
            hashMap.put(variableAssignment.name.name, UNDEFINED);
        }
    }

    private void checkOperation(Operation operation) {
        if (!(operation.lhs instanceof ColorLiteral) && !(operation.lhs instanceof BoolLiteral)) {
            if (operation instanceof MultiplyOperation) {
                checkMultiplyOperation((MultiplyOperation) operation);
            } else if (operation instanceof AddOperation) {
                checkAddOperation((AddOperation) operation);
            } else if (operation instanceof SubtractOperation) {
                checkSubtractOperation((SubtractOperation) operation);
            }
            //TODO : add variableType to the variable
        }

    }

    private void checkMultiplyOperation(MultiplyOperation operation) {
        if (!(operation.lhs instanceof Operation) && !(operation.rhs instanceof Operation)) {
            if ((operation.lhs instanceof ScalarLiteral)) {
                if (!checkMeasurement(operation.rhs))
                    operation.setError("One property of the operation has invalid type");
            } else if ((operation.rhs instanceof ScalarLiteral)) {
                if (!checkMeasurement(operation.lhs))
                    operation.setError("One property of the operation has invalid type");
            } else {
                operation.setError("One property of the operation has invalid type");
            }
        }
    }

    private void checkAddOperation(AddOperation operation) {
        if (!(operation.lhs instanceof Operation) && !(operation.rhs instanceof Operation)) {
            if (!checkMeasurement(operation.lhs) || !checkMeasurement(operation.rhs)) {
                operation.setError("One property of the operation has invalid type");
            }
        }
    }

    private void checkSubtractOperation(SubtractOperation operation) {
        if (!(operation.lhs instanceof Operation) && !(operation.rhs instanceof Operation)) {

            if (!checkMeasurement(operation.lhs) || !checkMeasurement(operation.rhs)) {
                operation.setError("One property of the operation has invalid type");
            }
        }
    }

    private ExpressionType GetExpressionType(Expression expression) {
        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            if ((operation.lhs instanceof ScalarLiteral) && (operation.rhs instanceof ScalarLiteral)) return SCALAR;
            if ((operation.lhs instanceof PixelLiteral) || (operation.rhs instanceof PixelLiteral)) return PIXEL;
            if ((operation.lhs instanceof PercentageLiteral) || (operation.rhs instanceof PercentageLiteral))
                return PERCENTAGE;
        } else {
            if (expression instanceof BoolLiteral) {
                return BOOL;
            } else if (expression instanceof ColorLiteral) {
                return COLOR;
            } else if (expression instanceof PercentageLiteral) {
                return PERCENTAGE;
            } else if (expression instanceof PixelLiteral) {
                return PIXEL;
            } else if (expression instanceof ScalarLiteral) {
                return SCALAR;
            } else {
                return UNDEFINED;
            }
        }
        return null;
    }
}
