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
package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import javax.servlet.http.HttpServletResponse

/**
 * Extension methods related to the servlet APIs
 *
 * @author Guillaume Laforge
 */
class ServletExtensions {
    /**
     * Adds a fake <code>getHeaders()</code> method to <code>HttpServletResponse</code>.
     * It allows the similar subscript notation syntax of request,
     * but for setting or overriding a header on the response
     * (ie. calling <code>response.setHeader()</code>).
     * It also allows the leftShift notation for adding a header to the response
     * (ie. calling <code>response.addHeader()</code>.
     *
     * <pre><code>
     *  // sets or overrides the header 'a'
     *  response.headers['a'] == 'b'
     *
     *  // adds an additional value to an existing header
     *  // or sets a first value for a non-existant header
     *  response.headers['a'] << 'b'
     * </code></pre>
     *
     * @param response
     * @return a custom map on which you can use the subscript notation to add headers
     */
    @CompileStatic
    static Map getHeaders(HttpServletResponse response) {
        new HashMap() {
            Object put(Object k, Object v) {
                def vString = v.toString()
                response.setHeader(k.toString(), vString)
                return vString
            }

            Object get(Object k) {
                [leftShift: {
                    def vString = it.toString()
                    response.addHeader(k.toString(), vString)
                    return vString }]
            }
        }
    }
}
