import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime

def url = "http://github.com/api/v2/xml/commits/list/glaforge/gaelyk/master".toURL()

def slurper = new XmlSlurper()
def result = slurper.parseText(url.text)

def sdf = new SDF("yyyy-MM-dd'T'HH:mm:ssz", Locale.US)

html.ul {
	result.commit.each { commit ->
		li {
			a href: "https://github.com${commit.url}", commit.message
			def time = commit.'committed-date'.text().replaceAll(/(-|\+)(\d\d):(\d\d)/, '$1$2$3')
			def prettyTime = new PrettyTime().format(sdf.parse(time))
			i "committed ${prettyTime}"
            i "by ${commit.author.name}"
		}
	}	
}
