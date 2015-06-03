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
package groovyx.gaelyk

import com.google.appengine.api.LifecycleManager
import com.google.appengine.api.blobstore.BlobstoreService
import com.google.appengine.api.blobstore.BlobstoreServiceFactory
import com.google.appengine.api.capabilities.CapabilitiesService
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory
import com.google.appengine.api.channel.ChannelService
import com.google.appengine.api.channel.ChannelServiceFactory
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.mail.MailService
import com.google.appengine.api.mail.MailServiceFactory
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory
import com.google.appengine.api.oauth.OAuthService
import com.google.appengine.api.oauth.OAuthServiceFactory
import com.google.appengine.api.taskqueue.Queue
import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.urlfetch.URLFetchService
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import com.google.appengine.api.users.User
import com.google.appengine.api.users.UserService
import com.google.appengine.api.users.UserServiceFactory
import com.google.appengine.api.xmpp.XMPPService
import com.google.appengine.api.xmpp.XMPPServiceFactory
import groovy.transform.CompileStatic
import groovyx.gaelyk.logging.LoggerAccessor
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import java.lang.reflect.Modifier
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService
import com.google.appengine.api.log.LogService
import com.google.appengine.api.log.LogServiceFactory
import com.google.appengine.api.search.SearchService
import com.google.appengine.api.search.SearchServiceFactory

/**
 * This Groovy AST Transformation is a local transformation which is triggered by the Groovy compiler
 * when developers annotate their classes with the <code>@GaelykBindings</code> annotation.
 * The transformation will inject the various variables and services usually injected in Groovlets and templates.
 *
 * @author Vladimir Orany
 * @author Guillaume Laforge
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@CompileStatic
class GaelykBindingsTransformation implements ASTTransformation {

    void visit(ASTNode[] nodes, SourceUnit unit) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof ClassNode)) {
            println "Internal error: expecting [AnnotationNode, ClassNode] but got: ${Arrays.asList(nodes)}"
        }

        ClassNode parent = (ClassNode) nodes[1]

        addGetterIfNotExists(parent, DatastoreService,         "getDatastore",         DatastoreServiceFactory,    "getDatastoreService")
        addGetterIfNotExists(parent, MemcacheService,          "getMemcache",          MemcacheServiceFactory,     "getMemcacheService")
        addGetterIfNotExists(parent, URLFetchService,          "getUrlFetch",          URLFetchServiceFactory,     "getURLFetchService")
        addGetterIfNotExists(parent, MailService,              "getMail",              MailServiceFactory,         "getMailService")
        addGetterIfNotExists(parent, ImagesServiceWrapper,     "getImages",            ImagesServiceWrapper,       "getInstance")
        addGetterIfNotExists(parent, UserService,              "getUsers",             UserServiceFactory,         "getUserService")
        addGetterIfNotExists(parent, Queue,                    "getDefaultQueue",      QueueFactory,               "getDefaultQueue")
        addGetterIfNotExists(parent, XMPPService,              "getXmpp",              XMPPServiceFactory,         "getXMPPService")
        addGetterIfNotExists(parent, BlobstoreService,         "getBlobstore",         BlobstoreServiceFactory,    "getBlobstoreService")
        addGetterIfNotExists(parent, OAuthService,             "getOauth",             OAuthServiceFactory,        "getOAuthService")
        addGetterIfNotExists(parent, CapabilitiesService,      "getCapabilities",      CapabilitiesServiceFactory, "getCapabilitiesService")
        addGetterIfNotExists(parent, ChannelService,           "getChannel",           ChannelServiceFactory,      "getChannelService")
        addGetterIfNotExists(parent, LifecycleManager,         "getLifecycle",         LifecycleManager,           "getInstance")
        addGetterIfNotExists(parent, ProspectiveSearchService, "getProspectiveSearch", ProspectiveSearchService,   "getProspectiveSearchService")
        addGetterIfNotExists(parent, User,                     "getUser",              GaelykBindingEnhancer,      "getCurrentUser")
        addGetterIfNotExists(parent, QueueAccessor,            "getQueues",            GaelykBindingEnhancer,      "getQueues")
        addGetterIfNotExists(parent, Boolean,                  "getLocalMode",         GaelykBindingEnhancer,      "getLocalMode")
        addGetterIfNotExists(parent, Map,                      "getApp",               GaelykBindingEnhancer,      "getApp")
        addGetterIfNotExists(parent, LoggerAccessor,           "getLogger",            GaelykBindingEnhancer,      "getLogger")
        addGetterIfNotExists(parent, Class,                    "getNamespace",         GaelykBindingEnhancer,      "getNamespaceManager")
        addGetterIfNotExists(parent, LogService,               "getLogService",        LogServiceFactory,          "getLogService")
        addGetterIfNotExists(parent, SearchService,            "getSearchService",     SearchServiceFactory,       "getSearchService")
    }

    private void addGetterIfNotExists(ClassNode parent, Class serviceClass, String getterName, Class factoryClass, String factoryMethodName) {
        if(!parent.getGetterMethod(getterName)) {
            parent.addMethod makeServiceGetter(serviceClass, getterName, factoryClass, factoryMethodName)
        }
    }

    private MethodNode makeServiceGetter(Class serviceClass, String accessorName, Class factoryClass, String factoryMethodName) {
        def returnType  = ClassHelper.make(serviceClass).plainNodeReference
        def factoryType = ClassHelper.make(factoryClass).plainNodeReference

        BlockStatement block = new BlockStatement()
        block.addStatement(new ReturnStatement(new MethodCallExpression(
                new ClassExpression(factoryType), factoryMethodName, new TupleExpression()
        )))
        
        new MethodNode(
                accessorName, 
                Modifier.PRIVATE | Modifier.STATIC, 
                returnType, 
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                block
        )
    }
}
