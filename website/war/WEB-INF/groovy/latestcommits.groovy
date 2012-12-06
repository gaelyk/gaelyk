import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime
import groovy.json.JsonSlurper

def url = "https://api.github.com/repos/gaelyk/gaelyk/commits".toURL()

def slurper = new JsonSlurper()
def results = slurper.parseText(url.getText('UTF-8'))

def sdf = new SDF("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

html.ul {
    results.each { result ->
        li {
            a href: "http://github.com/gaelyk/gaelyk/commit/${result.sha}", result.commit.message
            def time = result.commit.committer.date.replaceAll(/(-|\+)(\d\d):(\d\d)/, '$1$2$3')
            def prettyTime = new PrettyTime().format(sdf.parse(time))
            i "committed ${prettyTime}"
            i "by ${result.commit.author.name}"
        }
    }
}
