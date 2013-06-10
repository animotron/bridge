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
import org.animotron.bridge.http.helper.HttpErrorHelper;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class HttpServerHandler extends ChannelInboundMessageHandlerAdapter<FullHttpRequest> {

    private static HttpHandler[] handlers;

    public HttpServerHandler(HttpHandler[] handlers) {
        this.handlers = handlers;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            if (request.getDecoderResult().isSuccess()) {
                for (HttpHandler handler : handlers)
                    if (handler.handle(ctx, request)) return;
                HttpErrorHelper.handle(ctx, request, NOT_FOUND);
            }
            HttpErrorHelper.handle(ctx, request, BAD_REQUEST);
        } catch (Throwable t) {
            HttpErrorHelper.handle(ctx, request, t);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}