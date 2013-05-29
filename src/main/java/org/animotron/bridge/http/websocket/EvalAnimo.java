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
package org.animotron.bridge.http.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.animotron.expression.AnimoExpression;

import static org.animotron.bridge.http.HttpServer.CACHE;
import static org.animotron.graph.serializer.Serializer.PRETTY_ANIMO_RESULT;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */

public class EvalAnimo {

    public static void handle(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        if (frame.text().isEmpty())
            return;
        try {
            ctx.channel().write(
                    new TextWebSocketFrame(
                            PRETTY_ANIMO_RESULT.serialize(new AnimoExpression(frame.text()), CACHE)));
        } catch (Throwable e) {
            //XXX: send error message
        }
    }

}