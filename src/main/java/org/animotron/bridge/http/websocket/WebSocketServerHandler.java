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
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebSocketServerHandler extends ChannelInboundMessageHandlerAdapter<WebSocketFrame>{

    private WebSocketHandler handler;
    private WebSocketServerHandshaker hs;

    public WebSocketServerHandler(WebSocketHandler handler, WebSocketServerHandshaker hs) {
        this.handler = handler;
        this.hs = hs;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        try {
            if (frame instanceof CloseWebSocketFrame) {
                frame.retain();
                handler.close(hs, ctx, (CloseWebSocketFrame) frame);
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                handler.ping(hs, ctx, (PingWebSocketFrame) frame);
                return;
            }
            if (frame instanceof WebSocketFrame) {
                handler.handle(hs, ctx, frame);
            }
        } catch (Throwable t) {
            throw (Exception) t;
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}