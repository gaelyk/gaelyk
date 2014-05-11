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
package groovyx.gaelyk.extensions;

import groovy.lang.IntRange;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.StreamingMarkupBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.xml.sax.SAXException;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.MessageType;
import com.google.appengine.api.xmpp.Presence;
import com.google.appengine.api.xmpp.PresenceBuilder;
import com.google.appengine.api.xmpp.PresenceShow;
import com.google.appengine.api.xmpp.PresenceType;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.SendResponse.Status;
import com.google.appengine.api.xmpp.Subscription;
import com.google.appengine.api.xmpp.SubscriptionBuilder;
import com.google.appengine.api.xmpp.SubscriptionType;
import com.google.appengine.api.xmpp.XMPPService;

/**
 * Jabber / XMPP extension methods
 *
 * @author Guillaume Laforge
 */
public class XmppExtensions {
    private static final String TYPE_ATTR = "type";
    private static final String FROM_ATTR = "from";
    private static final String TO_ATTR = "to";
    private static final String TEXT_BODY_ATTR = "body";
    private static final String XML_BODY_ATTR = "xml";

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
    public static SendResponse send(XMPPService xmppService, @SuppressWarnings("rawtypes") Map msgAttr) {
        MessageBuilder msgBuilder = new MessageBuilder();

        if (msgAttr.containsKey(XML_BODY_ATTR) && msgAttr.containsKey(TEXT_BODY_ATTR)) {
            throw new RuntimeException("You have to choose between XML and text bodies, you can't have both!");
        }

        // sets the body of the message
        if (msgAttr.containsKey(XML_BODY_ATTR)) {
            msgBuilder.asXml(true);
            Object xml = new StreamingMarkupBuilder().bind(msgAttr.get(XML_BODY_ATTR));
            msgBuilder.withBody(String.valueOf(xml));
        } else if (msgAttr.containsKey(TEXT_BODY_ATTR)) {
            msgBuilder.withBody(String.valueOf(msgAttr.get(TEXT_BODY_ATTR)));
        }

        // sets the recepients of the message
        if (msgAttr.containsKey(TO_ATTR)) {
            Object to = msgAttr.get(TO_ATTR);
            if (to instanceof String) {
                msgBuilder.withRecipientJids(new JID((String) to));
            } else if (to instanceof List<?>) {
                List<?> toList = (List<?>)to;
                JID[] jids = new JID[toList.size()];
                for (int i = 0; i < toList.size(); i++) {
                    jids[i] = new JID(String.valueOf(toList.get(i)));
                }
                msgBuilder.withRecipientJids(jids);
            }
        }

        // sets the sender of the message
        if (msgAttr.containsKey(FROM_ATTR)) {
            msgBuilder.withFromJid(new JID(String.valueOf(msgAttr.get(FROM_ATTR))));
        }

        // sets the type of the message
        if (msgAttr.containsKey(TYPE_ATTR)) {
            Object type = msgAttr.get(TYPE_ATTR);
            if (type instanceof MessageType) {
                msgBuilder.withMessageType((MessageType) type);
            } else if (type instanceof String) {
                msgBuilder.withMessageType(MessageType.valueOf((String) type));
            }
        }

        return xmppService.sendMessage(msgBuilder.build());
    }

    /**
     * Send a chat invitation to a Jabber ID.
     *
     * @param the Jabber ID to invite
     */
    public static void sendInvitation(XMPPService xmppService, String jabberId) {
        xmppService.sendInvitation(new JID(jabberId));
    }

    /**
     * Send a chat invitation to a Jabber ID from another Jabber ID.
     *
     * @param jabberIdTo the Jabber ID to invite
     * @param jabberIdFrom the Jabber ID to use to send the invitation request
     */
    public static void sendInvitation(XMPPService xmppService, String jabberIdTo, String jabberIdFrom) {
        xmppService.sendInvitation(new JID(jabberIdTo), new JID(jabberIdFrom));
    }

    /**
     * Get the presence of a Jabber ID.
     *
     * @param the Jabber ID
     * @return the presence information
     */
    public static Presence getPresence(XMPPService xmppService, String jabberId) {
        return xmppService.getPresence(new JID(jabberId));
    }

    /**
     * Get the presence of a Jabber ID.
     *
     * @param jabberIdTo the Jabber ID to get the presence from
     * @param jabberIdFrom the Jabber ID to use to send the presence request
     * @return the presence information
     */
    public static Presence getPresence(XMPPService xmppService, String jabberIdTo, String jabberIdFrom) {
        return xmppService.getPresence(new JID(jabberIdTo), new JID(jabberIdFrom));
    }

