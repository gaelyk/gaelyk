import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime

def url = "http://github.com/api/v2/xml/commits/list/glaforge/gaelyk/master".toURL()

def slurper = new XmlSlurper()
def result = slurper.parseText(url.text)

html.ul {
	result.commit.each { commit ->
		li {
			a href: commit.url, commit.message
			br()
			i "by ${commit.author.name}" 
			br()
			def sdf = new SDF("yyyy-MM-dd'T'HH:mm:ssz", Locale.US)
			def time = commit.'committed-date'.text().replaceAll(/(-|\+)(\d\d):(\d\d)/, '$1$2$3')
			def prettyTime = new PrettyTime().format(sdf.parse(time))
			i "committed ${prettyTime}"
		}
	}	
}
