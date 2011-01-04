package groovyx.gaelyk.routes

import org.codehaus.groovy.control.CompilerConfiguration

/**
 * 
 * @author Guillaume Laforge
 */
class RoutesBaseScriptTest extends GroovyTestCase {

    void testBaseScriptClass() {
        def config = new CompilerConfiguration()
        config.scriptBaseClass = RoutesBaseScript.class.name

        def shell = new GroovyShell(config)
        RoutesBaseScript script = shell.parse("""\
            get "/foo",       forward:  "/foo.groovy"
            put "/create",    forward:  "/create.groovy"
            post "/upload",   forward:  "/upload.groovy"
            delete "/delete", forward:  "/delete.groovy"
            all "/",          redirect: "/index.html"
            email             to:       "/incomingEmail.groovy"
            jabber            to:       "/incomingJabber.groovy"
        """.stripIndent())

        script.run()

        def routes = script.routes
        assert routes.size() == 7

    }
}
