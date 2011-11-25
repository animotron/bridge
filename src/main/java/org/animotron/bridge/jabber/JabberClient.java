/*
 *  Copyright (C) 2011 The Animo Project
 *  http://animotron.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.animotron.bridge.jabber;

import javolution.util.FastMap;
import org.animotron.bridge.FSBridge;
import org.animotron.expression.AnimoExpression;
import org.animotron.graph.AnimoGraph;
import org.animotron.graph.serializer.AnimoPrettyResultSerializer;
import org.animotron.graph.serializer.AnimoPrettySerializer;
import org.animotron.statement.operator.THE;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.util.Map;

import static org.animotron.graph.AnimoGraph.startDB;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class JabberClient implements MessageListener, ChatManagerListener, PacketListener {
	
	static {
    	//initialize animo
    	startDB("data");

    	try {
		    FSBridge.load("src/main/animo/");
		    FSBridge.load("etc/");
    	} catch (Exception e) {
		}
	}
	
	//private Node I = THE._("animotron@gmail.com");
	
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
		return "animotron@gmail.com";
	}
	
	protected String getPassword() {
		return "S68lib16";
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
				op = THE._.get(msg.substring(5).trim());
				sendResult = false;
				
			} else if (msg.substring(0,3).toLowerCase().equals("ann")) {
				op = new AnimoExpression(msg.substring(4));
			} else
				return null;
		} else
			op = new AnimoExpression(msg);

        try {
        	if (!sendResult)
        		return AnimoPrettySerializer._.serialize(op);
        	else
        		return AnimoPrettyResultSerializer._.serialize(op);

        } catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public static void main(String[] args) throws Exception {
		JabberClient client = new JabberClient();
		
		Thread.sleep(12*60*60*1000);
		
		client.disconnect();
		
		AnimoGraph.shutdownDB();
		
	}
}