package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import static nl.han.ica.icss.ast.types.ExpressionType.*;
import static nl.han.ica.icss.ast.types.ExpressionType.UNDEFINED;

public class Generator {

    public String generate(AST ast) {
        return generateStylesheet(ast.root);
    }

    private String generateStylesheet(Stylesheet stylesheet) {
        StringBuilder s = new StringBuilder();
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                s.append(generateVariableAssignment((VariableAssignment) child));
            } else {
                s.append("\n").append(generateStylerule((Stylerule) child));
            }
        }
        return s + "\n";
    }

    private String generateVariableAssignment(VariableAssignment variableAssignment) {
        return variableAssignment.name.name + " :=" + generateExpression(variableAssignment.expression) + ";\n";
    }

    private String generateStylerule(Stylerule stylerule) {

        StringBuilder s = new StringBuilder(stylerule.selectors.get(0).toString() + " {\n");
        for (ASTNode child : stylerule.body) {
            if (child instanceof Declaration) {
                s.append("\t").append(generateDeclaration((Declaration) child));
            } else if (child instanceof IfClause) {
                s.append("\t").append(generateIfClause((IfClause) child));
            } else if (child instanceof VariableAssignment) {
                s.append(generateVariableAssignment((VariableAssignment) child));
            }
        }
        s.append("}");
        return s.toString();
    }

    private String generateIfClause(IfClause ifClause) {
        StringBuilder s = new StringBuilder("if[" + generateExpression(ifClause.getConditionalExpression()) + "] {" + "\n");
        for (ASTNode statement : ifClause.body) {
            s.append("\t  ");
            if (statement instanceof Declaration) {
                s.append(generateDeclaration((Declaration) statement));
            } else if (statement instanceof IfClause) {
                s.append(generateIfClause((IfClause) statement));
            }
        }
        if (ifClause.elseClause != null) {
            s.append("\t  ").append(generateElseClause(ifClause.elseClause));
        }
        return s + "\t}\n";
    }

    private String generateElseClause(ElseClause elseClause) {
        StringBuilder s = new StringBuilder("} else {" + "\n");
        for (ASTNode statement : elseClause.body) {
            s.append("\t    ");
            if (statement instanceof Declaration) {
                s.append(generateDeclaration((Declaration) statement));
            } else if (statement instanceof IfClause) {
                s.append(generateIfClause((IfClause) statement));
            }
        }
        return s + "  ";
    }

    private String generateDeclaration(Declaration declaration) {
        return declaration.property.name + ": " + generateExpression(declaration.expression) + ";\n";
    }

    private String generateExpression(Expression expression) {
        String s = "";
        if (expression instanceof Literal) {
            s += generateLiteral(expression);
        } else if (expression instanceof Operation) {
            s += generateOperation((Operation) expression);
        } else if (expression instanceof VariableReference){
            s += ((VariableReference) expression).name;
        }

        return s + "";
    }

    private String generateOperation(Operation operation) {
        String s = "";
        if (operation instanceof MultiplyOperation) {
            s += generateExpression(operation.lhs) + " * " + generateExpression(operation.rhs);
        } else if (operation instanceof AddOperation) {
            s += generateExpression(operation.lhs) + " + " + generateExpression(operation.rhs);
        } else if (operation instanceof SubtractOperation) {
            s += generateExpression(operation.lhs) + " + " + generateExpression(operation.rhs);
        }
        return s + "";
    }

    private String generateLiteral(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return Boolean.toString(((BoolLiteral) expression).value);
        } else if (expression instanceof ColorLiteral) {
            return ((ColorLiteral) expression).value;
        } else if (expression instanceof PercentageLiteral) {
            return ((PercentageLiteral) expression).value + "%";
        } else if (expression instanceof PixelLiteral) {
            return ((PixelLiteral) expression).value + "px";
        } else if (expression instanceof ScalarLiteral) {
            return Integer.toString(((ScalarLiteral) expression).value);
        } else if (expression instanceof VariableReference) {
            return ((VariableReference) expression).name;
        }
        return "";
    }


}
