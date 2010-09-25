get "/",            forward: "/index.gtpl",     cache: 1.hour

get "/tutorial",    forward: "/tutorial.gtpl",  cache: 1.hour
get "/download",    forward: "/download.gtpl",  cache: 1.hour
get "/community",   forward: "/community.gtpl", cache: 1.hour

// bypass all admin urls

all "/_ah/**", ignore: true

// for compatibility-sake with older allowed URLs

get "/tutorial/",    forward: "/tutorial.gtpl",     cache: 1.hour
get "/download/",    forward: "/download.gtpl",     cache: 1.hour
get "/community/",   forward: "/community.gtpl",    cache: 1.hour

get "/tutorial/index.gtpl",    forward: "/tutorial.gtpl",   cache: 1.hour
get "/download/index.gtpl",    forward: "/download.gtpl",   cache: 1.hour
get "/community/index.gtpl",   forward: "/community.gtpl",  cache: 1.hour

