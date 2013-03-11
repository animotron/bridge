/*
 *  Copyright (C) 2011-2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.animotron.bridge.jabber;

import javolution.util.FastMap;
import org.animotron.expression.AnimoExpression;
import org.animotron.graph.AnimoGraph;
import org.animotron.statement.operator.DEF;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.util.Map;

import static org.animotron.graph.AnimoGraph.startDB;
import static org.animotron.graph.serializer.Serializer.PRETTY_ANIMO;
import static org.animotron.graph.serializer.Serializer.PRETTY_ANIMO_RESULT;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class JabberClient implements MessageListener, ChatManagerListener, PacketListener {
	
	static {
    	//initialize animo
    	startDB("data");

//    	try {
//		    FSBridge.load("src/main/animo/");
//		    FSBridge.load("etc/");
//    	} catch (Throwable t) {
//		}
	}
	
	//private Node I = DEF._("animotron@gmail.com");
	
	XMPPConnection connection;
	Map<String, MultiUserChat> chats = new FastMap<String, MultiUserChat>();
	
	public JabberClient() throws XMPPException, IOException {

		ConnectionConfiguration config = 
			new ConnectionConfiguration(
				getHost(), 
				Integer.valueOf(getPort()), 
				getServiceName()
			);
		connection = new XMPPConnection(config);
		connection.connect();
		connection.login(getUsername(), getPassword());
		
		connection.getChatManager().addChatListener(this);
		
		Presence presence = new Presence(Presence.Type.available);
		presence.setPriority(10);
		connection.sendPacket(presence);
		
	}
	
	protected String getHost() {
		return "talk.google.com";
	}

	protected String getPort() {
		return "5222";
	}

	protected String getServiceName() {
		return "gmail.com";
	}

	protected String getUsername() {
		return "ann@animotron.org";
	}
	
	protected String getPassword() {
		return "";
	}

	public void disconnect() {
		connection.disconnect();
	}
	
	@Override
	public void processMessage(Chat chat, Message message) {
		System.out.println("from = "+message.getFrom());
		System.out.println("body = "+message.getBody());
		
		if (message.getType() == Message.Type.error)
			return;
		
		try {
			if (message.getFrom().endsWith("groupchat.google.com")) {
				System.out.println("joining...");
				MultiUserChat muc = new MultiUserChat(connection, message.getFrom());
				muc.join(getUsername(), getPassword());
				muc.addMessageListener(this);
				chats.put(message.getFrom(), muc);
				return;
			} else {
				String msg = processMessage(message, false);
				if (msg != null) {
					chat.sendMessage( msg );

					Presence presence = new Presence(Presence.Type.available);
					presence.setPriority(10);
					connection.sendPacket(presence);
				}
			}
		} catch (XMPPException e) {
		}
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		System.out.println("chatCreated "+chat.getParticipant());
		if (!chat.getListeners().contains(this))
			chat.addMessageListener(this);
		try {
			chat.sendMessage("Hello");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof Message) { 
			Message message = (Message)packet;
	
			System.out.println("GET PACKET");
			System.out.println("from = "+message.getFrom());
			System.out.println("body = "+message.getBody());
			System.out.println("type = "+message.getType());
			
			if (message.getType() == Message.Type.error)
				return;
	
			String key = message.getFrom();
			key = key.substring(0, key.lastIndexOf('/'));
			MultiUserChat muc = chats.get(key);
			
			if (muc != null && !message.getFrom().endsWith(getUsername())) {
				if (message.getType() == Message.Type.groupchat)
					try {
						String msg = processMessage(message, true);
						if (msg != null)
							muc.sendMessage( msg );
					} catch (XMPPException e) {
					}
			}
		}
	}
	
	private String processMessage(Message message, boolean expectCall) {
		System.out.println("Thread "+message.getThread());
		String msg = message.getBody();
		
		boolean sendResult = true;
		
		Relationship op = null;
		if (expectCall) {
			if (msg.substring(0,4).toLowerCase().equals("anna")) {
				op = DEF._.get(msg.substring(5).trim());
				sendResult = false;
				
			} else if (msg.substring(0,3).toLowerCase().equals("ann")) {
				op = new AnimoExpression(msg.substring(4));
			} else
				return null;
		} else
			op = new AnimoExpression(msg);

        try {
        	if (!sendResult)
        		return PRETTY_ANIMO.serialize(op);
        	else
        		return PRETTY_ANIMO_RESULT.serialize(op);

        } catch (Throwable t) {
			t.printStackTrace();
			return t.getMessage();
		}
	}

	public static void main(String[] args) throws Throwable {
		JabberClient client = new JabberClient();
		
		Thread.sleep(12*60*60*1000);
		
		client.disconnect();
		
		AnimoGraph.shutdownDB();
		
	}
}