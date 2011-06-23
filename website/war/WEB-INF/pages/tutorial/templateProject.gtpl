<title>The template project</title>

<h1>Introduction</h1>

<p>
The template project, that you can download from the <a href="/download">download page</a>,
follows the project structure outlined in the <a href="/tutorial/setup">setup section</a>.
You will notice it features a <code>build.gradle</code> file for letting you build, test, run and deploy you project.
This build file is using the <a href="http://gradle.org">Gradle</a> build system,
which is using the <b>Groovy</b> language for its build description DSL.
This Gradle build file leverages key plugins that we'll describe below,
and offers integration with the <a href="http://spockframework.org">Spock</a> testing framework for testing your groovlets.
</p>

<blockquote>
<b>Note: </b> If you want to use the Gradle build,
you'll have to <a href="http://gradle.org/installation.html">install Gradle</a> first.
The build file was tested and used with version 1.0-milestone-3 of Gradle.
</b>
</blockquote>

<a name="build"></a>
<h1>Gradle build file</h1>

<p>
The Gradle build file uses the following Gradle plugins:
</p>

<ul>
<li>
    <b>groovy</b>: for compiling the Groovy sources in the <code>src</code> folder
</li>
<li>
    <b>eclipse</b>: for creating/updating the proper project files for Eclipse
</li>
<li>
    <b>idea</b>: for creating/updating the proper project files for IntelliJ IDEA
</li>
<li>
    <b>gae</b>: for interacting with the Google App Engine SDK for running, deploying apps,
    instead of using the SDK command-line directly
</li>
<li>
    <b>gaelyk</b>: for creating views and controllers, taking care of plugins
</li>
</ul>

<p>
You'll find more information on
the <a href="https://github.com/bmuschko/gradle-gae-plugin">gae plugin</a> and
<a href="https://github.com/bmuschko/gradle-gaelyk-plugin">gaelyk plugin</a>
on their respective project pages.
</p>

<h2>In a nutshell</h2>

<p>
You will find the following gradle tasks handy:
</p>

<ul>
    <li>
        <tt>gradle tasks</tt>: to list all the possible tasks which are available
    </li>
    <li>
        <tt>gradle classes</tt>: to compile your Groovy sources in the <code>src/main/groovy</code> folder
        and have them placed in <code>WEB-INF/classes</code>
    </li>
    <li>
        <tt>gradle test</tt>: to compile and run your tests from <code>src/main/test</code>
    </li>
    <li>
        <tt>gradle gaeRun</tt>: to run your application locally
    </li>
    <li>
        <tt>gradle gaeStop</tt>: to stop your locally running application
    </li>
    <li>
        <tt>gradle gaeUpload</tt>: to upload your application to production
    </li>
</ul>

<a name="spock"></a>
<h1>Testing with Spock</h1>

<p>
You can test your groovlets with the <a href="http://spockframework.org">Spock testing framework</a>.
As an example, lets imagine we have the following groovlet, in <code>WEB-INF/groovy/dataStoreGroovlet.groovy</code>,
which inserts some data in the datastore:
</p>

<pre class="brush:groovy">
import com.google.appengine.api.datastore.*

def e = new Entity("person")
e.firstname = 'Bart'
e.lastname = 'Simpson'
e.save()
</pre>

<p>
You could test such a Groovlet with the following Spock test, in <code>src/test/groovy/DatastoreServiceSpec.groovy</code>:
</p>

<pre class="brush:groovy">
import groovyx.gaelyk.spock.*
import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

class DatastoreServiceSpec extends GaelykUnitSpec {

    def setup() {
        groovlet 'dataStoreGroovlet.groovy'
    }

    def "the datastore is used from within the groovlet"() {
        given: "the initialised groovlet is invoked and data is persisted"
        dataStoreGroovlet.get()

        when: "the datastore is queried for data"
        def query = new Query("person")
        query.addFilter("firstname", Query.FilterOperator.EQUAL, "Marco")
        def preparedQuery = datastore.prepare(query)
        def entities = preparedQuery.asList(withLimit(1))

        then: "the persisted data is found in the datastore"
        def person = entities[0]
        person.firstname == 'Bart'
        person.lastname == 'Simpson'
    }
}
</pre>

<p>
Then, you could run the test by executing the gradle command: <code>gradle test</code>.
</p>

<p>
For further information, please have a look at the
<a href="https://github.com/marcoVermeulen/gaelyk-spock">Spock support for Gaelyk</a>.
</p>
