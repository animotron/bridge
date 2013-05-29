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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import org.animotron.bridge.http.websocket.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.getHeader;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
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

        if (!request.getDecoderResult().isSuccess()) {
            HttpHandler.sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if (WS_URI.equals(request.getUri())) {
            if (request.getMethod() != GET) {
                HttpHandler.sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }
            if (!"websocket".equals(getHeader(request, UPGRADE))) {
                HttpHandler.sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, UPGRADE_REQUIRED));
                return;
            }
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(request), getHeader(request, SEC_WEBSOCKET_PROTOCOL), false);
            hs = wsFactory.newHandshaker(request);
            if (hs == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            } else {
                hs.handshake(ctx.channel(), request);
            }
            return;
        }

        String uri = request.getUri();

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

        // Check for closing frame
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

    private static String getWebSocketLocation(FullHttpRequest request) {
        return "ws://" + getHeader(request, HOST) + WS_URI;
    }
}