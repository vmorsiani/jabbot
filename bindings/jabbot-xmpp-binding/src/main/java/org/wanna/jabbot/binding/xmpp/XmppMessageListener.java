package org.wanna.jabbot.binding.xmpp;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Stanza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanna.jabbot.binding.BindingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vmorsiani <vmorsiani>
 * @since 2014-05-30
 */
public class XmppMessageListener implements StanzaListener{
	final Logger logger = LoggerFactory.getLogger(XmppMessageListener.class);
	private List<BindingListener> listeners = new ArrayList<>();

	public XmppMessageListener(List<BindingListener> listeners) {
		this.listeners = (listeners==null?new ArrayList<BindingListener>() : listeners);
	}

	@Override
	public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
		if(packet instanceof org.jivesoftware.smack.packet.Message){
			org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message)packet;
            final XmppMessage m = MessageHelper.createRequestMessage(message);
            for (BindingListener listener : listeners) {
                listener.onMessage(m);
            }
		}
	}
}
