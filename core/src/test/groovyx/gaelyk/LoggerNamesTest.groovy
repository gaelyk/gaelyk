package groovyx.gaelyk

import groovyx.gaelyk.logging.GroovyLogger

/**
 * @author Guillaume Laforge
 */
class LoggerNamesTest extends GroovyTestCase {
    void testTemplateName() {
        def expected = [
                '/index.gtpl':                  'gaelyk.template.index',
                '/tutorial/chap1.gtpl':         'gaelyk.template.tutorial.chap1',
                '/WEB-INF/pages/index.gtpl':    'gaelyk.template.WEB-INF.pages.index',
                '/WEB-INF/includes/a.gtpl':     'gaelyk.template.WEB-INF.includes.a',
                '/WEB-INF/includes/dir/b.gtpl': 'gaelyk.template.WEB-INF.includes.dir.b'
        ]

        expected.each { String uri, String loggerName ->
            assert GroovyLogger.forTemplateUri(uri).name == loggerName
        }
    }

    void testGroovletName() {
        def expected = [
                '/upload.groovy':                   'gaelyk.groovlet.upload',
                '/WEB-INF/groovy/upload.groovy':    'gaelyk.groovlet.upload',
                '/WEB-INF/groovy/ctrl/edit.groovy': 'gaelyk.groovlet.ctrl.edit'
        ]

        expected.each { String uri, String loggerName ->
            assert GroovyLogger.forGroovletUri(uri).name == loggerName
        }
    }
}
