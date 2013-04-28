package groovyx.gaelyk.search;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;


@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS) 
public class SearchDslAstTransformation implements ASTTransformation {


    @Override public void visit(ASTNode[] nodes, final SourceUnit source) {
        GroovyClassVisitor methodVisitor = new ConfigClosureFinder(source);
        
        for(ClassNode cn : source.getAST().getClasses()){
            methodVisitor.visitClass(cn);
        }
    }

}
