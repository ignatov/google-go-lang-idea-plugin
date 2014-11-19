package ro.redeul.google.go.inspection;

import org.jetbrains.annotations.NotNull;
import ro.redeul.google.go.lang.psi.GoFile;
import ro.redeul.google.go.lang.psi.expressions.GoExpr;
import ro.redeul.google.go.lang.psi.expressions.binary.GoBinaryExpression;
import ro.redeul.google.go.lang.psi.expressions.binary.GoRelationalExpression;
import ro.redeul.google.go.lang.psi.typing.GoType;
import ro.redeul.google.go.lang.psi.typing.GoTypeInterface;
import ro.redeul.google.go.lang.psi.typing.GoTypeName;
import ro.redeul.google.go.lang.psi.typing.GoTypePointer;
import ro.redeul.google.go.lang.psi.visitors.GoRecursiveElementVisitor;

public class TypeMatchInspection extends AbstractWholeGoFileInspection {
    @Override
    protected void doCheckFile(@NotNull GoFile file, @NotNull final InspectionResult result) {
        new GoRecursiveElementVisitor() {
            @Override
            public void visitBinaryExpression(GoBinaryExpression expression) {
                checkBinaryExpression(result, expression);
            }

            @Override
            public void visitRelExpression(GoRelationalExpression expression) {
                if ( !preValidate(expression) )
                    return;

                checkRelationalExpression(result, expression);
            }
        }.visitFile(file);
    }

    private boolean preValidate(GoBinaryExpression expression) {
        GoExpr left = expression.getLeftOperand();
        GoExpr right = expression.getRightOperand();

        if (left == null || right == null)
            return false;

        if (left.isConstantExpression() || right.isConstantExpression())
            return false;

        GoType[] leftTypes = left.getType();
        GoType[] rightTypes = right.getType();
        if (leftTypes.length == 0 || rightTypes.length == 0 || leftTypes[0] == null || rightTypes[0] == null)
            return false;

        return true;
    }


    private void checkRelationalExpression(InspectionResult result, GoRelationalExpression expression) {
        // TODO IMPLEMENT THIS
        /*GoType leftType = expression.getLeftOperand().getType()[0];
        GoType rightType = expression.getRightOperand().getType()[0];

        switch (expression.op()) {
            case Eq:
            case NotEq:
        }*/
    }

    public static void checkBinaryExpression(InspectionResult result, GoBinaryExpression expression) {
        GoExpr left = expression.getLeftOperand();
        GoExpr right = expression.getRightOperand();
        if (left == null || right == null) {
            return;
        }
        if (left.isConstantExpression() || right.isConstantExpression()){
            return;
        }
        GoType[] leftTypes = left.getType();
        GoType[] rightTypes = right.getType();
        if (leftTypes.length == 0 || rightTypes.length == 0){
            return;
        }

        String operator = expression.op().toString();
        boolean equality = operator.equals("!=") || operator.equals("==");
        boolean shift = operator.equals("<<")||operator.equals(">>");
        for (GoType leftType : leftTypes) {
            for (GoType rightType : rightTypes) {
                if (leftType == null || rightType == null) {
                    return;
                }
                GoType leftUnder = leftType.underlyingType();
                GoType rightUnder = rightType.underlyingType();
                boolean hasInterface = leftUnder instanceof GoTypeInterface || rightUnder instanceof GoTypeInterface;
                if (!equality) {
                    if (leftType instanceof GoTypePointer || rightType instanceof GoTypePointer){
                        result.addProblem(expression, "operator "+operator+" not defined on pointer");
                        return;
                    }
                    if (hasInterface) {
                        result.addProblem(expression, "operator "+operator+" not defined on interface");
                        return;
                    }
                    if (shift){
                        String rightUnderStr = rightUnder.toString();
                        if (rightUnderStr.startsWith("uint")||rightUnderStr.equals("byte")){
                            return;
                        }else{
                            result.addProblem(expression, "shift count type " + rightUnder+", must be unsigned integer");
                            return;
                        }
                    }
                }else{
                    if (hasInterface) {
                        return;
                    }
                    if (leftType instanceof GoTypePointer && rightType instanceof GoTypePointer){
                        GoTypePointer lptr = (GoTypePointer)leftType;
                        GoTypePointer rptr = (GoTypePointer)rightType;
                        leftType = lptr.getTargetType();
                        rightType = rptr.getTargetType();
                    }
                }
                if (leftType instanceof GoTypeName && rightType instanceof GoTypeName) {
                    String leftName = ((GoTypeName)leftType).getName();
                    if (leftName.equals("byte")) {
                        leftName = "uint8";
                    }else if (leftName.equals("rune")) {
                        leftName = "int32";
                    }
                    String rightName = ((GoTypeName)rightType).getName();
                    if (rightName.equals("byte")) {
                        rightName = "uint8";
                    }else if (rightName.equals("rune")) {
                        rightName = "int32";
                    }
                    if (leftName.equals(rightName)) {
                        return;
                    }
                }
            }
        }
        result.addProblem(expression, "mismatched types");
    }
}