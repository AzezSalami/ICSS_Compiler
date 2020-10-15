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
            s.append(generateStylerule((Stylerule) child));
        }
        return s + "\n";
    }

    private String generateStylerule(Stylerule stylerule) {

        StringBuilder s = new StringBuilder(stylerule.selectors.get(0).toString() + " {\n");
        for (ASTNode child : stylerule.body) {
            s.append("\t").append(generateDeclaration((Declaration) child));
        }
        s.append("}\n");
        return s.toString();
    }


    private String generateDeclaration(Declaration declaration) {
        return declaration.property.name + ": " + generateExpression(declaration.expression) + ";\n";
    }

    private String generateExpression(Expression expression) {
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
