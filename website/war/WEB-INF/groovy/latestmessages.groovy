import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime

def url = "http://groups.google.com/group/gaelyk/feed/rss_v2_0_topics.xml".toURL()

def slurper = new XmlSlurper()
def result = slurper.parseText(url.text)

def sdf = new SDF('EEE, dd MMM yyyy HH:mm:ss z', Locale.US)

html.ul {
	result.channel.item.each { item ->
		li {
			a href: item.link, item.title
			br()
			//br()
			def prettyTime = new PrettyTime().format(sdf.parse(item.pubDate.text().replace('UT', 'GMT')))
			i "posted ${prettyTime}"
            i "by ${item.author}"
		}
	}
}