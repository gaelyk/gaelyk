package groovyx.gaelyk.logging

import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.Level

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

        [groovletLogger, nonGroovletOrTemplateLogger].each { GroovyLogger logger ->
            def oldLevel = logger.level

            logger.level = Level.FINEST

            def result = new StringBuilder()
            logger.addHandler new Handler() {
                void close() { }
                void flush() { }

                void publish(LogRecord logRecord) {
                    result << "($logRecord.level)$logRecord.message"
                }
            }

            logger.severe  "severe"
            logger.warning "warning"
            logger.info    "info"
            logger.config  "config"
            logger.fine    "fine"
            logger.finer   "finer"
            logger.finest  "finest"

            assert result.toString() == "(SEVERE)severe(WARNING)warning(INFO)info(CONFIG)config(FINE)fine(FINER)finer(FINEST)finest"

            logger.level = oldLevel
        }
    }
    
    void testToString() {
        def log = new GroovyLogger("foobar")
        assert log.toString() == "[GroovyLogger $log.name"
    }
}
