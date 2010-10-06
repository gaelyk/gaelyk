html.ul {
    [
            "/",
            "/tutorial",
            "/tutorial/setup",
            "/tutorial/views-and-controllers",
            "/tutorial/url-routing",
            "/tutorial/app-engine-shortcuts",
            "/tutorial/plugins",
            "/tutorial/run-deploy",
            "/download",
            "/community"
    ].each {
        memcache.clearCacheForUri it
        li "Cleared cache for URI: $it"
    }
}
