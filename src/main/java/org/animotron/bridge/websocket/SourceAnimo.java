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
package org.animotron.bridge.websocket;

import org.animotron.cache.FileCache;
import org.animotron.statement.operator.DEF;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.animotron.graph.serializer.Serializer.PRETTY_ANIMO;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
@WebSocket
public class SourceAnimo {

    @OnWebSocketMessage

    public void onMessage(Session session, String data) {
        if (data.isEmpty())
            return;
        try {
            Relationship r = DEF._.get(data);
            if (r != null) {
                session.getRemote().sendString(PRETTY_ANIMO.serialize(r, FileCache._));
            } else {
                //XXX: send error message
            }
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            try {
                session.getRemote().sendString(sw.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
