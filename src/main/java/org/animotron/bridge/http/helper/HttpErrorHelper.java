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
package org.animotron.bridge.http.helper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.animotron.exception.AnimoException;
import org.animotron.exception.ENotFound;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;

import java.io.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.setHeader;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.animotron.bridge.http.helper.HttpHelper.sendHttpResponse;
import static org.animotron.bridge.http.helper.HttpHelper.serialize;

/**
 * 
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 */
public class HttpErrorHelper {

	public static void handle(ChannelHandlerContext ctx, FullHttpRequest request, Throwable x) {
        try {
            if (x instanceof ENotFound || x instanceof FileNotFoundException) {
                handle(ctx, request, null, NOT_FOUND);
            } else {
                handle(ctx, request, x, INTERNAL_SERVER_ERROR);
            }
        } catch (Throwable t) {
        	FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
        	setHeader(response, CONTENT_TYPE, "text/plain; charset=UTF-8");
        	ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(os);
            t.printStackTrace(pw);
            response.content().writeBytes(os.toByteArray());
            sendHttpResponse(ctx, request, response);
        }
    }

    protected static void handle(ChannelHandlerContext ctx, FullHttpRequest request, Throwable x, HttpResponseStatus status) throws Throwable {
        serialize(ctx, new AnimoRequest(request, status, x), request, new DefaultFullHttpResponse(HTTP_1_1, status));
    }

    public static void handle(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) throws Throwable {
        handle(ctx, request, null, status);
    }

    private static class AnimoRequest extends AbstractRequestExpression {

        private static final String STACK_TRACE = "stack-trace";
        private static final String ERROR = "error";
        private static final String CODE = "code";
        private Throwable x;
        private int status;

        public AnimoRequest(FullHttpRequest req, HttpResponseStatus status, Throwable x) throws Throwable {
            super(req);
            this.status = status.code();
            this.x = x;
        }

        @Override
        protected void service() throws AnimoException, IOException {
            builder.start(ANY._);
                builder._(REF._, ERROR);
                builder.start(WITH._);
                    builder._(REF._, CODE);
                    builder._(status);
                builder.end();
            builder.end();
        }

        @Override
        protected void context() throws AnimoException, IOException {
            builder.start(AN._);
                builder._(REF._, CODE);
                builder._(status);
            builder.end();
            if (x != null) {
                builder.start(AN._);
                    builder._(REF._, STACK_TRACE);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    x.printStackTrace(pw);
                    BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
                    String s;
                    while ((s = br.readLine()) != null) {
                        builder._(s);
                    }
                builder.end();
            }
        }

    }

}
