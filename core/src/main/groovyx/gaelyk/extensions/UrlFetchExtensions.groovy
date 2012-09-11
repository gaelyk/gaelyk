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
import com.google.appengine.api.urlfetch.HTTPResponse
import com.google.appengine.api.urlfetch.HTTPHeader
import com.google.appengine.api.urlfetch.HTTPMethod
import com.google.appengine.api.urlfetch.URLFetchService
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import com.google.appengine.api.urlfetch.FetchOptions
import com.google.appengine.api.urlfetch.HTTPRequest

/**
 * URL Fetch Service extension methods
 *
 * @author Guillaume Laforge
 */
class UrlFetchExtensions {

    /**
     * @return the HTTP status code (synonym of <code>getResponseCode()</code>)
     */
    @CompileStatic
    static int getStatusCode(HTTPResponse response) {
        response.responseCode
    }

    /**
     * @return a convenient Map<String, String> of HTTP Headers from the response
     */
    static Map<String, String> getHeadersMap(HTTPResponse response) {
        response.headers.inject([:]) { Map m, HTTPHeader h -> m[h.name] = h.value; m }
    }

    /**
     * Gets the text of the response.
     *
     * @param response the response
     * @param encoding encoding used (default: 'UTF-8')
     * @return the string representing the response content
     */
    @CompileStatic
    static String getText(HTTPResponse response, String encoding = 'UTF-8') {
        new String(response.content, encoding)
    }

    private static fetch(URL url, HTTPMethod method, Map<String, String> options) {
        URLFetchService urlFetch = URLFetchServiceFactory.URLFetchService
        def fetchOptions = FetchOptions.Builder.withDefaults()

        // specify the fetch options
        options.each { String key, value ->
            switch(key) {
                case 'allowTruncate':
                    if (value)
                        fetchOptions.allowTruncate()
                    else
                        fetchOptions.disallowTruncate()
                    break
                case 'followRedirects':
                    if (value)
                        fetchOptions.followRedirects()
                    else
                        fetchOptions.doNotFollowRedirects()
                    break
                case 'deadline':
                    fetchOptions.deadline = value
                    break
            // bypass the headers, payload, params and async options
                case 'headers':
                case 'payload':
                case 'params':
                case 'async':
                    break
                default:
                    throw new RuntimeException("Unknown fetch option: $key")
            }
        }

        // add params
        if (options.params) {
            def encodedParams = options.params.collect { k, v -> "${URLEncoder.encode(k, 'UTF-8')}=${URLEncoder.encode(v, 'UTF-8')}" }.join('&')
            // if it's a POST method, encode the params as an URL encoded payload
            if (method == HTTPMethod.POST) {
                if (!options.headers) { options.headers = [:] }
                options.headers << ['Content-Type': 'application/x-www-form-urlencoded']
                options.payload = encodedParams
            } else {
                url = new URL("${url.toString()}?${encodedParams}")
            }
        }

        def request = new HTTPRequest(url, method, fetchOptions)

        // add the headers to the request
        if (options.headers) {
            Map headers = options.headers
            headers.each { String key, String value ->
                request.addHeader(new HTTPHeader(key, value))
            }
        }

        // add the payload
        if (options.payload) {
            request.payload = options.payload
        }

        // do an async call, if the async: true option is present
        if (options.async)
            urlFetch.fetchAsync(request)
        else
            urlFetch.fetch(request)
    }

    /**
     * Use the URLFetch Service to do a GET on the URL.
     *
     * @param url URL to GET
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    @CompileStatic
    static get(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.GET, options)
    }

    /**
     * Use the URLFetch Service to do a POST on the URL.
     *
     * @param url URL to POST to
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    @CompileStatic
    static post(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.POST, options)
    }

    /**
     * Use the URLFetch Service to do a PUT on the URL.
     *
     * @param url URL to PUT to
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    @CompileStatic
    static put(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.PUT, options)
    }

    /**
     * Use the URLFetch Service to do a DELETE on the URL.
     *
     * @param url URL to DELETE
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    @CompileStatic
    static delete(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.DELETE, options)
    }

    /**
     * Use the URLFetch Service to do a HEAD on the URL.
     *
     * @param url URL to HEAD
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    @CompileStatic
    static head(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.HEAD, options)
    }
}
