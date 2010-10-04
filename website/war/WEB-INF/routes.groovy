get "/",            forward: "/WEB-INF/pages/index.gtpl",     cache: 1.hour

get "/tutorial",    forward: "/WEB-INF/pages/tutorial.gtpl",  cache: 1.hour

get "/tutorial/setup",                  forward: "/WEB-INF/pages/tutorial/setup.gtpl",                  cache: 1.hour
get "/tutorial/views-and-controllers",  forward: "/WEB-INF/pages/tutorial/viewsAndControllers.gtpl",    cache: 1.hour
get "/tutorial/url-routing",            forward: "/WEB-INF/pages/tutorial/flexibleUrlRouting.gtpl"//,     cache: 1.hour
get "/tutorial/app-engine-shortcuts",   forward: "/WEB-INF/pages/tutorial/gaeShortcuts.gtpl",           cache: 1.hour
get "/tutorial/plugins",                forward: "/WEB-INF/pages/tutorial/plugins.gtpl",                cache: 1.hour
get "/tutorial/run-deploy",             forward: "/WEB-INF/pages/tutorial/runDeploy.gtpl",              cache: 1.hour

get "/download",    forward: "/WEB-INF/pages/download.gtpl"//,  cache: 1.hour
get "/community",   forward: "/WEB-INF/pages/community.gtpl", cache: 1.hour

