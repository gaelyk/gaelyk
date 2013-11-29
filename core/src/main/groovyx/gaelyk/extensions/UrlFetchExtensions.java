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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * URL Fetch Service extension methods
 *
 * @author Guillaume Laforge
 */
public class UrlFetchExtensions {

    /**
     * @return the HTTP status code (synonym of <code>getResponseCode()</code>)
     */
    public static int getStatusCode(HTTPResponse response) {
        return response.getResponseCode();
    }

    /**
     * @return a convenient Map<String, String> of HTTP Headers from the response
     */
    public static Map<String, String> getHeadersMap(HTTPResponse response) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (HTTPHeader header : response.getHeaders()) {
            headers.put(header.getName(), header.getValue()); 
        }
        return headers;
    }

    /**
     * Gets the text of the response.
     *
     * @param response the response
     * @return the string representing the response content
     */
    public static String getText(HTTPResponse response) {
        try {
            return getText(response, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should never happen
            return null;
        }
    }
    
    /**
     * Gets the text of the response.
     *
     * @param response the response
     * @param encoding encoding used (default: 'UTF-8')
     * @return the string representing the response content
     * @throws UnsupportedEncodingException 
     */
    public static String getText(HTTPResponse response, String encoding) throws UnsupportedEncodingException {
        return new String(response.getContent(), encoding);
    }

    private static Object fetch(URL url, HTTPMethod method, Map<String, Object> options) throws IOException {
        URLFetchService urlFetch = URLFetchServiceFactory.getURLFetchService();
        FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

        // specify the fetch options
        for (Entry<String, Object> entry : options.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            switch(key) {
                case "allowTruncate":
                    if (DefaultGroovyMethods.asBoolean(value))
                        fetchOptions.allowTruncate();
                    else
                        fetchOptions.disallowTruncate();
                    break;
                case "followRedirects":
                    if (DefaultGroovyMethods.asBoolean(value))
                        fetchOptions.followRedirects();
                    else
                        fetchOptions.doNotFollowRedirects();
                    break;
                case "deadline":
                    fetchOptions.setDeadline(((Number)value).doubleValue());
                    break;
            // bypass the headers, payload, params and async options
                case "headers":
                case "payload":
                case "params":
                case "async":
                    break;
                default:
                    throw new RuntimeException("Unknown fetch option: " + key);
            }
        }

        // add params
        if (options.containsKey("params")) {
            String encodedParams = MiscExtensions.toQueryString((Map<?,?>)options.get("params"));
            // if it's a POST method, encode the params as an URL encoded payload
            if (method == HTTPMethod.POST) {
                @SuppressWarnings("unchecked") Map<String, String> headersMap = (Map<String, String>) options.get("headers");
                if (headersMap == null) {
                    headersMap = new LinkedHashMap<>();
                    options.put("headers", headersMap);
                }
                headersMap.put("Content-Type", "application/x-www-form-urlencoded");
                
                options.put("payload", encodedParams.getBytes());
            } else {
                url = new URL(url.toString() + "?" + encodedParams);
            }
        }

        HTTPRequest request = new HTTPRequest(url, method, fetchOptions);

        // add the headers to the request
        if (options.containsKey("headers")) {
            @SuppressWarnings("unchecked") Map<String, String> headers = (Map<String, String>) options.get("headers");
            for (Entry<String, String> e : headers.entrySet()) {
                request.addHeader(new HTTPHeader(e.getKey(), e.getValue()));
            }
        }

        // add the payload
        if (options.containsKey("payload")) {
            request.setPayload((byte[]) options.get("payload"));
        }

        // do an async call, if the async: true option is present
        if (options.containsKey("async") && DefaultGroovyMethods.asBoolean(options.get("async"))) return urlFetch.fetchAsync(request);
        return urlFetch.fetch(request);
    }

    /**
     * Use the URLFetch Service to do a GET on the URL.
     *
     * @param url URL to GET
     * @return an HTTPResponse
     * @throws IOException 
     */
    public static HTTPResponse get(URL url) throws IOException {
        return (HTTPResponse) fetch(url, HTTPMethod.GET, new HashMap<String, Object>());
    }
    
    /**
     * Use the URLFetch Service to do a GET on the URL.
     *
     * @param url URL to GET
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     * @throws IOException 
     */
    public static Object get(URL url, Map<String, Object> options) throws IOException {
        return fetch(url, HTTPMethod.GET, options);
    }

    /**
     * Use the URLFetch Service to do a POST on the URL.
     *
     * @param url URL to POST to
     * @return an HTTPResponse
     * @throws IOException 
     */
    public static HTTPResponse post(URL url) throws IOException {
        return (HTTPResponse) fetch(url, HTTPMethod.POST, new HashMap<String, Object>());
    }
    
    /**
     * Use the URLFetch Service to do a POST on the URL.
     *
     * @param url URL to POST to
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     * @throws IOException 
     */
    public static Object post(URL url, Map<String, Object> options) throws IOException {
        return fetch(url, HTTPMethod.POST, options);
    }

    /**
     * Use the URLFetch Service to do a PUT on the URL.
     *
     * @param url URL to PUT to
     * @return an HTTPResponse
     * @throws IOException 
     */
    public static HTTPResponse put(URL url) throws IOException {
        return (HTTPResponse) fetch(url, HTTPMethod.PUT, new HashMap<String, Object>());
    }
    
    /**
     * Use the URLFetch Service to do a PUT on the URL.
     *
     * @param url URL to PUT to
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     * @throws IOException 
     */
    public static Object put(URL url, Map<String, Object> options) throws IOException {
        return fetch(url, HTTPMethod.PUT, options);
    }

    /**
     * Use the URLFetch Service to do a DELETE on the URL.
     *
     * @param url URL to DELETE
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     * @throws IOException 
     */
    public static HTTPResponse delete(URL url) throws IOException {
        return (HTTPResponse) fetch(url, HTTPMethod.DELETE, new HashMap<String, Object>());
    }

    /**
     * Use the URLFetch Service to do a DELETE on the URL.
     *
     * @param url URL to DELETE
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     * @throws IOException 
     */
    public static Object delete(URL url, Map<String, Object> options) throws IOException {
        return fetch(url, HTTPMethod.DELETE, options);
    }
    
    
    /**
     * Use the URLFetch Service to do a HEAD on the URL.
     *
     * @param url URL to HEAD
     * @return an HTTPResponse
     * @throws Exception 
     */
    public static HTTPResponse head(URL url) throws Exception {
        return (HTTPResponse) fetch(url, HTTPMethod.HEAD, new HashMap<String, Object>());
    }
    
    /**
     * Use the URLFetch Service to do a HEAD on the URL.
     *
     * @param url URL to HEAD
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     * @throws IOException 
     */
    public static Object head(URL url, Map<String, Object> options) throws IOException {
        return fetch(url, HTTPMethod.HEAD, options);
    }
    
}
