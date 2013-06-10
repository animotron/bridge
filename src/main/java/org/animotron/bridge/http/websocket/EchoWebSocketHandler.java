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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import javolution.util.FastList;

import java.util.List;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */

public class EchoWebSocketHandler extends WebSocketHandler<WebSocketFrame> {

    public EchoWebSocketHandler(String protocol) {
        super(protocol);
    }

    private static List<Channel> set = FastList.newInstance();

    @Override
    public void handle(WebSocketServerHandshaker hs, ChannelHandlerContext ctx, WebSocketFrame frame) {
        for (Channel s : set) {
            s.write(frame.retain());
        }
    }

    @Override
    public void open(WebSocketServerHandshaker hs, ChannelHandlerContext ctx) {
        set.add(ctx.channel());
    }

    @Override
    public void close(WebSocketServerHandshaker hs, ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
        super.close(hs, ctx, frame);
        set.remove(ctx.channel());
    }

}