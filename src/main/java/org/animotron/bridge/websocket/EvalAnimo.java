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
import org.animotron.expression.AnimoExpression;
import org.animotron.expression.Expression;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import static org.animotron.graph.serializer.Serializer.PRETTY_ANIMO_RESULT;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */

@WebSocket
public class EvalAnimo {

    @OnWebSocketMessage
    public void onMessage(Session session, String data) {
        if (data.isEmpty())
            return;
        try {
            Expression e = new AnimoExpression(data);
            session.getRemote().sendString(PRETTY_ANIMO_RESULT.serialize(e, FileCache._));
        } catch (Throwable e) {
            //XXX: send error message
        }
    }
}