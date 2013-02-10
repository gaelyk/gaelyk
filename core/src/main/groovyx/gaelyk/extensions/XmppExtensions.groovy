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

import com.google.appengine.api.xmpp.SendResponse
import com.google.appengine.api.xmpp.XMPPService
import com.google.appengine.api.xmpp.MessageBuilder
import groovy.xml.StreamingMarkupBuilder
import com.google.appengine.api.xmpp.JID
import com.google.appengine.api.xmpp.MessageType
import groovy.transform.CompileStatic
import com.google.appengine.api.xmpp.Presence
import groovy.util.slurpersupport.GPathResult
import javax.servlet.http.HttpServletRequest
import com.google.appengine.api.xmpp.PresenceBuilder
import com.google.appengine.api.xmpp.PresenceType
import com.google.appengine.api.xmpp.PresenceShow
import com.google.appengine.api.xmpp.Subscription
import com.google.appengine.api.xmpp.SubscriptionBuilder
import com.google.appengine.api.xmpp.SubscriptionType

/**
 * Jabber / XMPP extension methods
 *
 * @author Guillaume Laforge
 */
class XmppExtensions {
    /**
     * Send an XMPP/Jabber message with the XMPP service using a map of attributes to build the message.
     * <p>
     * Possible attributes are:
     * <ul>
     * <li>from: the sender Jabber ID represented as a String</li>
     * <li>to: a String or a list of String representing recepients' Jabber IDs</li>
     * <li>type: an instance of the MessageType enum, or a String representation
     * ('CHAT', 'ERROR', 'GROUPCHAT', 'HEADLINE', 'NORMAL')</li>
     * <li>body: a String representing the raw text to send</li>
     * <li>xml: a closure representing the XML you want to send (serialized using StreamingMarkupBuilder)</li>
     * </ul>
     *
     * @param msgAttr a map of attributes as described
     * @return an intance of SendResponse
     */
    static SendResponse send(XMPPService xmppService, Map msgAttr) {
        MessageBuilder msgBuilder = new MessageBuilder()

        if (msgAttr.xml && msgAttr.body) {
            throw new RuntimeException("You have to choose between XML and text bodies, you can't have both!")
        }

        // sets the body of the message
        if (msgAttr.xml) {
            msgBuilder.asXml(true)
            def xml = new StreamingMarkupBuilder().bind(msgAttr.xml)
            msgBuilder.withBody(xml.toString())
        } else if (msgAttr.body) {
            msgBuilder.withBody(msgAttr.body)
        }

        // sets the recepients of the message
        if (msgAttr.to) {
            if (msgAttr.to instanceof String) {
                msgBuilder.withRecipientJids(new JID(msgAttr.to))
            } else if (msgAttr.to instanceof List) {
                msgBuilder.withRecipientJids(msgAttr.to.collect{ new JID(it) } as JID[])
            }
        }

        // sets the sender of the message
        if (msgAttr.from) {
            msgBuilder.withFromJid(new JID(msgAttr.from))
        }

        // sets the type of the message
        if (msgAttr.type) {
            if (msgAttr.type instanceof MessageType) {
                msgBuilder.withMessageType(msgAttr.type)
            } else if (msgAttr.type instanceof String) {
                msgBuilder.withMessageType(MessageType.valueOf(msgAttr.type))
            }
        }

        xmppService.sendMessage(msgBuilder.build())
    }

    /**
     * Send a chat invitation to a Jabber ID.
     *
     * @param the Jabber ID to invite
     */
    @CompileStatic
    static void sendInvitation(XMPPService xmppService, String jabberId) {
        xmppService.sendInvitation(new JID(jabberId))
    }

    /**
     * Send a chat invitation to a Jabber ID from another Jabber ID.
     *
     * @param jabberIdTo the Jabber ID to invite
     * @param jabberIdFrom the Jabber ID to use to send the invitation request
     */
    @CompileStatic
    static void sendInvitation(XMPPService xmppService, String jabberIdTo, String jabberIdFrom) {
        xmppService.sendInvitation(new JID(jabberIdTo), new JID(jabberIdFrom))
    }

    /**
     * Get the presence of a Jabber ID.
     *
     * @param the Jabber ID
     * @return the presence information
     */
    @CompileStatic
    static Presence getPresence(XMPPService xmppService, String jabberId) {
        xmppService.getPresence(new JID(jabberId))
    }

