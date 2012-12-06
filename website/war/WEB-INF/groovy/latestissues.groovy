import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime
import groovy.json.JsonSlurper

def content = "https://api.github.com/repos/gaelyk/gaelyk/issues".toURL().get().text
def struct = new JsonSlurper().parseText(content)

def sdf = new SDF("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

html.ul {
    struct.each { issue ->
        li {
            a href: issue.html_url, issue.title
//          2009/07/20 01:41:25 -0700
            def prettyTime = new PrettyTime().format(sdf.parse(issue.created_at))
            i " opened ${prettyTime}"
            i "by ${issue.user.login}"
        }
    }
}

