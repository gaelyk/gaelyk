<title>The template project</title>

<h1>Introduction</h1>

<p>
The template project, that you can download from the <a href="/download">download page</a>,
follows the project structure outlined in the <a href="/tutorial/setup">setup section</a>.
You will notice it features a <code>build.gradle</code> file for letting you build, test, run and deploy you project.
This build file is using the <a href="http://gradle.org">Gradle</a> build system,
which is using the <b>Groovy</b> language for its build description DSL.
This Gradle build file leverages key plugins that we'll describe below,
and offers integration with the <a href="http://spockframework.org">Spock</a> testing framework for testing your groovlets,
and with <a href="http://www.gebish.org/">Geb</a> for your functional tests.
</p>

<blockquote>
<b>Note: </b> The template project conveniently provides the
<a href="http://gradle.org/current/docs/userguide/gradle_wrapper.html">Gradle wrapper</a> to run your code without
having to install the Gradle runtime. You do not have to provide a Google App Engine SDK in your environment. It will
automatically be downloaded by the build script.
</b>
</blockquote>

<a name="build"></a>
<h1>Gradle build file</h1>

<p>
The Gradle build file uses the following Gradle plugins:
</p>

<ul>
<li>
    <b>java</b>: for compiling the Java sources in the <code>src/main/java</code> folder
</li>
<li>
    <b>groovy</b>: for compiling the Groovy sources in the <code>src/main/groovy</code> folder
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
on their respective project pages. The pages describe all available configuration options and tasks in detail.
</p>

<h2>In a nutshell</h2>

<p>
You will find the following Gradle tasks handy:
</p>

<ul>
    <li>
        <tt>gradlew tasks</tt>: to list all the possible tasks which are available
    </li>
    <li>
        <tt>gradlew classes</tt>: to compile your Java/Groovy sources in the folders <code>src/main/java</code> and
        <code>src/main/groovy</code> and have them placed in <code>WEB-INF/classes</code>
    </li>
    <li>
        <tt>gradlew test</tt>: to compile and run your tests from <code>src/test/java</code> and <code>src/test/groovy</code>
    </li>
    <li>
        <tt>gradlew gaeFunctionalTest</tt>: to run the Spock and Geb-powered functional tests from <code>src/functionalTest/groovy</code>
    </li>
    <li>
        <tt>gradlew gaeRun</tt>: to run your application locally
    </li>
    <li>
        <tt>gradlew gaeStop</tt>: to stop your locally running application
    </li>
    <li>
        <tt>gradlew gaeUpload</tt>: to upload your application to production
    </li>
    <li>
        <tt>gradlew gaelykInstallPlugin</tt>: to install a plugin provided by the
        <a href="http://www.gradle.org/gradle_command_line.html">command line property (<code>-P</code>)</a> <code>plugin</code>
    </li>
    <li>
        <tt>gradlew gaelykUninstallPlugin</tt>: to uninstall a plugin provided by the
        <a href="http://www.gradle.org/gradle_command_line.html">command line property (<code>-P</code>)</a> <code>plugin</code>
    </li>
    <li>
        <tt>gradlew gaelykListInstalledPlugins</tt>: to show the installed plugins
    </li>
    <li>
        <tt>gradlew gaelykCreateController&lt;ControllerName&gt;</tt>: to create a Groovlet with the specified name
    </li>
    <li>
        <tt>gradlew gaelykCreateView&lt;ViewName&gt;</tt>: to create a Groovy template with the specified name
    </li>
    <li>
        <tt>gradlew cleanEclipse eclipse</tt>: to generate Eclipse project files
    </li>
    <li>
        <tt>gradlew cleanIdea idea</tt>: to generate IntelliJ project files
    </li>
</ul>

<blockquote>
<b>Remark:</b> Note that the <a href="https://github.com/bmuschko/gradle-gaelyk-plugin">Gradle Gaelyk plugin</a>
takes care of precompiling your Groovlets and Templates before upload to production, for faster startup times.
The <code>gaelykPrecompileGroovlet</code> and <code>gaelykPrecompileTemplate</code>
are called automatically and transparently during the build.
</blockquote>

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
Then, you could run the test by executing the gradle command: <code>gradlew test</code>.
</p>

<p>
For further information, please have a look at the
<a href="https://github.com/marcoVermeulen/gaelyk-spock">Spock support for Gaelyk</a>.
</p>

<a name="geb"></a>
<h1>Functional tests with Geb</h1>

<p>
With <a href="http://www.gebish.org/">Geb</a>, you can add functional tests to your <b>Gaelyk</b> application.
You'll need to instruct Geb the address of your local dev server, in <code>src/functionalTest/groovy/GebConfig.groovy</code>:
</p>

<pre class="brush:groovy">
baseUrl = 'http://localhost:8080/'
</pre>

<p>
Then you can create your first smoke test as follows in <code>src/functionalTest/groovy/SmokeSpec.groovy</code>:
</p>

<pre class="brush:groovy">
import geb.spock.GebSpec

class SmokeSpec extends GebSpec {
    void "main page title should be 'Gaelyk'"() {
        when:
        go ''

        then:
        title == 'Gaelyk'
    }
}
</pre>

<p>
Then you can run the functional tests with:
</p>

<p>
For more information, please have a look at this quick
<a href="http://blog.proxerd.pl/article/funcational-testing-of-gae-lyk-applications-with-geb">tutorial</a>,
and <a href="http://www.gebish.org/">learn more about Geb</a>.
</p>