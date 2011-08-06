import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime
import groovy.json.JsonSlurper

def content = "http://github.com/api/v2/json/issues/list/glaforge/gaelyk/open".toURL().get().text
def struct = new JsonSlurper().parseText(content)

def sdf = new SDF("yyyy/MM/dd HH:mm:ss Z", Locale.US)

html.ul {
	struct.issues.each { issue ->
		li {
			a href: "http://github.com/glaforge/gaelyk/issues#issue/${issue.number}", issue.title
//			2009/07/20 01:41:25 -0700
			def prettyTime = new PrettyTime().format(sdf.parse(issue.created_at))
			i " opened ${prettyTime}"
            i "by ${issue.user}"
		}
	}
}

