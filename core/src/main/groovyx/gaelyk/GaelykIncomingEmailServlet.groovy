/*
 * Copyright 2009 the original author or authors.
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
package groovyx.gaelyk

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.mail.internet.MimeMessage
import javax.mail.Session

/**
 * Servlet for handling incoming Email messages.
 * This servlet should be configured with a servlet mapping to the <code>_ah/mail/*</code>
 * as this is the hard-coded path Google App Engine uses.
 * The start represents the name part of the email address of the sender (the part before the @ character)
 *
 * @author Guillaume Laforge
 */
class GaelykIncomingEmailServlet extends HttpServlet {

    /**
     * Handles incoming Email messages.
     * <p>
     * An instance of <code>MimeMessage</code> named <code>message</code> is added into the binding.
     */
    void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        def props = new Properties()
        def session = Session.getDefaultInstance(props, null)
        def message = new MimeMessage(session, req.inputStream)
        
        def binding = new Binding(message: message)
        GaelykBindingEnhancer.bind(binding)

        use (GaelykCategory) {
            new GroovyShell(binding).evaluate(new File('WEB-INF/groovy/email.groovy'))
        }
    }
}