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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Header;

/**
 * Extension methods dedicated to the Mail service
 *
 * @author Guillaume Laforge
 */
public class MailExtensions {
    
    /**
     * Create a <code>MailService.Message</code> out of Map parameters.
     * Each map key must correspond to a valid property on the message object.
     */
    @SuppressWarnings("unchecked") private static MailService.Message createMessageFromMap(Map<String, Object> m) {
        MailService.Message msg = new MailService.Message();
        for (Entry<String, Object> e : m.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();

            switch (k) {
                case "headers":
                    if (v instanceof Collection<?>) msg.setHeaders((Collection<Header>) v);
                    else throw new IllegalArgumentException("Headers must be collection of Header objects");
                    break;
                case "to":
                    if (v instanceof Collection<?>) msg.setTo((Collection<String>) v);
                    else msg.setTo(Arrays.asList(new String[] { String.valueOf(v) }));
                    break;
                case "bcc":
                    if (v instanceof Collection<?>) msg.setTo((Collection<String>) v);
                    else msg.setTo(Arrays.asList(new String[] { String.valueOf(v) }));
                    break;
                case "cc":
                    if (v instanceof Collection<?>) msg.setTo((Collection<String>) v);
                    else msg.setTo(Arrays.asList(new String[] { String.valueOf(v) }));
                    break;
                case "from":
                case "sender":
                    msg.setSender(String.valueOf(v));
                    break;
                case "replyTo":
                    msg.setReplyTo(String.valueOf(v));
                    break;
                case "htmlBody":
                    msg.setHtmlBody(String.valueOf(v));
                    break;
                case "textBody":
                    msg.setTextBody(String.valueOf(v));
                    break;
                case "subject":
                    msg.setSubject(String.valueOf(v));
                    break;
                case "attachment":
                case "attachments":
                    if (v instanceof Map<?,?>) {
                        Map<String, Object> opts = (Map<String, Object>) v;
                        List<MailService.Attachment> attchs = new ArrayList<>();
                        attchs.add(new MailService.Attachment((String) opts.get("fileName"), (byte[]) opts.get("data")));
                        v = attchs;
                    }
                    if (v instanceof Collection<?>) {
                        Collection<Object> attchsIn = (Collection<Object>) v;
                        Collection<MailService.Attachment> attchsOut = new ArrayList<>();
                        for (Object attch : attchsIn) {
                            if (attch instanceof MailService.Attachment) {
                                MailService.Attachment a = (MailService.Attachment) attch;
                                attchsOut.add(a);
                            }
                            if (attch instanceof Map<?,?>) {
                                Map<?,?> a = (Map<?,?>) attch;
                                attchsOut.add(new MailService.Attachment((String) a.get("fileName"), (byte[]) a.get("data")));
                            }
                        }
                        msg.setAttachments(attchsOut);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message property " + k);
            }
        }
        return msg;
    }

    /**
     * Additional <code>send()</code> method taking a map as parameter.
     * The map can contain the normal properties of the
     * <code>MailService.Message</code> class.
     * @throws IOException 
     *
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    public static void send(MailService mailService, Map<String, Object> m) throws IOException {
        MailService.Message msg = createMessageFromMap(m);
        mailService.send(msg);
    }

    /**
     * Additional <code>sendToAdmins()</code> method for sending emails to the application admins.
     * This method is taking a map as parameter.
     * The map can contain the normal properties of the
     * <code>MailService.Message</code> class.
     * @throws IOException 
     *
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    public static void sendToAdmins(MailService mailService, Map<String, Object> m) throws IOException {
        MailService.Message msg = createMessageFromMap(m);
        mailService.sendToAdmins(msg);
    }

    /**
     * Parses an incoming email message coming from the request into a <code>MimeMessage</code>.
     *
     * @param request incoming request
     * @return an instance of <code>MimeMessage</code>
     * @throws IOException 
     * @throws MessagingException 
     */
    public static MimeMessage parseMessage(MailService mailService, HttpServletRequest request) throws MessagingException, IOException {
        Session session = Session.getDefaultInstance(new Properties(), null);
        return new MimeMessage(session, request.getInputStream());
    }
}
