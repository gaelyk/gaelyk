package groovyx.gaelyk

/**
 * Category methods for time duration handling, for the caching expiration definition
 *
 * @author Guillaume Laforge
 */
class ExpirationTimeCategory {
    /**
     * Category method to support the notation: <code>10.seconds</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    static int getSeconds(Integer self) {
        self
    }

    /**
     * Category method to support the notation: <code>1.second</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    static int getSecond(Integer self) {
        self
    }

    /**
     * Category method to support the notation: <code>10.minutes</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    static int getMinutes(Integer self) {
        self * 60
    }

    /**
     * Category method to support the notation: <code>1.minute</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    static int getMinute(Integer self) {
        self * 60
    }

    /**
     * Category method to support the notation: <code>10.hours</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    static int getHours(Integer self) {
        self * 3600
    }

    /**
     * Category method to support the notation: <code>1.hour</code> in the URL routing configuration file
     * for defining the duration the output of the template or groovlet must stay in the cache
     * @return a number of seconds
     */
    static int getHour(Integer self) {
        self * 3600
    }

}
