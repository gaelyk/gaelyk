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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Extension methods related to the servlet APIs.
 * 
 * @author Guillaume Laforge
 */
public class ServletExtensions {
    /**
     * Adds a fake <code>getHeaders()</code> method to <code>HttpServletResponse</code>.
     * It allows the similar subscript notation syntax of request,
     * but for setting or overriding a header on the response
     * (ie. calling <code>response.setHeader()</code>).
     * It also allows the leftShift notation for adding a header to the response
     * (ie. calling <code>response.addHeader()</code>.
     * 
     * <pre>
     * <code>
     *  // sets or overrides the header 'a'
     *  response.headers['a'] == 'b'
     * 
     *  // adds an additional value to an existing header
     *  // or sets a first value for a non-existant header
     *  response.headers['a'] << 'b'
     * </code>
     * </pre>
     * 
     * @param response
     * @return a custom map on which you can use the subscript notation to add headers
     */
    public static Map<Object, Object> getHeaders(final HttpServletResponse response) {
        return new HashMap<Object, Object>() {
            private static final long serialVersionUID = 1L;

            public String put(Object k, Object v) {
                String vString = v.toString();
                response.setHeader(k.toString(), vString);
                return vString;
            }

            public Object get(Object k) {
                return new ResponseHeaderAdder(response, k);
            }
        };
    }

    /**
     * Object allowing to add headers to the given response.
     * 
     * @author Vladimir Orany
     */
    public static final class ResponseHeaderAdder {
        private final HttpServletResponse response;
        private final Object              key;

        /**
         * Creates new object allowing to add headers to the given response.
         * @param response where will be new headers added
         * @param key      name of the header
         */
        public ResponseHeaderAdder(HttpServletResponse response, Object key) {
            this.response = response;
            this.key = key;
        }

        public String leftShift(Object value) {
            response.addHeader(key.toString(), value.toString());
            return value.toString();
        }
    }
}
