get "/tutorial",    forward: "/tutorial.gtpl"
get "/download",    forward: "/download.gtpl"
get "/community",   forward: "/community.gtpl"

// for compatibility-sake with older allowed URLs

get "/tutorial/",    forward: "/tutorial.gtpl"
get "/download/",    forward: "/download.gtpl"
get "/community/",   forward: "/community.gtpl"

get "/tutorial/index.gtpl",    forward: "/tutorial.gtpl"
get "/download/index.gtpl",    forward: "/download.gtpl"
get "/community/index.gtpl",   forward: "/community.gtpl"

