package org.wanna.jabbot.extensions.foaas;

/**
 * @author vmorsiani <vmorsiani>
 * @since 2014-05-31
 */

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanna.jabbot.command.MessageWrapper;
import org.wanna.jabbot.command.MucHolder;
import org.wanna.jabbot.command.config.CommandConfig;
import org.wanna.jabbot.extensions.AbstractCommandAdapter;
import org.wanna.jabbot.extensions.foaas.binding.Field;
import org.wanna.jabbot.extensions.foaas.binding.Operation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class AttackCommand extends AbstractCommandAdapter {
	final Logger logger = LoggerFactory.getLogger(AttackCommand.class);

	private final Random randomizer = new Random();
	private Map<Integer,List<Operation>> operationsMap = new HashMap<>();

	public AttackCommand(CommandConfig configuration) {
		super(configuration);
		initializeOperations();
	}

	@Override
	public void process(MucHolder chatroom, MessageWrapper message) {
		String response;
		if(getParsedCommand().getArgs() != null ){
			//Add +1 to args lenght as we'll always have a "from" arg from Sender
			int length = getParsedCommand().getArgs().length+1;
			if(length > 2){
				length = 2;
			}

			Operation operation = pickOperation(length);
			String from = message.getSender();

			try {
				this.feedFields(operation,from,getParsedCommand().getArgs());
				response = operation.execute();
				chatroom.sendMessage(secureResponse(response));
			} catch (UnsupportedEncodingException e) {
				logger.error("unable to set Fields",e);
			}
		}
	}

	private void feedFields(Operation operation,String from, String[] args) throws UnsupportedEncodingException {
		int i = 0;
		for (Field field : operation.getFields()) {
			if(field.getField().equalsIgnoreCase("from")){
				field.setValue(URLEncoder.encode(from,"UTF-8"));
			}else if(args != null && args.length > i){
				field.setValue(URLEncoder.encode(args[i],"UTF-8"));
				i++;
			}
		}
	}

	/**
	 * Make sure one does not use / command from jabber in order to spam someone else
	 * using /say or having the bot acting weird using /me.
	 * by Stripping all the leading / from the response
	 *
	 * @param response the raw response to be returned
	 * @return cleaned response
	 */
	private String secureResponse(String response) throws UnsupportedEncodingException {
		response = URLDecoder.decode(response,"UTF-8");
		while(response.startsWith("/")){
			response = response.replace("/","");
		}
		return response;
	}

	/**
	 * Picks a list of operations based on parameter count
	 * then picks a random operation out of the list
	 *
	 * @param argsLength amount of available parameters
	 *
	 * @return Random operation based on argsLength
	 */
	private Operation pickOperation(int argsLength){
		if(argsLength > operationsMap.size()){
			argsLength = operationsMap.size();
		}

		final List<Operation> attacks;
		attacks = operationsMap.get(argsLength);
		return attacks.get(randomizer.nextInt(attacks.size()));
	}

	/**
	 * Initialize an operations map using the http://foaas.herokuapp.com/operations
	 * If the operation requires more than 2 fields discard it.
	 * discarding should be done until at least proper args delimiter is supported
	 *
	 * supported fields are:
	 * From: represents the user which triggered the command
	 * Name: represents the first argument passed to the command
	 */
	private void initializeOperations(){
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String json;
		List<Operation> operations;
		try {
			json = query("/operations");
			TypeFactory typeFactory = mapper.getTypeFactory();
			CollectionType collectionType = typeFactory.constructCollectionType(
					List.class, Operation.class);
			operations = mapper.readValue(json, collectionType);
			for (Operation operation : operations) {
				//Only allow 2 fields commands for now (from & name)
				if(operation.getFields().size() <= 2) {
					if (!operationsMap.containsKey(operation.getFields().size())) {
						ArrayList<Operation> array = new ArrayList<>();
						array.add(operation);
						operationsMap.put(operation.getFields().size(), array);
					} else {
						operationsMap.get(operation.getFields().size()).add(operation);
					}
				}
			}
		} catch (IOException e) {
			logger.error("error initializing operation map",e);
		}

	}

	private String query(String option) throws IOException {
		final DefaultHttpClient httpclient = new DefaultHttpClient();
		final String baseUrl = "http://foaas.com";
		HttpGet httpGet = new HttpGet(baseUrl +option);
		httpGet.setHeader("Accept","text/plain");

		try
		{
			HttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				return EntityUtils.toString(entity, HTTP.UTF_8);
			}
		} catch (IOException e) {
			logger.error("error querying foaas",e);
		}

		return null;
	}
}
