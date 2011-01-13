
def message = xmpp.parseMessage(request)

def s = "Received message from ${message.from} with body ${message.body}"

println s