    /**
     * Get the presence of a Jabber ID.
     *
     * @param jabberIdTo the Jabber ID to get the presence from
     * @param jabberIdFrom the Jabber ID to use to send the presence request
     * @return the presence information
     */
    @CompileStatic
    static Presence getPresence(XMPPService xmppService, String jabberIdTo, String jabberIdFrom) {
        xmppService.getPresence(new JID(jabberIdTo), new JID(jabberIdFrom))
    }

    /**
     * Get the sender Jabber ID of the message in the form of a String.
     *
     * @return the Jabber ID of the sender
     */
    @CompileStatic
    static String getFrom(com.google.appengine.api.xmpp.Message message) {
        message.getFromJid().getId()
    }

    /**
     * Get the XML content of this message (if it's an XML message) in the form of a DOM parsed with XmlSlurper.
     *
     * @return the slurped XML document
     */
    @CompileStatic
    static GPathResult xml(com.google.appengine.api.xmpp.Message message) {
        if (message.isXml()) {
            def slurper = new XmlSlurper()
            return slurper.parseText(message.getStanza())
        } else {
            throw new RuntimeException("You can't get the XML of this message as this is not an XML message.")
        }
    }

    /**
     * Gets the list of recipients of this message in the form of a list of Jabber ID strings.
     *
     * @return a list of Jabber ID strings
     */
    @CompileStatic
    static List getRecipients(com.google.appengine.api.xmpp.Message message) {
        message.getRecipientJids().collect { JID jid -> jid.getId() }
    }

    /**
     * Checks the status of the sending of the message was successful for all its recipients
     */
    static boolean isSuccessful(SendResponse status) {
        status.statusMap.every { it.value == SendResponse.Status.SUCCESS }
    }

    /**
     * Override the GAE SDK XMPPService#parsePresence as it hard-codes the path for the presence handler,
     * thus preventing from using Gaelyk's routing to point at our own handler.
     *
     * @param xmppService the XMPP service
     * @param request the servlet request
     * @return a Presence
     */
    static Presence parsePresence(XMPPService xmppService, HttpServletRequest request) {
        // value of the presence, added by the routing logic as request parameter
        String value = request.getParameter('value')

        Map formData = parseXmppFormData(request)

        new PresenceBuilder()
                .withFromJid(new JID(formData.from))
                .withToJid(new JID(formData.to))
                .withPresenceType(PresenceType."${value.toUpperCase()}")
                .withPresenceShow(value == 'available' ? PresenceShow.NONE : null)
                .build()
    }

    /**
     * Override the GAE SDK XMPPService#parseSubscription as it hard-codes the path for the subscription handler,
     * thus preventing from using Gaelyk's routing to point at our own handler.
     *
     * @param xmppService the XMPP service
     * @param request the servlet request
     * @return a Subscription
     */
    static Subscription parseSubscription(XMPPService xmppService, HttpServletRequest request) {
        // value of the subscription, added by the routing logic as request parameter
        String value = request.getParameter('value')

        Map formData = parseXmppFormData(request)

        new SubscriptionBuilder()
                .withFromJid(new JID(formData.from))
                .withToJid(new JID(formData.to))
                .withSubscriptionType(SubscriptionType."${value.toUpperCase()}")
                .build()
    }

    /**
     * Parse the form-data from the Jabber requests,
     * as it contains useful information like presence and subscription details, etc.
     *
     * @param text the body of the request
     * @return a map containing form-data key value pairs
     */
    static Map parseXmppFormData(HttpServletRequest request) {
        /*
            App Engine encodes the presence, subscription into the body of the post request, in form-data.
            An example form-data follows:

            --ItS1i0T-5328197
            Content-Disposition: form-data; name="to"

            you@you.com
            --ItS1i0T-5328197
            Content-Disposition: form-data; name="from"

            me@me.com
            --ItS1i0T-5328197
            Content-Disposition: form-data; name="available"

            true
            --ItS1i0T-5328197
            Content-Disposition: form-data; name="stanza"
            Content-Type: text/xml

            <presence from="me@me.com" to="you@you.com"><show/><status/></presence>
            --ItS1i0T-5328197--
         */

        def body = request.reader.text

        // split the request body lines
        def lines = body.readLines()

        // split the form-data lines around the boundaries
        // remove a first surrounding empty lines and closing boundary
        // trim the last \n characters
        def parts = body.split(lines[0].trim())[1..-2]*.trim()

        // reads the part keys and values into a Map
        return parts*.readLines().collectEntries {
            [
                    // extract the name from the form-data part
                    (it[it.findIndexOf{ l -> l.startsWith("Content-Disposition: form-data")}] =~ /.*name="(.*)".*/)[0][1],
                    // the last line contains the data associated with the key
                    it[-1]
            ]
        }
    }
}
