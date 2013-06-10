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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.animotron.expression.AnimoExpression;

import static org.animotron.bridge.http.HttpServer.CACHE;
import static org.animotron.graph.serializer.Serializer.PRETTY_ANIMO;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class SaveAnimo extends WebSocketHandler<TextWebSocketFrame> {

    public SaveAnimo(String protocol) {
        super(protocol);
    }

    @Override
    public void handle(WebSocketServerHandshaker hs, ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String exp = frame.text();
        if (exp.isEmpty()) return;
        try {
            ctx.channel().write(
                    new TextWebSocketFrame(
                            PRETTY_ANIMO.serialize(new AnimoExpression(exp), CACHE)));
        } catch (Throwable e) {
            //XXX: send error message
        }
    }

}
