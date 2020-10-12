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
        for (ASTNode child : sheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else {
                checkStyleRule((Stylerule) child);
            }
        }
    }

    private void checkStyleRule(Stylerule styleRule) {
        for (ASTNode child : styleRule.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }
    }

    private void checkIfClause(IfClause ifClause) {

    }

    private void checkDeclaration(Declaration declaration) {

        if (declaration.expression instanceof Operation) {
            checkOperation((Operation) declaration.expression);
        } else {
            switch (declaration.property.name) {
                case "width":
                    checkMeasurement(declaration, "width");
                    break;
                case "height":
                    checkMeasurement(declaration, "height");
                    break;
                case "color":
                    checkColor(declaration, "color");
                    break;
                case "background-color":
                    checkColor(declaration, "background-color");
                    break;
            }
        }
    }

    private void checkColor(Declaration declaration, String text) {
        if (!(declaration.expression instanceof ColorLiteral)
                && (checkVariableValue((VariableReference) declaration.expression)) != COLOR) {
            declaration.setError("Property '" + text + "' has invalid type");
        }
    }

    private void checkMeasurement(Declaration declaration, String text) {
        if (!(declaration.expression instanceof PixelLiteral)
                && !(declaration.expression instanceof PercentageLiteral)
                && (checkVariableValue((VariableReference) declaration.expression)) != PIXEL
                && (checkVariableValue((VariableReference) declaration.expression)) != PERCENTAGE) {
            declaration.setError("Property '" + text + "' has invalid type");
        }
    }

    private ExpressionType checkVariableValue(VariableReference variableReference) {
        for (HashMap<String, ExpressionType> map : variableTypes) {
            if (map.containsKey(variableReference.name)) {
                return map.get(variableReference.name);
            }
        }
        return null;
    }


    private void checkVariableAssignment(VariableAssignment variableAssignment) {
        if (variableAssignment.expression instanceof Literal) {
            checkLiteral(variableAssignment);
        } else if (variableAssignment.expression instanceof Operation) {
            checkOperation((Operation) variableAssignment.expression);
        }
    }

    private void checkLiteral(VariableAssignment variableAssignment) {
        HashMap<String, ExpressionType> hashMap = new HashMap<>();
        if (variableAssignment.expression instanceof BoolLiteral) {
            hashMap.put(variableAssignment.name.name, BOOL);
            variableTypes.add(hashMap);
        } else if (variableAssignment.expression instanceof ColorLiteral) {
            hashMap.put(variableAssignment.name.name, BOOL);
            variableTypes.add(hashMap);
        } else if (variableAssignment.expression instanceof PercentageLiteral) {
            hashMap.put(variableAssignment.name.name, PERCENTAGE);
            variableTypes.add(hashMap);
        } else if (variableAssignment.expression instanceof PixelLiteral) {
            hashMap.put(variableAssignment.name.name, PIXEL);
            variableTypes.add(hashMap);
        } else if (variableAssignment.expression instanceof ScalarLiteral) {
            hashMap.put(variableAssignment.name.name, SCALAR);
            variableTypes.add(hashMap);
        } else {
            hashMap.put(variableAssignment.name.name, UNDEFINED);
            variableTypes.add(hashMap);
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
        }

    }

    private void checkMultiplyOperation(MultiplyOperation operation) {
        if (!(operation.lhs instanceof ScalarLiteral) || !(operation.rhs instanceof ScalarLiteral)) {
            operation.setError("One Property of an operation has invalid type");
        } else if (operation.lhs instanceof VariableReference)
    }

    private void checkAddOperation(AddOperation operation) {
    }

    private void checkSubtractOperation(SubtractOperation operation) {

    }


}