    /**
     * Get the sender Jabber ID of the message in the form of a String.
     *
     * @return the Jabber ID of the sender
     */
    public static String getFrom(com.google.appengine.api.xmpp.Message message) {
        return message.getFromJid().getId();
    }

    /**
     * Get the XML content of this message (if it's an XML message) in the form of a DOM parsed with XmlSlurper.
     *
     * @return the slurped XML document
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    public static GPathResult xml(com.google.appengine.api.xmpp.Message message) throws IOException, SAXException, ParserConfigurationException {
        if (message.isXml()) {
            XmlSlurper slurper = new XmlSlurper();
            return slurper.parseText(message.getStanza());
        } else {
            throw new RuntimeException("You can't get the XML of this message as this is not an XML message.");
        }
    }

    /**
     * Gets the list of recipients of this message in the form of a list of Jabber ID strings.
     *
     * @return a list of Jabber ID strings
     */
    public static List<String> getRecipients(com.google.appengine.api.xmpp.Message message) {
        List<String> ids = new ArrayList<>();
        for (JID jid : message.getRecipientJids()) {
            ids.add(jid.getId());
        }
        return ids;
    }

    /**
     * Checks the status of the sending of the message was successful for all its recipients
     */
    public static boolean isSuccessful(SendResponse status) {
        for ( Status s : status.getStatusMap().values()) {
            if (!SendResponse.Status.SUCCESS.equals(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Override the GAE SDK XMPPService#parsePresence as it hard-codes the path for the presence handler,
     * thus preventing from using Gaelyk's routing to point at our own handler.
     *
     * @param xmppService the XMPP service
     * @param request the servlet request
     * @return a Presence
     * @throws IOException 
     */
    public static Presence parsePresence(XMPPService xmppService, HttpServletRequest request) throws IOException {
        // value of the presence, added by the routing logic as request parameter
        String value = request.getParameter("value");

        @SuppressWarnings("rawtypes") Map formData = parseXmppFormData(request);

        return new PresenceBuilder()
                .withFromJid(new JID((String) formData.get("from")))
                .withToJid(new JID((String) formData.get("to")))
                .withPresenceType(PresenceType.valueOf(value.toUpperCase()))
                .withPresenceShow("available".equals(value) ? PresenceShow.NONE : null)
                .build();
    }

    /**
     * Override the GAE SDK XMPPService#parseSubscription as it hard-codes the path for the subscription handler,
     * thus preventing from using Gaelyk's routing to point at our own handler.
     *
     * @param xmppService the XMPP service
     * @param request the servlet request
     * @return a Subscription
     * @throws IOException 
     */
    public static Subscription parseSubscription(XMPPService xmppService, HttpServletRequest request) throws IOException {
        // value of the subscription, added by the routing logic as request parameter
        String value = request.getParameter("value");

        @SuppressWarnings("rawtypes") Map formData = parseXmppFormData(request);

        return new SubscriptionBuilder()
                .withFromJid(new JID((String) formData.get("from")))
                .withToJid(new JID((String) formData.get("to")))
                .withSubscriptionType(SubscriptionType.valueOf(value.toUpperCase()))
                .build();
    }

    /**
     * Parse the form-data from the Jabber requests,
     * as it contains useful information like presence and subscription details, etc.
     *
     * @param text the body of the request
     * @return a map containing form-data key value pairs
     * @throws IOException 
     */
    public static Map<String, String> parseXmppFormData(HttpServletRequest request) throws IOException {
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

        String body = IOGroovyMethods.getText(request.getReader());

        // split the request body lines
        List<String> lines = StringGroovyMethods.readLines(body);

        // split the form-data lines around the boundaries
        // remove a first surrounding empty lines and closing boundary
        // trim the last \n characters
        List<String> parts = DefaultGroovyMethods.getAt(body.split(lines.get(0).trim()), new IntRange(true, 1,-2));
        for (int i = 0; i < parts.size(); i++) {
            parts.set(i, parts.get(i).trim()); 
        }        

        // reads the part keys and values into a Map
        Map<String, String> ret = new HashMap<String, String>(parts.size());
        Pattern pattern = Pattern.compile(".*name=\"(.*)\".*");
        for (String part : parts) {
            List<String> partLines = StringGroovyMethods.readLines(part);
            String name = null;
            for (String line : partLines) {
                if (line.startsWith("Content-Disposition: form-data")) {
                    Matcher m = pattern.matcher(line);
                    if (m.matches()) {
                        name = m.group(1);
                    }
                    break;
                }
            }
            ret.put(name, partLines.get(partLines.size() - 1));
        }
        return ret;
    }
}
