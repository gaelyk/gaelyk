package groovyx.gaelyk.search;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.syntax.Token;

public enum ExpressionToMethodCallsTransformer implements ExpressionTransformer {

    INSTANCE;

    @Override public Expression transform(Expression exp) {
        if (exp instanceof NotExpression) {
            NotExpression nexp = (NotExpression) exp;
            return new MethodCallExpression(transform(nexp.getExpression()), "not", MethodCallExpression.NO_ARGUMENTS);
        }
        
        if (exp instanceof BitwiseNegationExpression) {
            BitwiseNegationExpression bnexp = (BitwiseNegationExpression) exp;
            return new MethodCallExpression(transform(bnexp.getExpression()), "bitwiseNegate", MethodCallExpression.NO_ARGUMENTS);
        }
        
        if (exp instanceof BinaryExpression) {
            BinaryExpression bexp = (BinaryExpression) exp;
            String method = getMethod(bexp.getOperation());
            if (method == null) {
                // only transform if we want special handling
                return exp;
            }
            return new MethodCallExpression(transform(bexp.getLeftExpression()), method, transform(bexp.getRightExpression()));
        }
        return exp;
    }

    private String getMethod(Token operation) {
        String operator = operation.getText();
        if (">".equals(operator)) {
            return "greaterThan";
        } else if (">=".equals(operator)) {
            return "greaterThanEqual";
        } else if ("<".equals(operator)) {
            return "lowerThan";
        } else if ("<=".equals(operator)) {
            return "lowerThanEqual";
        } else if ("&&".equals(operator)) {
            return "and";
        } else if ("||".equals(operator)) {
            return "or";
        } else if ("==".equals(operator)) {
            return "isEqualTo";
        } else if ("=~".equals(operator)) {
            return "isSameAs";
        } else if ("==~".equals(operator)) {
            return "isSameAs";
        } else if ("!=".equals(operator)) { return "isNotEqualTo"; }
        return null;
    }

}
