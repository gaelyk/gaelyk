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

/**
 * Extension methods for time duration handling, for the caching expiration definition
 *
 * @author Guillaume Laforge, Vladimir Orany
 */
public class ExpirationTimeExtensionMethods {
    /**
     * Method to support the notation: <code>10.seconds</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    public static int getSeconds(Integer self) {
        return self;
    }

    /**
     * Method to support the notation: <code>1.second</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    public static int getSecond(Integer self) {
        return self;
    }

    /**
     * Method to support the notation: <code>10.minutes</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    public static int getMinutes(Integer self) {
        return self * 60;
    }

    /**
     * Method to support the notation: <code>1.minute</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    public static int getMinute(Integer self) {
        return self * 60;
    }

    /**
     * Method to support the notation: <code>10.hours</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    public static int getHours(Integer self) {
        return self * 3600;
    }

    /**
     * Method to support the notation: <code>1.hour</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    public static int getHour(Integer self) {
        return self * 3600;
    }

}
