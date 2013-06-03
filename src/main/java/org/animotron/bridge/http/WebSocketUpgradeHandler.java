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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.animotron.exception.ENotFound;
import org.animotron.expression.BinaryExpression;
import org.animotron.statement.operator.DEF;
import org.neo4j.graphdb.Relationship;

import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL;
import static io.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static io.netty.handler.codec.http.HttpHeaders.getHeader;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.UPGRADE_REQUIRED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse;
import static org.animotron.bridge.http.Mime.mime;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebSocketUpgradeHandler extends HttpHandler {

    public static WebSocketServerHandshaker handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable {
        if (!isSuccess(ctx, request)) return null;
        if (!isAllowed(ctx, request, GET)) return null;
        if (!"websocket".equals(getHeader(request, UPGRADE))) {
            sendStatus(ctx, UPGRADE_REQUIRED);
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(request), getHeader(request, SEC_WEBSOCKET_PROTOCOL), false);
        WebSocketServerHandshaker hs = wsFactory.newHandshaker(request);
        if (hs == null) {
            sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            hs.handshake(ctx.channel(), request);
        }
        return hs;
	}

    private static String getWebSocketLocation(FullHttpRequest request) {
        return "ws://" + getHeader(request, HOST) + request.getUri();
    }


}
