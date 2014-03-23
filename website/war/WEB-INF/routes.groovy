def stableDuration = 0
def hotContentDuration = 0

get "/",            forward: "/WEB-INF/pages/index.gtpl",     cache: stableDuration

get "/tutorial",    forward: "/WEB-INF/pages/tutorial.gtpl",  cache: stableDuration

get "/tutorial/print", forward: "/WEB-INF/pages/tutorial/print.gtpl", cache: stableDuration

get "/tutorial/setup",                  forward: "/WEB-INF/pages/tutorial/setup.gtpl",                  cache: stableDuration
get "/tutorial/views-and-controllers",  forward: "/WEB-INF/pages/tutorial/viewsAndControllers.gtpl",    cache: stableDuration
get "/tutorial/url-routing",            forward: "/WEB-INF/pages/tutorial/flexibleUrlRouting.gtpl",     cache: stableDuration
get "/tutorial/app-engine-shortcuts",   forward: "/WEB-INF/pages/tutorial/gaeShortcuts.gtpl",           cache: stableDuration
get "/tutorial/plugins",                forward: "/WEB-INF/pages/tutorial/plugins.gtpl",                cache: stableDuration
get "/tutorial/run-deploy",             forward: "/WEB-INF/pages/tutorial/runDeploy.gtpl",              cache: stableDuration
get "/tutorial/template-project",       forward: "/WEB-INF/pages/tutorial/templateProject.gtpl",        cache: stableDuration

get "/download",    forward: "/WEB-INF/pages/download.gtpl",  cache: hotContentDuration
get "/community",   forward: "/WEB-INF/pages/community.gtpl", cache: hotContentDuration
get "/search",      forward: "/WEB-INF/pages/search.gtpl",    cache: stableDuration
get "/plugins",     forward: "/WEB-INF/pages/plugins.gtpl",   cache: stableDuration
