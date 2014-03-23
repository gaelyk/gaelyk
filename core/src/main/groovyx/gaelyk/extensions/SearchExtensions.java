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
package groovyx.gaelyk.extensions;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.Script;
import groovyx.gaelyk.RetryingFuture;
import groovyx.gaelyk.search.DocumentDefinitions;
import groovyx.gaelyk.search.QueryBuilder;
import groovyx.gaelyk.search.SearchQueryStringCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;

/**
 * Search service method extensions
 *
 * @author Guillaume Laforge
 */
class SearchExtensions {

    /**
     * Get a search service instance restricted to a given namespace.
     * <pre><code>
     *     search['namespaceName']
     * </code></pre>
     *
     * @param search the search service
     * @param namespace the namespace name
     * @return a search service over a specific namespace
     */
    public static SearchService getAt(SearchService search, String namespace) {
        return SearchServiceFactory.getSearchService(namespace);
    }

    /**
     * Shortcut notation to easily get an index from the search service.
     * <pre><code>
     *     def index = search.index("books")
     * </code></pre>
     *
     * @param search the search service
     * @param indexName the name of the index
     * @return an index
     */
    public static Index index(SearchService search, String indexName) {
        return search.getIndex(IndexSpec.newBuilder().setName(indexName).build());
    }

    /**
     * Add a new document to the index.
     *
     * <pre><code>
     *     index.add {
     *         document(id: "1234", locale: US, rank: 3) {
     *             title text: "Big bad wolf", locale: ENGLISH
     *             published date: new Date()
     *             numberOfCopies number: 35
     *             summary html: "<p>super story</p>", locale: ENGLISH
     *             description text: "a book for children"
     *             category atom: "children"
     *             keyword text: "wolf"
     *             keyword text: "red hook"
     *             location geoPoint: new GeoPoint(15,50)
     *         }
     *     }
     * </code></pre>
     *
     * The named arguments are restricted to id, locale and rank.
     * The calls inside the closure correspond to the field name, its type thanks to a named argument
     * of the form <code>type: value</code>, and optionally a locale.
     * You can have several times the same field name, for multi-valued fields.
     *
     * @param index the index to which to add the documents
     * @param closure the closure defining the documents to be added to the index
     * @return an instance of PutResponse
     * @deprecated use {@link #put(Index, Closure)} instead
     */
    @Deprecated
    public static PutResponse add(Index index, @DelegatesTo(value=DocumentDefinitions.class, strategy=Closure.DELEGATE_FIRST) Closure<?> closure) {
        return put(index, closure);
    }

    /**
     * Put a new document to the index.
     *
     * <pre><code>
     *     index.put {
     *         document(id: "1234", locale: US, rank: 3) {
     *             title text: "Big bad wolf", locale: ENGLISH
     *             published date: new Date()
     *             numberOfCopies number: 35
     *             summary html: "<p>super story</p>", locale: ENGLISH
     *             description text: "a book for children"
     *             category atom: "children"
     *             keyword text: "wolf"
     *             keyword text: "red hook"
     *             location geoPoint: new GeoPoint(15,50)
     *         }
     *     }
     * </code></pre>
     *
     * The named arguments are restricted to id, locale and rank.
     * The calls inside the closure correspond to the field name, its type thanks to a named argument
     * of the form <code>type: value</code>, and optionally a locale.
     * You can have several times the same field name, for multi-valued fields.
     *
     * @param index the index to which to put the documents
     * @param closure the closure defining the documents to be added to the index
     * @return an instance of PutResponse
     */
    public static PutResponse put(Index index, @DelegatesTo(value=DocumentDefinitions.class, strategy=Closure.DELEGATE_FIRST) Closure<?> closure) {
        return index.put(applyDefinitionsClosure(closure).getDocs());
    }
    
    private static DocumentDefinitions applyDefinitionsClosure(@DelegatesTo(value=DocumentDefinitions.class, strategy=Closure.DELEGATE_FIRST) Closure<?> closure) {
        Closure<?> docDefClosure = (Closure<?>)closure.clone();
        docDefClosure.setResolveStrategy(Closure.DELEGATE_FIRST);
        DocumentDefinitions definitions = new DocumentDefinitions();
        docDefClosure.setDelegate(definitions);
        docDefClosure.call();
        return definitions;
    }

    /**
     * Put a new document to the index.
     *
     * <pre><code>
     *     index.putAsync {
     *         document(id: "1234", locale: US, rank: 3) {
     *             title text: "Big bad wolf", locale: ENGLISH
     *             published date: new Date()
     *             numberOfCopies number: 35
     *             summary html: "<p>super story</p>", locale: ENGLISH
     *             description text: "a book for children"
     *             category atom: "children"
     *             keyword text: "wolf"
     *             keyword text: "red hook"
     *             location geoPoint: new GeoPoint(15,50)
     *         }
     *     }
     * </code></pre>
     *
     * The named arguments are restricted to id, locale and rank.
     * The calls inside the closure correspond to the field name, its type thanks to a named argument
     * of the form <code>type: value</code>, and optionally a locale.
     * You can have several times the same field name, for multi-valued fields.
     *
     * @param index the index to which to put the documents
     * @param closure the closure defining the documents to be added to the index
     * @return an instance of PutResponse
     */
    public static Future<PutResponse> putAsync(Index index, @DelegatesTo(value=DocumentDefinitions.class, strategy=Closure.DELEGATE_FIRST) Closure<?> closure) {
        return index.putAsync(applyDefinitionsClosure(closure).getDocs());
    }

