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
package groovyx.gaelyk.query

import groovyx.gaelyk.datastore.Entity

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import com.google.appengine.api.datastore.Query.FilterOperator
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.syntax.SyntaxException

/**
 * This AST transformation makes two transformations at the AST level.
 * First of all, this transformation is applied only with the context of a closure
 * passed to the <code>datastore.query {}</code> or <code>datastore.execute {}</code> calls.
 * The two modifcations made on the AST are to transform the <code>where prop op value</code> calls
 * into a <code>where new WhereClause(prop, op, value)</code> call,
 * and the <code>from kindName as className</code>
 * into a <code>from kindName, className</code> call.
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class QueryDslTransformation implements ASTTransformation {

    /**
     * Visit the AST of the scripts and classes that contain datastore query/execute calls.
     *
     * @param nodes a null array since we use a global transformation
     * @param source the source unit on which we'll apply the transformations
     */
    void visit(ASTNode[] nodes, SourceUnit source) {

        def whereMethodVisitor = new ClassCodeVisitorSupport() {
            void visitMethodCallExpression(MethodCallExpression clauseCall) {

                // transform "where a op b" into "where WhereClause(a, op, b)"
                if (
                    clauseCall.method instanceof ConstantExpression &&
                    (clauseCall.method.value == "where" || clauseCall.method.value == "and") &&
                    clauseCall.arguments instanceof ArgumentListExpression &&
                    clauseCall.arguments.expressions.size() == 1 &&
                    clauseCall.arguments.expressions[0].class in [BinaryExpression, NotExpression, VariableExpression]
                ) {
                    def column, operation, value

                    if (clauseCall.arguments.expressions[0] instanceof BinaryExpression) {
                        BinaryExpression binExpr = clauseCall.arguments.expressions[0]

                        column = binExpr.leftExpression
                        // filter operator expression
                        ConstantExpression op = null
                        switch (binExpr.operation.text) {
                            case '=':
                                source.addError(new SyntaxException(
                                        "You must use '==' instead of '=' for equality comparisons",
                                        binExpr.operation.getStartLine(),
                                        binExpr.operation.getStartColumn()))
                                break
                            case '==': op = new ConstantExpression('EQUAL');                 break
                            case '!=': op = new ConstantExpression('NOT_EQUAL');             break
                            case '<':  op = new ConstantExpression('LESS_THAN');             break
                            case '<=': op = new ConstantExpression('LESS_THAN_OR_EQUAL');    break
                            case '>':  op = new ConstantExpression('GREATER_THAN');          break
                            case '>=': op = new ConstantExpression('GREATER_THAN_OR_EQUAL'); break
                            case 'in': op = new ConstantExpression('IN');                    break
                        }
                        operation = new PropertyExpression(new ClassExpression(ClassHelper.make(FilterOperator)), op)
                        value = binExpr.rightExpression
                    } else if (clauseCall.arguments.expressions[0] instanceof NotExpression) {
                        // of the form: where !alive

                        column = clauseCall.arguments.expressions[0].expression
                        operation = new PropertyExpression(new ClassExpression(ClassHelper.make(FilterOperator)), new ConstantExpression('EQUAL'))
                        value = ConstantExpression.FALSE
                    } else if (clauseCall.arguments.expressions[0] instanceof VariableExpression) {
                        // of the form: where alive

                        column = clauseCall.arguments.expressions[0]
                        operation = new PropertyExpression(new ClassExpression(ClassHelper.make(FilterOperator)), new ConstantExpression('EQUAL'))
                        value = ConstantExpression.TRUE
                    }

                    clauseCall.arguments.expressions[0] = new ConstructorCallExpression(
                            ClassHelper.make(WhereClause),
                            new TupleExpression(new NamedArgumentListExpression([
                                    new MapEntryExpression(new ConstantExpression('column'), column),
                                    new MapEntryExpression(new ConstantExpression('operation'), operation),
                                    new MapEntryExpression(new ConstantExpression('comparedValue'), value)
                            ]))
                    )
                }

                // transform "from persons as Person" into "from persons, Person"
                if (
                    clauseCall.method instanceof ConstantExpression &&
                    clauseCall.method.value == "from"  &&
                    clauseCall.arguments instanceof ArgumentListExpression &&
                    clauseCall.arguments.expressions.size() == 1 &&
                    clauseCall.arguments.expressions[0] instanceof CastExpression
                ) {
                    CastExpression castExpr = clauseCall.arguments.expressions[0]
                    clauseCall.arguments.expressions[0] = castExpr.expression
                    clauseCall.arguments.expressions[1] = new ClassExpression(castExpr.type)
                }

                // continue the visit
                super.visitMethodCallExpression(clauseCall)
            }

            void visitClosureExpression(ClosureExpression expression) {
                super.visitClosureExpression(expression)
            }
            
            @Override public void visitExpressionStatement(ExpressionStatement statement) {
                if(statement.expression instanceof BinaryExpression){
                    BinaryExpression be = statement.expression
                    if(be.operation.text != '='){
                        addError("Missing where keyword!", statement)                        
                    }
                }
                super.visitExpressionStatement(statement)
            }

            protected SourceUnit getSourceUnit() { source }
        }

        def queryMethodVisitor = new ClassCodeVisitorSupport() {
            void visitMethodCallExpression(MethodCallExpression call) {
                if (isOnDatastore(call) || isOnPogo(call)) {
                    ClosureExpression closureExpr = call.arguments.expressions[0]
                    whereMethodVisitor.visitClosureExpression(closureExpr)
                } else {
                    super.visitMethodCallExpression(call)
                }
            }

            protected SourceUnit getSourceUnit() { source }

            protected boolean isOnDatastore(MethodCallExpression call){
                // 'datastore' variable
                call.objectExpression instanceof VariableExpression && call.objectExpression.variable == 'datastore' &&
                        // 'query' or 'execute' or 'iterate' method
                        call.method instanceof ConstantExpression &&
                        (call.method.value == 'build' || call.method.value == 'query' || call.method.value == 'execute' || call.method.value == 'iterate') &&
                        // closure single argument
                        call.arguments.expressions.size() == 1 && call.arguments.expressions[0] instanceof ClosureExpression
            }

            protected boolean isOnPogo(MethodCallExpression call){
                // 'datastore' variable
                call.objectExpression instanceof ClassExpression && call.objectExpression.type.getAnnotations(ClassHelper.make(Entity).plainNodeReference)  &&
                        // 'query' or 'execute' or 'iterate' method
                        call.method instanceof ConstantExpression && (call.method.value == 'find' || call.method.value == 'findAll' || call.method.value == 'count' || call.method.value == 'iterate') &&
                        // closure single argument
                        call.arguments.expressions.size() == 1 && call.arguments.expressions[0] instanceof ClosureExpression
            }
        }

        source.AST.classes.each { ClassNode cn ->
            queryMethodVisitor.visitClass(cn)
        }
    }
}
