import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime
import org.xml.sax.SAXParseException

def url = "http://groups.google.com/group/gaelyk/feed/rss_v2_0_topics.xml".toURL()

try {
    def slurper = new XmlSlurper()
    def result = slurper.parseText(url.text)

    def sdf = new SDF('EEE, dd MMM yyyy HH:mm:ss z', Locale.US)

    html.ul {
        result.channel.item.each { item ->
            li {
                a href: item.link.text(), item.title.text().trim()
                def prettyTime = new PrettyTime().format(sdf.parse(item.pubDate.text().replace('UT', 'GMT')))
                i "posted ${prettyTime}"
                i "by ${item.author}"
            }
        }
    }
} catch (SAXParseException spe) {
    html.div {
        span "A problem occurred while parsing the Google Group RSS feed, please go directly to the"
        a href: "http://groups.google.com/group/gaelyk", "Gaelyk Google Group"
        span " instead. Or try again later."
    }
} catch (IOException ioe) {
    html.div {
        span "The Google Group feed could not be fetched, please go directly to the"
        a href: "http://groups.google.com/group/gaelyk", "Gaelyk Google Group"
        span " instead. Or try again later."
    }
}
