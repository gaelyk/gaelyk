/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import com.google.appengine.api.datastore.Key

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class EntityTransformation extends AbstractASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof ClassNode)) {
            println "Internal error: expecting [AnnotationNode, ClassNode] but got: ${Arrays.asList(nodes)}"
        }

        AnnotationNode anno = (AnnotationNode) nodes[0]
        ClassNode parent = (ClassNode) nodes[1]
        ClassNode keyType = handleKey(parent, source)

        if(!keyType) {
            return
        }

        ClassNode keyCN = ClassHelper.makeWithoutCaching(Key).plainNodeReference

        handleVersion(parent, source)
        boolean hasParent = handleParent(parent, source, keyCN)
        handleEntityProperties(anno, parent, source)

        addDatastoreEntityInterface(keyType, parent)

        parent.addMethod(addDelegatedMethod('save', keyCN))
        parent.addMethod(addDelegatedMethod('delete'))
        if(hasParent){
            parent.addMethod(addStaticDelegatedMethod(parent, "get", [parentKey: keyCN, key: keyType], parent.plainNodeReference))
            parent.addMethod(addStaticDelegatedMethod(parent, "delete", [parentKey: keyCN, key: keyType]))
        } else {
            parent.addMethod(addStaticDelegatedMethod(parent, "get", [key: keyType], parent.plainNodeReference))
            parent.addMethod(addStaticDelegatedMethod(parent, "delete", [key: keyType]))
        }
        parent.addMethod(addStaticDelegatedMethod(parent, "find", [key: Closure], parent.plainNodeReference, [key: makeDelegatesToAnno(QueryBuilder, Closure.DELEGATE_FIRST)]))
        parent.addMethod(addStaticDelegatedMethod(parent, "count", [:], ClassHelper.int_TYPE))
        parent.addMethod(addStaticDelegatedMethod(parent, "count", [query: Closure], ClassHelper.int_TYPE, [query: makeDelegatesToAnno(QueryBuilder, Closure.DELEGATE_FIRST)]))
        parent.addMethod(addStaticDelegatedMethod(parent, "count", [query: QueryBuilder], ClassHelper.int_TYPE))

        parent.addMethod(addStaticDelegatedMethod(parent, "findAll", [:], getBoundListNode(parent)))
        parent.addMethod(addStaticDelegatedMethod(parent, "findAll", [query: Closure], getBoundListNode(parent), [query: makeDelegatesToAnno(QueryBuilder, Closure.DELEGATE_FIRST)]))
        parent.addMethod(addStaticDelegatedMethod(parent, "findAll", [query: QueryBuilder], getBoundListNode(parent)))

        parent.addMethod(addStaticDelegatedMethod(parent, "iterate", [:], getPogoIteratorNode(parent)))
        parent.addMethod(addStaticDelegatedMethod(parent, "iterate", [query: Closure], getPogoIteratorNode(parent), [query: makeDelegatesToAnno(QueryBuilder, Closure.DELEGATE_FIRST)]))
        parent.addMethod(addStaticDelegatedMethod(parent, "iterate", [query: QueryBuilder], getPogoIteratorNode(parent)))
    }
    
    private AnnotationNode makeDelegatesToAnno(Class cls, int strategy){
        AnnotationNode anno = new AnnotationNode(ClassHelper.makeWithoutCaching(DelegatesTo).plainNodeReference)
        anno.addMember('value', new ClassExpression(ClassHelper.makeWithoutCaching(cls).plainNodeReference))
        anno.addMember('strategy', new ConstantExpression(strategy, true))
        anno
    }

    private addDatastoreEntityInterface(ClassNode keyType, ClassNode parent) {
        ClassNode datastoreEntityInterface = ClassHelper.makeWithoutCaching(DatastoreEntity).plainNodeReference
        datastoreEntityInterface.setGenericsTypes([new GenericsType(keyType)] as GenericsType[])
        parent.addInterface(datastoreEntityInterface)
    }

    private ClassNode getPogoIteratorNode(ClassNode parent) {
        ClassNode pogoIteratorNode = ClassHelper.makeWithoutCaching(Iterator).plainNodeReference
        pogoIteratorNode.setGenericsTypes([new GenericsType(parent)] as GenericsType[])
        return pogoIteratorNode
    }

    private ClassNode getBoundListNode(ClassNode parent) {
        ClassNode pogoListNode = ClassHelper.makeWithoutCaching(List).plainNodeReference
        pogoListNode.setGenericsTypes([new GenericsType(parent)] as GenericsType[])
        return pogoListNode
    }

    private ClassNode handleKey(ClassNode parent, SourceUnit source) {
        ClassNode keyAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Key).plainNodeReference

        PropertyNode existingKeyProperty = findPropertyIncludingSuper(parent) { PropertyNode prop ->
            prop.field.annotations.any { AnnotationNode anno ->
                anno.classNode == keyAnnoClassNode
            }
        }

        if(existingKeyProperty && !(existingKeyProperty.type in [
            ClassHelper.long_TYPE,
            ClassHelper.Long_TYPE,
            ClassHelper.STRING_TYPE
        ])){
            source.addError(new SyntaxException("Only long or String are allowed as a key property! Found ${existingKeyProperty.type.name} ${existingKeyProperty.declaringClass.name}.${existingKeyProperty.name}.", existingKeyProperty.lineNumber, existingKeyProperty.columnNumber))
            return
        }

        if (!existingKeyProperty) {
            existingKeyProperty = new PropertyNode(new FieldNode('id', Modifier.PUBLIC, ClassHelper.long_TYPE, parent, null), Modifier.PUBLIC, null, null)
            existingKeyProperty.field.addAnnotation(new AnnotationNode(keyAnnoClassNode))
            parent.addProperty existingKeyProperty
        }

        boolean hasNumericKey = existingKeyProperty.type in [ClassHelper.long_TYPE, ClassHelper.Long_TYPE]

        parent.addMethod new MethodNode(
                'hasDatastoreNumericKey',
                Modifier.PUBLIC,
                ClassHelper.boolean_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(hasNumericKey ? ConstantExpression.PRIM_TRUE : ConstantExpression.PRIM_FALSE)
                )

        parent.addMethod new MethodNode(
                'hasDatastoreKey',
                Modifier.PUBLIC,
                ClassHelper.boolean_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(ConstantExpression.PRIM_TRUE)
                )

        parent.addMethod new MethodNode(
                'getDatastoreKey',
                Modifier.PUBLIC,
                // XXX: can't bound yet
                // hasNumericKey ? ClassHelper.Long_TYPE : ClassHelper.STRING_TYPE,
                ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(new VariableExpression(existingKeyProperty))
                )
        
        BinaryExpression bes = new AstBuilder().buildFromString("this.${existingKeyProperty.name} = ${existingKeyProperty.name}")[0].statements[0].expression

        Parameter setKeyParameter = new Parameter(/* hasNumericKey ? ClassHelper.Long_TYPE : ClassHelper.STRING_TYPE*/ ClassHelper.OBJECT_TYPE, existingKeyProperty.name)
        BlockStatement setKeyBlock = new BlockStatement()
        setKeyBlock.addStatement(new ExpressionStatement(
                bes
            )
        )
        

        parent.addMethod new MethodNode(
                'setDatastoreKey',
                Modifier.PUBLIC,
                ClassHelper.VOID_TYPE,
                [setKeyParameter] as Parameter[],
                ClassNode.EMPTY_ARRAY,
                setKeyBlock
                )
        existingKeyProperty.type
    }

    private void handleVersion(ClassNode parent, SourceUnit source) {
        ClassNode versionAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Version).plainNodeReference

        PropertyNode existingVersionProperty = findPropertyIncludingSuper(parent) { PropertyNode prop ->
            prop.field.annotations.any { AnnotationNode anno ->
                anno.classNode == versionAnnoClassNode
            }
        }

        if(existingVersionProperty && !(existingVersionProperty.type in [ClassHelper.long_TYPE, ClassHelper.Long_TYPE])){
            source.addError(new SyntaxException("Only long is allowed as a version property! Found ${existingVersionProperty.type.name} ${existingVersionProperty.declaringClass.name}.${existingVersionProperty.name}.", existingVersionProperty.lineNumber, existingVersionProperty.columnNumber))
            return
        }

        parent.addMethod new MethodNode(
                'hasDatastoreVersion',
                Modifier.PUBLIC,
                ClassHelper.boolean_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(existingVersionProperty ? ConstantExpression.PRIM_TRUE : ConstantExpression.PRIM_FALSE)
                )

        BlockStatement getVersionBlock = new BlockStatement()
        getVersionBlock.addStatement(new ExpressionStatement(existingVersionProperty ? new VariableExpression(existingVersionProperty) : new ConstantExpression(0)))

        parent.addMethod new MethodNode(
                'getDatastoreVersion',
                Modifier.PUBLIC,
                ClassHelper.long_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                getVersionBlock
                )

        def mce
        if(existingVersionProperty){
            mce = new AstBuilder().buildFromString("this.${existingVersionProperty.name} = ${existingVersionProperty.name}")[0].statements[0].expression
        } else {
            mce = ConstantExpression.NULL
        }
        BlockStatement setKeyBlock = new BlockStatement()
        setKeyBlock.addStatement(new ExpressionStatement(mce))

        parent.addMethod new MethodNode(
                'setDatastoreVersion',
                Modifier.PUBLIC,
                ClassHelper.VOID_TYPE,
                [
                    new Parameter(ClassHelper.long_TYPE, existingVersionProperty?.name ?: 'version')] as Parameter[],
                ClassNode.EMPTY_ARRAY,
                setKeyBlock
                )
    }

    private boolean handleParent(ClassNode parent, SourceUnit source, ClassNode keyCN) {
        ClassNode parentAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Parent).plainNodeReference

        PropertyNode existingParentProperty = findPropertyIncludingSuper(parent) { PropertyNode prop ->
            prop.field.annotations.any { AnnotationNode anno ->
                anno.classNode == parentAnnoClassNode
            }
        }

        if(existingParentProperty && existingParentProperty.type != keyCN){
            source.addError(new SyntaxException("Only Key is allowed as a version property! Found ${existingParentProperty.type.name} ${existingParentProperty.declaringClass.name}.${existingParentProperty.name}.", existingParentProperty.lineNumber, existingParentProperty.columnNumber))
            return false
        }

        parent.addMethod new MethodNode(
                'hasDatastoreParent',
                Modifier.PUBLIC,
                ClassHelper.boolean_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(existingParentProperty ? ConstantExpression.PRIM_TRUE : ConstantExpression.PRIM_FALSE)
                )

        BlockStatement getParentBlock = new BlockStatement()
        getParentBlock.addStatement(existingParentProperty ? new ExpressionStatement(new VariableExpression(existingParentProperty)) : new ReturnStatement(ConstantExpression.NULL))



        parent.addMethod new MethodNode(
                'getDatastoreParent',
                Modifier.PUBLIC,
                keyCN,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                getParentBlock
                )

        BlockStatement setKeyBlock = new BlockStatement()
        if(existingParentProperty){
            setKeyBlock.addStatement(new ExpressionStatement(new AstBuilder().buildFromString("this.${existingParentProperty.name} = ${existingParentProperty.name}")[0].statements[0].expression))
        } else {
            setKeyBlock.addStatement(new ReturnStatement(ConstantExpression.NULL))
        }

        parent.addMethod new MethodNode(
                'setDatastoreParent',
                Modifier.PUBLIC,
                ClassHelper.VOID_TYPE,
                [
                    new Parameter(keyCN, existingParentProperty?.name ?: 'parent')] as Parameter[],
                ClassNode.EMPTY_ARRAY,
                setKeyBlock
                )
        existingParentProperty
    }

    private void handleEntityProperties(AnnotationNode anno, ClassNode parent, SourceUnit source) {
        ClassNode indexedAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Indexed).plainNodeReference
        ClassNode unindexedAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Unindexed).plainNodeReference
        ClassNode ignoreAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Ignore).plainNodeReference
        ClassNode versionAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Version).plainNodeReference
        ClassNode keyAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Key).plainNodeReference
        ClassNode parentAnnoClassNode = ClassHelper.makeWithoutCaching(groovyx.gaelyk.datastore.Parent).plainNodeReference

        boolean defaultIndexed = memberHasValue(anno, 'unindexed', false)

        List<String> indexed = []
        List<String> unindexed = []

        eachPropertyIncludingSuper(parent) { PropertyNode prop ->
            if(Modifier.isStatic(prop.modifiers)) {
                return
            }
            boolean ignored = prop.field.annotations.any { AnnotationNode a ->
                a.classNode == ignoreAnnoClassNode || a.classNode == versionAnnoClassNode || a.classNode == keyAnnoClassNode || a.classNode == parentAnnoClassNode
            }
            if(ignored){
                return
            }
            boolean hasUnindexedAnno = prop.field.annotations.any { AnnotationNode a ->
                a.classNode == unindexedAnnoClassNode
            }
            if(hasUnindexedAnno){
                unindexed << prop.name
                return
            }
            boolean hasIndexedAnno = prop.field.annotations.any { AnnotationNode a ->
                a.classNode == indexedAnnoClassNode
            }
            if(hasIndexedAnno){
                indexed << prop.name
                return
            }
            if(defaultIndexed){
                indexed << prop.name
            } else {
                unindexed << prop.name
            }
        }

        parent.addField new FieldNode('DATASTORE_INDEXED_PROPERTIES', Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL, getBoundListNode(ClassHelper.STRING_TYPE), parent, buildList(indexed))

        parent.addMethod new MethodNode(
                'getDatastoreIndexedProperties',
                Modifier.PUBLIC,
                getBoundListNode(ClassHelper.STRING_TYPE),
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(new VariableExpression('DATASTORE_INDEXED_PROPERTIES'))
                )

        parent.addField new FieldNode('DATASTORE_UNINDEXED_PROPERTIES', Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL, getBoundListNode(ClassHelper.STRING_TYPE), parent, buildList(unindexed))
        parent.addMethod new MethodNode(
                'getDatastoreUnindexedProperties',
                Modifier.PUBLIC,
                getBoundListNode(ClassHelper.STRING_TYPE),
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(new VariableExpression('DATASTORE_UNINDEXED_PROPERTIES'))
                )
    }

    private void eachPropertyIncludingSuper(ClassNode parent, Closure iterator){
        parent.properties.each iterator
        ClassNode superNode = parent.superClass
        if(superNode && superNode != ClassHelper.OBJECT_TYPE){
            eachPropertyIncludingSuper(superNode, iterator)
        }
    }

    private PropertyNode findPropertyIncludingSuper(ClassNode parent, Closure filter){
        PropertyNode prop = parent.getProperties().find filter
        if(prop){
            return prop
        }
        ClassNode superNode = parent.superClass
        if(superNode && superNode != ClassHelper.OBJECT_TYPE){
            return findPropertyIncludingSuper(superNode, filter)
        }
        null
    }

    private Expression buildList(List<String> values) {
        ListExpression list = new ListExpression()
        for (String value in values) {
            list.addExpression(new ConstantExpression(value))
        }
        list
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

    private MethodNode addStaticDelegatedMethod(ClassNode parent, String name, Map<String, Class> parameters, ClassNode returnType = ClassHelper.DYNAMIC_TYPE, Map<String, List<AnnotationNode>> paramAnnos = [:]) {
        def helper = ClassHelper.makeWithoutCaching(EntityTransformationHelper).plainNodeReference

        def methodParams = parameters.collect { String n, cls ->
            ClassNode clsNode = cls instanceof ClassNode ? cls : ClassHelper.makeWithoutCaching(cls).plainNodeReference
            Parameter param = new Parameter(clsNode.plainNodeReference, n)
            if(paramAnnos[n]){
                paramAnnos[n].each {
                    param.addAnnotation(it)
                }
            }
            param
        }
        def variables = methodParams.collect { new VariableExpression(it) }

        BlockStatement block = new BlockStatement()
        block.addStatement(new ReturnStatement(new MethodCallExpression(
                new ClassExpression(helper), name,
                new ArgumentListExpression(
                new ClassExpression(parent),
                * variables
                ))))

        new MethodNode(
                name,
                Modifier.PUBLIC | Modifier.STATIC,
                returnType,
                methodParams as Parameter[],
                ClassNode.EMPTY_ARRAY,
                block
                )
    }
    
//    private void tester(Object id){
//        this.id = id
//    }
}
