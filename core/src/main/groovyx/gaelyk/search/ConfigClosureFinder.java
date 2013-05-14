package groovyx.gaelyk.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;

import com.google.appengine.api.search.SearchService;

final class ConfigClosureFinder extends ClassCodeVisitorSupport {

    private static final Set<String> DESIRED_NAMES = new HashSet<String>(Arrays.asList("prepare", "search", "searchAsync"));

    private final SourceUnit source;
    private final ClassCodeVisitorSupport visitor;

    public ConfigClosureFinder(SourceUnit source) {
        this.source = source;
        this.visitor = new InsideConfigClosureVisitor(source);
    }

    @Override protected SourceUnit getSourceUnit() {
        return source;
    }
    
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (isDesiredMethod(call)) {
            ClosureExpression closureExpr = (ClosureExpression) ((TupleExpression)call.getArguments()).getExpression(((TupleExpression)call.getArguments()).getExpressions().size() - 1);
            visitor.visitClosureExpression(closureExpr);
        } else {
            super.visitMethodCallExpression(call);
        }
    }

    private boolean isDesiredMethod(MethodCallExpression call) {
        if (!(call.getObjectExpression() instanceof VariableExpression)) { return false; }
        VariableExpression objectExp = (VariableExpression) call.getObjectExpression();

        if (!isDesiredType(objectExp)) { return false; }

        if (!(call.getMethod() instanceof ConstantExpression)) { return false; }

        ConstantExpression method = (ConstantExpression) call.getMethod();

        if (!isDesiredMethodName(method.getValue())) { return false; }

        if (call.getArguments() instanceof TupleExpression) {
            TupleExpression args = (TupleExpression) call.getArguments();
            return args.getExpression(args.getExpressions().size()-1) instanceof ClosureExpression;
        }
        
        return false;
    }

    private boolean isDesiredType(VariableExpression objectExp) {
        return "search".equals(objectExp.getName()) || ClassHelper.make(SearchService.class).equals(objectExp.getType());
    }
    
    private boolean isDesiredMethodName(Object name){
        return DESIRED_NAMES.contains(name);
    }
}
