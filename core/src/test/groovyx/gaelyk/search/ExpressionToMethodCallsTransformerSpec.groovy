package groovyx.gaelyk.search

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation;

import spock.lang.Specification
import spock.lang.Unroll

class ExpressionToMethodCallsTransformerSpec extends Specification {

    @Unroll
    def "Expression #code is transformed to method call properly"(){
        expect:
        evaluate(code) == expected
        
        where:
        code            | expected
        'a > b'         | 'a > b'
        'a < b'         | 'a < b'
        'a >= b'        | 'a >= b'
        'a <= b'        | 'a <= b'
        'a == b'        | 'a = b'
        'a && b'        | 'a AND b'
        'a || b'        | 'a OR b'
        'a =~ b'        | 'a: b'
        'a ==~ b'       | 'a: b'
        '!a'            | 'NOT a'
        'a != b'        | 'NOT (a = b)'
    }
    
    private String evaluate(String code){
        String init = '''
        @groovy.transform.Canonical
        class Helper {
            String content

            String greaterThan(String anything){
                "$content > $anything"
            }

            String greaterThanEqual(String anything){
                "$content >= $anything"
            }
            String lowerThan(String anything){
                "$content < $anything"
            }

            String lowerThanEqual(String anything){
                "$content <= $anything"
            }

            String and(String anything){
                "$content AND $anything"
            }

            String or(String anything){
                "$content OR $anything"
            }

            String isEqualTo(String anything){
                "$content = $anything"
            }

            String isNotEqualTo(String anything){
                "NOT ($content = $anything)"
            }

            String isSameAs(String anything){
                "$content: $anything"
            }

            String not(){
                "NOT $content"
            }
        }

        Helper a = new Helper("a")
        String b = "b"   

'''
        
        newShell().evaluate(init + code)
    }
    
    private GroovyShell newShell() {
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.addCompilationCustomizers(new ASTTransformationCustomizer(new ExpressionToMethodCallsTransformation()))
        new GroovyShell(cc)
    }
}

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS) 
class ExpressionToMethodCallsTransformation implements ASTTransformation {
    @Override public void visit(ASTNode[] nodes, SourceUnit source) {
        ClassCodeExpressionTransformer ccet = new ClassCodeExpressionTransformer(){
            
            @Override protected SourceUnit getSourceUnit() {
                return source;
            }
            
            @Override public Expression transform(Expression exp) {
                ExpressionToMethodCallsTransformer.INSTANCE.transform(exp)
            }
        }

        for(ClassNode cn : source.getAST().getClasses()){
            ccet.visitClass(cn);
        }
    }
}
