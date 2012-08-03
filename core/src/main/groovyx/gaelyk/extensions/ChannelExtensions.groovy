package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.channel.ChannelService
import com.google.appengine.api.channel.ChannelMessage

/**
 * Channel service extension methods
 */
@CompileStatic
class ChannelExtensions {

    /**
     * Send a message through the Channel service
     *
     * @param clientId the client ID
     * @param message the message to send
     */
    static void send(ChannelService channel, String clientId, String message) {
        channel.sendMessage(new ChannelMessage(clientId, message))
    }
}
