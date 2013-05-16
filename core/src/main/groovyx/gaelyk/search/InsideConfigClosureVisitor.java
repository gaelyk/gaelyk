package groovyx.gaelyk.search;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.SourceUnit;

final class InsideConfigClosureVisitor extends ClassCodeVisitorSupport {

    private SourceUnit source;

    public InsideConfigClosureVisitor(SourceUnit source) {
        this.source = source;
    }

    @Override protected SourceUnit getSourceUnit() {
        return source;
    }
    
    @Override public void visitExpressionStatement(ExpressionStatement statement) {
        if (statement.getExpression() instanceof BinaryExpression) {
            BinaryExpression bexp = (BinaryExpression) statement.getExpression();
            if (bexp.getLeftExpression() instanceof MethodCallExpression) {
                MethodCallExpression mce = (MethodCallExpression) bexp.getLeftExpression();
                if (mce.getMethod() instanceof ConstantExpression) {
                    ConstantExpression methodName = (ConstantExpression) mce.getMethod();
                    if("where".equals(methodName.getValue()) || "and".equals(methodName.getValue())){
                        addError("If you are using parentheses with " + methodName.getValue() + " keyword wrap the whole expression in parentheses like '" + methodName.getValue() + "((a == 1 || b > 2) && c =~ d)'", statement);
                    }
                }
            }
        }
        super.visitExpressionStatement(statement);
    }

    @Override public void visitMethodCallExpression(MethodCallExpression call) {
        if (call.getMethod() instanceof ConstantExpression) {
            ConstantExpression name = (ConstantExpression) call.getMethod();
            if("where".equals(name.getValue()) || "and".equals(name.getValue()) || "or".equals(name.getValue()) || "select".equals(name.getValue())){
                if (call.getArguments() instanceof TupleExpression) {
                    TupleExpression args = (TupleExpression) call.getArguments();
                    List<Expression> transformed = new ArrayList<Expression>();
                    for (Expression expression : args.getExpressions()) {
                        transformed.add(ExpressionToMethodCallsTransformer.INSTANCE.transform(expression));
                    }
                    call.setArguments(new ArgumentListExpression(transformed));
                }
            }
            
        }
        super.visitMethodCallExpression(call);
    }
}
