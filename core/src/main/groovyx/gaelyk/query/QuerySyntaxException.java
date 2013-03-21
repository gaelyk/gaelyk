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
package groovyx.gaelyk.query;


/**
 * Exception thrown when there is a syntax problem in your datastore queries using the datastore query DSL.
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
public class QuerySyntaxException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public QuerySyntaxException() {
        super();
    }

    public QuerySyntaxException(String message) {
        super(message);
    }

    public QuerySyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuerySyntaxException(Throwable cause) {
        super(cause);
    }

}
