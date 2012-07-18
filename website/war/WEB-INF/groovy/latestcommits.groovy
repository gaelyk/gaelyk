import java.text.SimpleDateFormat as SDF
import com.ocpsoft.pretty.time.PrettyTime
import groovy.json.JsonSlurper

def url = "https://api.github.com/repos/glaforge/gaelyk/commits".toURL()

def slurper = new JsonSlurper()
def result = slurper.parseText(url.getText('UTF-8'))

def sdf = new SDF("yyyy-MM-dd'T'HH:mm:ssz", Locale.US)

html.ul {
    result.commit.each { commit ->
        li {
            a href: "https://github.com${commit.url}", commit.message
            def time = commit.committer.date.replaceAll(/(-|\+)(\d\d):(\d\d)/, '$1$2$3')
            def prettyTime = new PrettyTime().format(sdf.parse(time))
            i "committed ${prettyTime}"
            i "by ${commit.author.name}"
        }
    }
}
