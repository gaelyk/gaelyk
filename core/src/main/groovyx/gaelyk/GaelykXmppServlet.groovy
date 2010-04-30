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

import com.google.appengine.api.xmpp.XMPPService
import com.google.appengine.api.xmpp.Message
import com.google.appengine.api.xmpp.XMPPServiceFactory

/**
 * Servlet for handling incoming XMPP/Jabber messages.
 * This servlet should be configured with a servlet mapping to the <code>_ah/xmpp/message/chat/</code>
 * as this is the hard-coded path Google App Engine uses.
 *
 * @author Guillaume Laforge
 */
class GaelykXmppServlet extends HttpServlet {

    /**
     * Handles XMPP incoming messages.
     * <p>
     * An instance of <code>Message</code> named <code>message</code> is added into the binding.
     */
    void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        XMPPService xmpp = XMPPServiceFactory.XMPPService
        Message message = xmpp.parseMessage(req)

        def binding = new Binding(message: message)
        GaelykBindingEnhancer.bind(binding)

        use (GaelykCategory) {
            new GroovyShell(binding).evaluate(new File('WEB-INF/groovy/jabber.groovy'))
        }
    }
}