    /**
     * Get a document field raw value or list of raw values.
     * <pre><code>
     *      assert document.title = "Big bad wolf"
     *      assert document.keyword[0] == "wolf"
     *      assert document.keyword[1] == "red hook"
     * </code></pre>
     *
     * @param document the document
     * @param fieldName the field name
     * @return a raw value or a list of raw values if the field is multivalued
     */
    public static Object get(Document document, String fieldName) {
        @SuppressWarnings("unchecked") List<Field> fields = (List<Field>) DefaultGroovyMethods.collect(document.getFields(fieldName));

        switch (fields.size()) {
            case 0:
                if(document instanceof ScoredDocument){
                    List<Object> exps = new ArrayList<Object>();
                    for (Field f : ((ScoredDocument) document).getExpressions()) {
                        if (f.getName().equals(fieldName)) {
                            exps.add(getFieldRawValue(f));
                        }
                    }
                            
                    if(exps.size() == 0){
                        return null;
                    }
                    if(exps.size() == 1){
                        return exps.get(0);
                    }
                    return exps;
                }
                return null;
            case 1:
                return getFieldRawValue(fields.get(0));
            default:
                List<Object> exps = new ArrayList<Object>();
                for (Field f : fields) {
                    exps.add(getFieldRawValue(f));
                }
                return exps;
        }
    }
    
    public static Future<Results<ScoredDocument>> searchAsync(final Index index, final String query, int retries){
       return RetryingFuture.retry(retries, new Callable< Future<Results<ScoredDocument>>>() {
           @Override public  Future<Results<ScoredDocument>> call() throws Exception {
            return index.searchAsync(query);
        }
       }); 
    }
    
    
    public static Future<Results<ScoredDocument>> searchAsync(final Index index, final Query query, int retries){
        return RetryingFuture.retry(retries, new Callable< Future<Results<ScoredDocument>>>() {
            @Override public  Future<Results<ScoredDocument>> call() throws Exception {
             return index.searchAsync(query);
         }
        });
    }
    
    public static QueryBuilder prepare(SearchService service, final @DelegatesTo(value=QueryBuilder.class, strategy=Closure.DELEGATE_FIRST) Closure<?> c){
        final QueryBuilder builder = new QueryBuilder(c.getThisObject() instanceof Script ? ((Script)c.getThisObject()).getBinding() : new Binding());
        
        GroovyCategorySupport.use(SearchQueryStringCategory.class,new Closure<Object>(builder) {
            public Object call(Object... args) {
                return DefaultGroovyMethods.with(builder, c);                
            };
        });
        

        
        if (builder.getIndexName() == null) { throw new IllegalStateException("Index name cannot be null");};
        if (builder.getQueryString() == null) { throw new IllegalStateException("Query String name cannot be null");};
        
        return builder;
    }
    
    public static Results<ScoredDocument> search(SearchService service, @DelegatesTo(value=QueryBuilder.class, strategy=Closure.DELEGATE_FIRST) Closure<?> c){
        QueryBuilder builder = prepare(service,c);
        Query query = builder.build();
        return index(service, builder.getIndexName()).search(query);
    }
    
    public static Results<ScoredDocument> search(SearchService service, int retries, @DelegatesTo(value=QueryBuilder.class, strategy=Closure.DELEGATE_FIRST) Closure<?> c) throws InterruptedException, ExecutionException{
        QueryBuilder builder = prepare(service,c);
        Query query = builder.build();
        return searchAsync(index(service, builder.getIndexName()), query, retries).get();
    }
    
    public static Future<Results<ScoredDocument>> searchAsync(SearchService service, @DelegatesTo(value=QueryBuilder.class, strategy=Closure.DELEGATE_FIRST) Closure<?> c){
        QueryBuilder builder = prepare(service,c);
        Query query = builder.build();
        return index(service, builder.getIndexName()).searchAsync(query);
    }
    
    public static Future<Results<ScoredDocument>> searchAsync(SearchService service, int retries, @DelegatesTo(value=QueryBuilder.class, strategy=Closure.DELEGATE_FIRST) Closure<?> c){
        QueryBuilder builder = prepare(service,c);
        Query query = builder.build();
        return searchAsync(index(service, builder.getIndexName()), query, retries);
    }
    
    private static Object getFieldRawValue(Field field) {
        switch(field.getType()) {
            case ATOM:      return field.getAtom();
            case DATE:      return field.getDate();
            case HTML:      return field.getHTML();
            case NUMBER:    return field.getNumber();
            case TEXT:      return field.getText();
            case GEO_POINT: return field.getGeoPoint();
        }
        return null;
    }
}
