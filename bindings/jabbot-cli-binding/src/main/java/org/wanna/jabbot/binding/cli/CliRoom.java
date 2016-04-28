package org.wanna.jabbot.binding.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanna.jabbot.binding.AbstractRoom;
import org.wanna.jabbot.binding.BindingListener;
import org.wanna.jabbot.binding.DefaultBindingMessage;
import org.wanna.jabbot.binding.config.RoomConfiguration;
import org.wanna.jabbot.binding.event.MessageEvent;
import org.wanna.jabbot.binding.messaging.DefaultResource;
import org.wanna.jabbot.binding.messaging.Message;
import org.wanna.jabbot.binding.messaging.body.BodyPart;
import org.wanna.jabbot.binding.messaging.body.TextBodyPart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vmorsiani <vmorsiani>
 * @since 2014-08-14
 */
public class CliRoom extends AbstractRoom<CliBinding> implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(CliRoom.class);
	private RoomConfiguration configuration;


	public CliRoom(CliBinding connection) {
		super(connection);

	}

	@Override
	public void run()
	{
		System.out.println("CLI Room started");
		BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				String line="";
				
				while (true) {
					line += buffer.readLine();

					if(line == null) {
						logger.error("Got a null, probably no console, dying now");
						return;
					}

					if (line.endsWith("\\")) {
						line = line.substring(0, line.length() - 1);
					} else {
						logger.debug("received {}",line);
						break;
					}
				}

				DefaultBindingMessage message = new DefaultBindingMessage();
				message.addBody(new TextBodyPart(line));
				message.setSender(new DefaultResource(this.getRoomName(),"cli"));
				message.setDestination(new DefaultResource("jabbot",null));
				message.setRoomName(this.getRoomName());

				super.connection.dispatchEvent(new MessageEvent(super.connection,message));
			} catch (IOException e) {
				logger.error("IO Error reading sdtin, dying");
				return;
			}
		}
	}

	@Override
	public boolean sendMessage(Message message) {
        for (BodyPart bodyPart : message.getBodies()) {
            System.out.println("received message with body type: "+bodyPart.getType());
            System.out.println(bodyPart.getText());
        }
		return true;
	}

	@Override
	public boolean join(final RoomConfiguration configuration) {
		this.configuration = configuration;
		new Thread(this).start();
		return true;
	}

	@Override
	public String getRoomName() {
		return configuration.getName();
	}
}
