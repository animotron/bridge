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

import com.eaio.uuid.UUID;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.animotron.exception.AnimoException;
import org.animotron.exception.ENotFound;
import org.animotron.expression.Expression;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.relation.USE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.getHeader;
import static io.netty.handler.codec.http.HttpHeaders.setHeader;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.animotron.graph.Properties.RUUID;
import static org.animotron.utils.MessageDigester.getTime;
import static org.animotron.utils.MessageDigester.uuid;

/**
 * 
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 */
public class AnimoHandler extends HttpHandler {

	public static void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            if (!isSuccess(ctx, request)) return;
            if (!isAllowed(ctx, request, GET)) return;
            Expression e = new AnimoRequest(request);
            UUID uuid;
            String suuid;
            if (RUUID.has(e)) {
                suuid = (String) RUUID.get(e);
                uuid = uuid(suuid);
            } else {
                uuid = uuid();
                suuid = uuid.toString();
            }
            if (suuid.equals(getHeader(request, IF_NONE_MATCH))) {
                sendStatus(ctx, NOT_MODIFIED);
            } else {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
                setDate(response);
                setLastModified(response, getTime(uuid));
                setHeader(response, ETAG, suuid);
                setHeader(response, CACHE_CONTROL, "no-cache");
                serialize(ctx, e, request, response, suuid);
            }
        } catch (Throwable t) {
            ErrorHandler.handle(ctx, request, t);
        }
	}

	protected static class AnimoRequest extends AbstractRequestExpression {
		private static final int MAX_PARTS_COUNT = 12;
		private List<String> list = new ArrayList<String>(MAX_PARTS_COUNT);
		private static final String ROOT = "root";
		public AnimoRequest(FullHttpRequest req) throws Throwable {
			super(req);
			String[] parts = req.getUri().split("/");
			if (parts.length > MAX_PARTS_COUNT) {
				throw new ENotFound(this);
			}
			for (String part : parts) {
				if (!part.isEmpty()) {
					if (list.contains(part)) {
						throw new ENotFound(this);
					}
					list.add(part);
				}
			}
		}

		@Override
		protected void service() throws AnimoException, IOException {
			builder._(REF._, list.isEmpty() ? ROOT : list.get(0));
		}

		@Override
		protected void context() throws AnimoException, IOException {
			if (list.size() > 1) {
				builder.start(USE._);
				for (int i = 1; i < list.size() - 1; i++) {
					builder._(REF._, list.get(i));
				}
				builder.end();
				builder.start(AN._);
				builder._(REF._, list.get(list.size() - 1));
				builder.end();
			}
		}

	}
}
