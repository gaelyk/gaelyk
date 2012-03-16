package groovyx.gaelyk.datastore

import groovyx.gaelyk.query.QueryBuilder

import java.lang.reflect.Modifier

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import com.google.appengine.api.datastore.Key;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class EntityTransformation implements ASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof ClassNode)) {
            println "Internal error: expecting [AnnotationNode, ClassNode] but got: ${Arrays.asList(nodes)}"
        }

        AnnotationNode anno = (AnnotationNode) nodes[0]
        ClassNode parent = (ClassNode) nodes[1]
        handleKey(parent, source)
        parent.addMethod(addDelegatedMethod('save', ClassHelper.makeWithoutCaching(Key).plainNodeReference))
        parent.addMethod(addDelegatedMethod('delete'))
        parent.addMethod(addStaticDelegatedMethod(parent, "get", [key: Object], parent.plainNodeReference))
        parent.addMethod(addStaticDelegatedMethod(parent, "delete", [key: Object]))
        parent.addMethod(addStaticDelegatedMethod(parent, "find", [key: Closure], parent.plainNodeReference))
        parent.addMethod(addStaticDelegatedMethod(parent, "count", [:], ClassHelper.int_TYPE))
        parent.addMethod(addStaticDelegatedMethod(parent, "count", [query: Closure], ClassHelper.int_TYPE))
        parent.addMethod(addStaticDelegatedMethod(parent, "count", [query: QueryBuilder], ClassHelper.int_TYPE))

        parent.addMethod(addStaticDelegatedMethod(parent, "findAll", [:], getPogoListNode(parent)))
        parent.addMethod(addStaticDelegatedMethod(parent, "findAll", [query: Closure], getPogoListNode(parent)))
        parent.addMethod(addStaticDelegatedMethod(parent, "findAll", [query: QueryBuilder], getPogoListNode(parent)))

        parent.addMethod(addStaticDelegatedMethod(parent, "iterate", [:], getPogoIteratorNode(parent)))
        parent.addMethod(addStaticDelegatedMethod(parent, "iterate", [query: Closure], getPogoIteratorNode(parent)))
        parent.addMethod(addStaticDelegatedMethod(parent, "iterate", [query: QueryBuilder], getPogoIteratorNode(parent)))
    }

    private ClassNode getPogoIteratorNode(ClassNode parent) {
        ClassNode pogoIteratorNode = ClassHelper.makeWithoutCaching(Iterator).plainNodeReference
        pogoIteratorNode.setGenericsTypes([new GenericsType(parent)] as GenericsType[])
        return pogoIteratorNode
    }

    private ClassNode getPogoListNode(ClassNode parent) {
        ClassNode pogoListNode = ClassHelper.makeWithoutCaching(List).plainNodeReference
        pogoListNode.setGenericsTypes([new GenericsType(parent)] as GenericsType[])
        return pogoListNode
    }

    private handleKey(ClassNode parent, SourceUnit source) {
        ClassNode keyAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Key)

        PropertyNode existingKeyProperty = parent.properties.find { PropertyNode prop ->
            prop.field.annotations.any { AnnotationNode anno ->
                anno.classNode == keyAnnoClassNode
            }
        }

        if (!existingKeyProperty) {
            existingKeyProperty = new PropertyNode(new FieldNode('id', Modifier.PUBLIC, ClassHelper.long_TYPE, parent, null), Modifier.PUBLIC, null, null)
            existingKeyProperty.field.addAnnotation(new AnnotationNode(keyAnnoClassNode))
            parent.addProperty existingKeyProperty
        }

        BlockStatement getKeyBlock = new BlockStatement()
        getKeyBlock.addStatement(new ExpressionStatement(new VariableExpression(existingKeyProperty.name, existingKeyProperty.type)))

        parent.addMethod new MethodNode(
                'get$key',
                Modifier.PUBLIC,
                existingKeyProperty.type,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                getKeyBlock
        )

        BlockStatement setKeyBlock = new BlockStatement()
        setKeyBlock.addStatement(new ExpressionStatement(new MethodCallExpression(new VariableExpression('this'), 'setProperty', new ArgumentListExpression(new ConstantExpression(existingKeyProperty.name), new VariableExpression(existingKeyProperty.name, existingKeyProperty.type)))))

        parent.addMethod new MethodNode(
                'set$key',
                Modifier.PUBLIC,
                ClassHelper.void_WRAPPER_TYPE,
                [new Parameter(existingKeyProperty.type, existingKeyProperty.name)] as Parameter[],
                ClassNode.EMPTY_ARRAY,
                setKeyBlock
        )
    }

    private MethodNode addDelegatedMethod(String name, ClassNode returnType = ClassHelper.DYNAMIC_TYPE) {
        def helper = ClassHelper.makeWithoutCaching(EntityTransformationHelper).plainNodeReference

        BlockStatement block = new BlockStatement()
        block.addStatement(new ReturnStatement(new MethodCallExpression(
                new ClassExpression(helper), name, new ArgumentListExpression(new VariableExpression('this'))
        )))

        new MethodNode(
                name,
                Modifier.PUBLIC,
                returnType,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                block
        )
    }

    private MethodNode addStaticDelegatedMethod(ClassNode parent, String name, Map<String, Class> parameters, ClassNode returnType = ClassHelper.DYNAMIC_TYPE) {
        def helper = ClassHelper.makeWithoutCaching(EntityTransformationHelper).plainNodeReference

        BlockStatement block = new BlockStatement()
        block.addStatement(new ReturnStatement(new MethodCallExpression(
                new ClassExpression(helper), name,
                new ArgumentListExpression(
                        new ClassExpression(parent),
                        * parameters.collect { String n, Class cls -> new VariableExpression(n)}
                ))))

        new MethodNode(
                name,
                Modifier.PUBLIC | Modifier.STATIC,
                returnType,
                parameters.collect { String n, Class cls -> new Parameter(ClassHelper.makeWithoutCaching(cls).plainNodeReference, n)} as Parameter[],
                ClassNode.EMPTY_ARRAY,
                block
        )
    }

}
