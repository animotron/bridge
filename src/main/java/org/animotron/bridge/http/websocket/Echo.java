/*
 *  Copyright (C) 2011-2013 The Animo Project
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
package org.animotron.bridge.http.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */

public class Echo {

//    private static List<Session> set = FastList.newInstance();

    public static void handle(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //To change body of created methods use File | Settings | File Templates.
    }

//
//    @OnWebSocketMessage
//    public void onMessage(Session session, String data) {
//        if (data.isEmpty())
//            return;
//        try {
//            for (Session s : set) {
//                s.getRemote().sendString(data);
//            }
//        } catch (Throwable e) {
//            //XXX: send error message
//        }
//    }
//
//    @OnWebSocketConnect
//    public void onConnect (Session session) {
//        set.add(session);
//    }
//
//    @OnWebSocketClose
//    public void onClose (Session session, int status, String reason) {
//        set.remove(session);
//    }
//

}