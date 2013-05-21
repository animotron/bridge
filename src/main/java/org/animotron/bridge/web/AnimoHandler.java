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
package org.animotron.bridge.web;

import com.eaio.uuid.UUID;
import org.animotron.exception.AnimoException;
import org.animotron.exception.ENotFound;
import org.animotron.expression.Expression;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.relation.USE;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import static org.animotron.bridge.web.WebSerializer.serialize;
import static org.animotron.graph.Properties.RUUID;
import static org.animotron.utils.MessageDigester.getTime;
import static org.animotron.utils.MessageDigester.uuid;

/**
 * 
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 */
public class AnimoHandler extends AbstractHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg)
			throws Exception {

		if (!msg.getDecoderResult().isSuccess()) {
			sendError(ctx, BAD_REQUEST);
			return;
		}

		if (msg.getMethod() != GET) {
			sendError(ctx, METHOD_NOT_ALLOWED);
			return;
		}

		try {
			Expression request = new AnimoRequest(msg);
			UUID uuid;
			String suuid;
			if (RUUID.has(request)) {
				suuid = (String) RUUID.get(request);
				uuid = uuid(suuid);
			} else {
				uuid = uuid();
				suuid = uuid.toString();
			}

			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);

			long modified = getTime(uuid);
			response.headers().set(LAST_MODIFIED, modified);

			final HttpVersion version = msg.getProtocolVersion();
			boolean isHTTP11 = version.majorVersion() == 1 && version.minorVersion() == 1;
			if (isHTTP11) {
				response.headers().set(ETAG, suuid);
				List<String> etags = response.headers().getAll(IF_NONE_MATCH);
				for (String etag : etags) {
					if (suuid.equals(etag)) {
						sendNotModified(ctx);
						return;
					}
				}
			}
			long since = Long.valueOf( getHeader(msg, IF_MODIFIED_SINCE) );
			if (since < modified || since > System.currentTimeMillis()) {
				if (isHTTP11) {
					response.headers().set(CACHE_CONTROL, "no-cache");
				}
				HttpHeaders.setDateHeader(response, EXPIRES, new Date(modified));

				serialize(request, response, suuid);

				ctx.write(response).addListener(ChannelFutureListener.CLOSE);

			} else {
				sendNotModified(ctx);
			}
		} catch (Throwable t) {
			// XXX: ErrorHandler.doRequest(req, res, t);
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
