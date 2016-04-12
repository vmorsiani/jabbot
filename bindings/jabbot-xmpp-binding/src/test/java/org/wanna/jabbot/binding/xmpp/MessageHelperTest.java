package org.wanna.jabbot.binding.xmpp;


import org.jivesoftware.smack.packet.Message;
import org.junit.Assert;
import org.junit.Test;
import org.wanna.jabbot.binding.messaging.Resource;
import org.wanna.jabbot.binding.messaging.body.TextBodyPart;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * @author Vincent Morsiani [vmorsiani@voxbone.com]
 * @since 2015-07-25
 */
public class MessageHelperTest {
    private org.jivesoftware.smack.packet.Message xmppMessage;
    private XmppMessage jabbotMessage;

    @Test
    public void createResponseFromPrivateMessage(){
        jabbotMessage = new XmppMessage();
        jabbotMessage.addBody(new TextBodyPart("test body"));
        jabbotMessage.setSender(new XmppResource("user1@test.com/User",null));
        jabbotMessage.setDestination(new XmppResource("jabbot@test.com/jabbot",null, Resource.Type.USER));

        xmppMessage = MessageHelper.createResponseMessage(jabbotMessage);
        Assert.assertThat(xmppMessage.getType(),is(Message.Type.chat));
        Assert.assertThat(xmppMessage.getFrom(),is("user1@test.com/User"));
        Assert.assertThat(xmppMessage.getTo(),is("jabbot@test.com/jabbot"));
        Assert.assertThat(xmppMessage.getBody(),is(jabbotMessage.getBody()));
    }

    @Test
    public void createResponseFromChatroomMessage(){
        jabbotMessage = new XmppMessage();
        jabbotMessage.addBody(new TextBodyPart("test body"));
        jabbotMessage.setSender(new XmppResource("chatroom1@conference.test.com", "User1", Resource.Type.ROOM));
        jabbotMessage.setDestination(new XmppResource("jabbot@test.com/jabbot",null));
        xmppMessage = MessageHelper.createResponseMessage(jabbotMessage);
        Assert.assertThat(xmppMessage.getType(),is(Message.Type.groupchat));
        Assert.assertThat(xmppMessage.getFrom(),is("chatroom1@conference.test.com"));
        Assert.assertThat(xmppMessage.getTo(),is("jabbot@test.com/jabbot"));
        Assert.assertThat(xmppMessage.getBody(),is(jabbotMessage.getBody()));
    }

    @Test
    public void createRequestFromPrivateMessage(){
        xmppMessage = new Message();
        xmppMessage.setBody("test body");
        xmppMessage.setTo("jabbot@test.com/jabbot");
        xmppMessage.setType(Message.Type.chat);
        xmppMessage.setFrom("chatroom1@conference.test.com/User1");

        jabbotMessage = MessageHelper.createRequestMessage(xmppMessage);
        Assert.assertThat(jabbotMessage.getDestination().getAddress(),is("jabbot@test.com/jabbot"));
        Assert.assertThat(jabbotMessage.getSender().getAddress(),is("chatroom1@conference.test.com/User1"));
        Assert.assertThat(jabbotMessage.getRoomName(),is(nullValue()));
        Assert.assertThat(jabbotMessage.getBody(),is(xmppMessage.getBody()));
    }

    @Test
    public void createRequestFromChatroomMessage(){
        xmppMessage = new Message();
        xmppMessage.setTo("jabbot@test.com/jabbot");
        xmppMessage.setType(Message.Type.groupchat);
        xmppMessage.setFrom("chatroom1@conference.test.com/User1");

        jabbotMessage = MessageHelper.createRequestMessage(xmppMessage);
        Assert.assertThat(jabbotMessage.getDestination().getAddress(),is("jabbot@test.com/jabbot"));
        Assert.assertThat(jabbotMessage.getSender().getName(),is("User1"));
        Assert.assertThat(jabbotMessage.getRoomName(),is(nullValue()));
    }
}