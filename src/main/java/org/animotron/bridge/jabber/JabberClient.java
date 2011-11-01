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

import java.util.Collection;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class JabberClient implements MessageListener {
	
	XMPPConnection connection;
	
	public JabberClient() throws XMPPException {
		ConnectionConfiguration config = 
				new ConnectionConfiguration("talk.google.com", 
								5222, 
								"gmail.com");
		connection = new XMPPConnection(config);
		connection.connect();
		connection.login("login", "psswd");
		
		Roster roster = connection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		 
		System.out.println("\n\n" + entries.size() + " buddy(ies):");
		for(RosterEntry r:entries) {
			System.out.println(r.getUser());
		}
		
		Chat chat = connection.getChatManager().createChat("somebody@gmail.com", this);
		chat.sendMessage("ready...");
	}
	
	public void disconnect() {
		connection.disconnect();
	}
	
	@Override
	public void processMessage(Chat chat, Message message) {
		System.out.println(message.getBody());
		try {
			chat.sendMessage("get: "+message.getBody());
		} catch (XMPPException e) {
		}
	}

	public static void main(String[] args) throws Exception {
		JabberClient client = new JabberClient();
		
		Thread.sleep(60*1000);
		
		client.disconnect();
		
	}
}