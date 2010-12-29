package groovyx.gaelyk.logging

/**
 * 
 * @author Guillaume Laforge
 */
class LoggingTest extends GroovyTestCase {

    void testLoggerAccessor() {
        def logger = new LoggerAccessor()

        GroovyLogger propertyCreatedLogger = logger.myLogger
        assert propertyCreatedLogger.name == 'myLogger'

        GroovyLogger getAtCreatedLogger = logger['otherLogger']
        assert getAtCreatedLogger.name == 'otherLogger'
    }

    void testLoggerNameForGroovletsAndTemplates() {
        GroovyLogger groovletLogger = GroovyLogger.forGroovletUri('/media/upload.groovy')
        assert groovletLogger.name == 'gaelyk.groovlet.media.upload'

        GroovyLogger templateLogger = GroovyLogger.forTemplateUri('/pages/uploadSuccess.gtpl')
        assert  templateLogger.name == 'gaelyk.template.pages.uploadSuccess'
    }

    void testLogLevels() {
        GroovyLogger groovletLogger = GroovyLogger.forGroovletUri('/media/upload.groovy')
        GroovyLogger nonGroovletOrTemplateLogger = new GroovyLogger('foo')

        [groovletLogger, nonGroovletOrTemplateLogger].each {
            it.severe "severe"
            it.warning "warning"
            it.info "info"
            it.config "config"
            it.fine "fine"
            it.finer "finer"
            it.finest "finest"
        }
    }
}
