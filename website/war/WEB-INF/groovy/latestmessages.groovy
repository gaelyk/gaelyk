import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime

def url = "http://groups.google.com/group/gaelyk/feed/rss_v2_0_msgs.xml".toURL()

//println url.text

def slurper = new XmlSlurper()
def result = slurper.parseText(url.text)

html.ul {
	result.channel.item.each { item ->
		li {
			a href: item.link, item.title
			br()
			i "by ${item.author}" 
			br()
			def sdf = new SDF('EEE, dd MMM yyyy HH:mm:ss z', Locale.US)
			def prettyTime = new PrettyTime().format(sdf.parse(item.pubDate.text().replace('UT', 'GMT')))
			i "posted ${prettyTime}"
		}
	}
}