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
package org.animotron.bridge.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import org.animotron.bridge.http.websocket.*;

import static org.animotron.bridge.http.HttpServer.*;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class HttpServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static final String WS_URI = "/ws";

    private WebSocketServerHandshaker hs;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        } catch (Throwable t) {
            throw (Exception) t;
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable {
        String uri = request.getUri();
        if (uri.equals(WS_URI)) {
            hs = WebSocketUpgradeHandler.handle(ctx, request);
        }
        if (uri.startsWith(BINARY_CONTEXT_URI)) {
            ResourceBridgeHandler.handle(ctx, request, BINARY_CONTEXT_URI);
            return;
        }
        if (uri.startsWith(ANIMO_CONTEXT_URI)) {
            ResourceMapHandler.handle(ctx, request, ANIMO_CONTEXT_URI, ANIMO_FOLDER);
            return;
        }
        AnimoHandler.handle(ctx, request);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            frame.retain();
            hs.close(ctx.channel(), (CloseWebSocketFrame) frame);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            frame.content().retain();
            ctx.channel().write(new PongWebSocketFrame(frame.content()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(
                    String.format("%s frame types not supported", frame.getClass().getName()));
        }
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame text = (TextWebSocketFrame) frame;
            switch (hs.selectedSubprotocol()) {
                case "echo"     : Echo.handle(ctx, text);
                case "eval"     : EvalAnimo.handle(ctx, text);
                case "save"     : SaveAnimo.handle(ctx, text);
                case "search"   : SearchAnimo.handle(ctx, text);
                case "src"      : SourceAnimo.handle(ctx, text);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}