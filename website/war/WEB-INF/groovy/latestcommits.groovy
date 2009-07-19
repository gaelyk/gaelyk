def url = "http://github.com/api/v1/xml/glaforge/gaelyk/commits/master".toURL()

def slurper = new XmlSlurper()
def result = slurper.parseText(url.text)

html.ul {
	result.commit.each { commit ->
		li {
			a href: commit.url, commit.message
			span " by ${commit.author.name}"
		}
	}	
}
