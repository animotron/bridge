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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.animotron.bridge.http.helper.HttpErrorHelper;
import org.animotron.bridge.http.websocket.WebSocketHandler;
import org.animotron.bridge.http.websocket.WebSocketServerHandler;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.getHeader;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.UPGRADE_REQUIRED;
import static io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse;
import static org.animotron.bridge.http.helper.HttpHelper.sendStatus;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebSocketUpgradeHttpHandler implements HttpHandler {

    private final String uriContext;
    private WebSocketHandler[] handlers;

    public WebSocketUpgradeHttpHandler(String uriContext, WebSocketHandler[] handlers) {
        this.uriContext = uriContext;
        this.handlers = handlers;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable {
        if (!request.getUri().equals(uriContext))
            return false;
        if (!request.getMethod().equals(GET)) {
            HttpErrorHelper.handle(ctx, request, METHOD_NOT_ALLOWED);
            return true;
        }
        if (!"websocket".equals(getHeader(request, UPGRADE))) {
            sendStatus(ctx, UPGRADE_REQUIRED);
            return true;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(request), getProtocol(request), false);
        WebSocketServerHandshaker hs = wsFactory.newHandshaker(request);
        if (hs == null) {
            sendUnsupportedWebSocketVersionResponse(ctx.channel());
            return true;
        }
        WebSocketHandler handler = selectHandler(getProtocol(request));
        if (handler == null) {
            hs.handshake(ctx.channel(), request);
            hs.close(ctx.channel(), new CloseWebSocketFrame());
            return true;
        }
        handler.open(hs, ctx);
        ctx.pipeline().removeLast();
        ctx.pipeline().addLast(new WebSocketServerHandler(handler, hs));
        hs.handshake(ctx.channel(), request);
        return true;
	}

    private WebSocketHandler selectHandler() {
        for (WebSocketHandler handler : handlers)
            if (handler.protocol == null) return handler;
        return null;
    }

    private WebSocketHandler selectHandler(String protocol) {
        if (protocol == null) return selectHandler();
        for (WebSocketHandler handler : handlers)
            if (protocol.equals(handler.protocol)) return handler;
        return null;
    }


    private String getProtocol(FullHttpRequest request){
        return getHeader(request, SEC_WEBSOCKET_PROTOCOL);
    }

    private String getWebSocketLocation(FullHttpRequest request) {
        return "ws://" + getHeader(request, HOST) + request.getUri();
    }

}
