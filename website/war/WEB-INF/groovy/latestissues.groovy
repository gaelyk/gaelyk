import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime

def url = "http://github.com/api/v2/json/issues/list/glaforge/gaelyk/open".toURL()

/*
// potentially restore this when the XML flavor of the Issues API is working again
def slurper = new XmlSlurper()
def result = slurper.parseText(url.text)
*/

// transform JSON into a valid list/map Groovy construct
def struct = evaluate(url.text.replaceAll(/}/, ']').replaceAll(/\{/, '[').replaceAll(/\\\//, /\/\//))

def sdf = new SDF("yyyy/MM/dd HH:mm:ss Z", Locale.US)

html.ul {
	struct.issues.each { issue ->
		li {
			a href: "http://github.com/glaforge/gaelyk/issues#issue/${issue.number}", issue.title
			br()
//			2009/07/20 01:41:25 -0700
			def prettyTime = new PrettyTime().format(sdf.parse(issue.created_at))
			i " opened ${prettyTime}"
            i "by ${issue.user}"
		}
	}
}

