/*
 *  Copyright (C) 2012-2013 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animi.
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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import org.animotron.exception.ENotFound;
import org.animotron.expression.Expression;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;
import static org.animotron.bridge.http.HttpServer.CACHE;
import static org.animotron.bridge.http.Mime.check;
import static org.animotron.graph.serializer.Serializer.STRING;
import static org.animotron.utils.MessageDigester.uuid;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class HttpHandler {

	private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);

    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
    }

    protected static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse response) {
        setContentLength(response);
        ChannelFuture f = ctx.channel().write(response);
        if (!isKeepAlive(req) || response.getStatus().code() != OK.code()) {
            f.addListener(CLOSE);
        }
    }

    protected static void sendStatus(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        setContentLength(response);
        setDate(response);
        ctx.write(response).addListener(CLOSE);
    }

    protected static void setDate(FullHttpResponse response) {
        setDateHeader(response, DATE, System.currentTimeMillis());
    }

    protected static void setLastModified(FullHttpResponse response, long time) {
        setDateHeader(response, LAST_MODIFIED, time);
    }

    private static void setDateHeader(FullHttpResponse response, String header, long time) {
        setHeader(response, header, formatDate(time));
    }

    private static void setContentLength(FullHttpResponse response) {
        HttpHeaders.setContentLength(response, response.content().readableBytes());
    }

    protected static String formatDate(long time) {
        return dateFormatter.format(new Date(time));
    }

    protected static Date parseDate(String time) {
        if (time == null) return null;
        if (time.isEmpty()) return null;
        try {
            return dateFormatter.parse(time);
        } catch (ParseException e) {
            return null;
        }
    }

    protected static boolean isSuccess(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable {
        if (request.getDecoderResult().isSuccess()) return true;
        ErrorHandler.handle(ctx, request, BAD_REQUEST);
        return false;
    }

    protected static boolean isAllowed(ChannelHandlerContext ctx, FullHttpRequest request, HttpMethod... method) throws Throwable {
        boolean allowed = true;
        for (HttpMethod m : method) {
            allowed &= request.getMethod() == m;
        }
        if (!allowed) ErrorHandler.handle(ctx, request, METHOD_NOT_ALLOWED);
        return allowed;
    }

    protected static void sendFile(final ChannelHandlerContext ctx, final FullHttpRequest request, final File file, String mime, String cache) throws Throwable {
        final RandomAccessFile raf = new RandomAccessFile(file, "r");
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        long modified = file.lastModified();
        Date since = parseDate(getHeader(request, IF_MODIFIED_SINCE));
        if (since != null && since.getTime() >= modified) {
            sendStatus(ctx, NOT_MODIFIED);
        } else {
            final long length = raf.length();
            setDate(response);
            setLastModified(response, modified);
            HttpHeaders.setContentLength(response, length);
            setHeader(response, CONTENT_TYPE, check(mime));
            setHeader(response, CACHE_CONTROL, cache);
            if (isKeepAlive(request)) {
                setHeader(response, CONNECTION, KEEP_ALIVE);
            }
            ctx.write(response);
            ChannelFuture writeFuture = ctx.write(new ChunkedFile(raf, 0, length, 8192));
            if (!isKeepAlive(request)) {
                writeFuture.addListener(CLOSE);
            }
        }
    }

    protected static void serialize(ChannelHandlerContext ctx, Expression e, FullHttpRequest request, FullHttpResponse response) throws Throwable {
        serialize(ctx, e, request, response, uuid().toString());
    }

    protected static void serialize(ChannelHandlerContext ctx, Expression e, FullHttpRequest request, FullHttpResponse response, String uuid) throws Throwable {
        String mime = "text/html";//mime(e);
        if (mime.isEmpty()) {
            throw new ENotFound(e);
        } else {
        	setHeader(response, CONTENT_TYPE, mime + "; charset=UTF-8");
            response.content().writeBytes(Unpooled.copiedBuffer(
                    STRING.serialize(e, CACHE, uuid), UTF_8));
            sendHttpResponse(ctx, request, response);
        }
    }
}
