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

import org.animotron.exception.AnimoException;
import org.animotron.exception.ENotFound;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.animotron.bridge.web.WebSerializer.serialize;

/**
 * 
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 */
public class ErrorHandler {

	public static void messageReceived(Throwable x, ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        long startTime = System.currentTimeMillis();
        HttpResponseStatus status = INTERNAL_SERVER_ERROR;
        try {
            if (x instanceof ENotFound || x instanceof FileNotFoundException) {
            	
            	FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);
                status =  NOT_FOUND;
            	res.setStatus(status);
                serialize(new AnimoRequest(msg, NOT_FOUND.code(), null), res);
            } else {
            	FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);
                res.setStatus(status);
                serialize(new AnimoRequest(msg, INTERNAL_SERVER_ERROR.code(), x), res);
            }
        } catch (Throwable t) {
        	FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);
            res.setStatus(status);
        	res.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        	ByteArrayOutputStream os = new ByteArrayOutputStream();

            PrintWriter pw = new PrintWriter(os);
            t.printStackTrace(pw);

            res.content().writeBytes(os.toByteArray());
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
    }

    private static class AnimoRequest extends AbstractRequestExpression {

        private static final String STACK_TRACE = "stack-trace";
        private static final String ERROR = "error";
        private static final String CODE = "code";
        private Throwable x;
        private int status;

        public AnimoRequest(FullHttpRequest req, int status, Throwable x) throws Throwable {
            super(req);
            this.status = status;
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
