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

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelMessage;

/**
 * Channel service extension methods
 *
 * @author Guillaume Laforge
 */
public class ChannelExtensions {

    /**
     * Send a message through the Channel service
     *
     * @param clientId the client ID
     * @param message the message to send
     */
    public static void send(ChannelService channel, String clientId, String message) {
        channel.sendMessage(new ChannelMessage(clientId, message));
    }
}
