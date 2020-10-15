package nl.han.ica.icss.transforms;

//BEGIN UITWERKING
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.ArrayList;
import java.util.List;
//EIND UITWERKING

public class RemoveIf implements Transform {

    @Override
    public void apply(AST ast) {
        transformStylesheet(ast.root);

    }
    private void transformStylesheet(Stylesheet node) {
        for (var child : node.getChildren()) {
            if (child instanceof Stylerule) {
                transformStylerule((Stylerule) child);
            }
        }
    }
    private void transformStylerule(Stylerule node) {
        node.body = (ArrayList<ASTNode>) transformDeclarationBlock(node);
    }

    private List<ASTNode> transformIfClause(IfClause node) {
        List<ASTNode> newBody = new ArrayList<>();

        if (((BoolLiteral) node.conditionalExpression).value) {
            newBody = transformDeclarationBlock(node);
        }
        else if (node.getElseClause() != null) {
            newBody = transformDeclarationBlock(node.elseClause);
        }

        return newBody;
    }

    private List<ASTNode> transformDeclarationBlock(ASTNode node) {
        List<ASTNode> finalDeclarations = new ArrayList<>();

        for (var child : node.getChildren()) {
            if (child instanceof IfClause) {
                finalDeclarations.addAll(transformIfClause((IfClause) child));
            }
            else if (child instanceof Declaration) {
                finalDeclarations.add(child);
            }
        }

        return finalDeclarations;
    }
    
}
