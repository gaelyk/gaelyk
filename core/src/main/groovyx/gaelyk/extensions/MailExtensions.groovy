/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.extensions

import com.google.appengine.api.mail.MailService
import groovy.transform.CompileStatic
import javax.mail.internet.MimeMessage
import javax.servlet.http.HttpServletRequest
import javax.mail.Session

/**
 * Extension methods dedicated to the Mail service
 *
 * @author Guillaume Laforge
 */
class MailExtensions {
    /**
     * Create a <code>MailService.Message</code> out of Map parameters.
     * Each map key must correspond to a valid property on the message object.
     */
    private static MailService.Message createMessageFromMap(Map m) {
        def msg = new MailService.Message()
        m.each { k, v ->
            // to and bcc fields contain collection of addresses
            // so if only one is provided, wrap it in a collection
            if (k in ['to', 'bcc'] && v instanceof String) v = [v]

            // adds a 'from' alias for 'sender'
            if (k == 'from') k = 'sender'

            // single email attachment
            if (k == 'attachment' && v instanceof Map) {
                k = 'attachments'
                v = [new MailService.Attachment(v.fileName, v.data)] as MailService.Attachment[]
            }

            // collects Attachments and maps representing attachments as a MailMessage.Attachment collection
            if (k == 'attachments') {
                v = v.collect { attachment ->
                    if (attachment instanceof MailService.Attachment)
                        attachment
                    else if (attachment instanceof Map)
                        new MailService.Attachment(attachment.fileName, attachment.data)
                } as MailService.Attachment[]
            }

            // set the property on Message object
            msg."$k" = v
        }
        return msg
    }

    /**
     * Additional <code>send()</code> method taking a map as parameter.
     * The map can contain the normal properties of the
     * <code>MailService.Message</code> class.
     *
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    @CompileStatic
    static void send(MailService mailService, Map m) {
        MailService.Message msg = createMessageFromMap(m)
        mailService.send msg
    }

    /**
     * Additional <code>sendToAdmins()</code> method for sending emails to the application admins.
     * This method is taking a map as parameter.
     * The map can contain the normal properties of the
     * <code>MailService.Message</code> class.
     *
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    @CompileStatic
    static void sendToAdmins(MailService mailService, Map m) {
        MailService.Message msg = createMessageFromMap(m)
        mailService.sendToAdmins msg
    }

    /**
     * Parses an incoming email message coming from the request into a <code>MimeMessage</code>
     *
     * @param request incoming request
     * @return an instance of <code>MimeMessage</code>
     */
    @CompileStatic
    static MimeMessage parseMessage(MailService mailService, HttpServletRequest request) {
        def session = Session.getDefaultInstance(new Properties(), null)
        return new MimeMessage(session, request.inputStream)
    }
}
