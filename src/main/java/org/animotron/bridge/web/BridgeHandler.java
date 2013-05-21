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
package org.animotron.bridge.web;

import org.animotron.exception.ENotFound;
import org.animotron.expression.BinaryExpression;
import org.animotron.statement.operator.DEF;
import org.neo4j.graphdb.Relationship;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import static org.animotron.bridge.web.WebSerializer.mime;
import static org.animotron.utils.MessageDigester.uuid;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class BridgeHandler extends AbstractHandler {
	
	private final static long MAX_AGE = 365 * 24 * 60 * 60;
	private final static long EXPIRES_IN = 365 * 24 * 60 * 60 * 1000;

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

        InputStream is = null;
        try {

            String id = msg.getUri().substring(1);

            Relationship r = DEF._.get(id);
            if (r == null) {
                throw new ENotFound(null);
            }

            File file = BinaryExpression.getFile(id);

            String mime = mime(r);
            mime = mime.isEmpty() ? "application/octet-stream" : mime;
            
            sendFile(ctx, msg, file, mime);
            
        } catch (Throwable t) {
            //XXX: ErrorHandler.doRequest(req, res, t);
        } finally {
            if (is != null) is.close();
        }
	}
	
	protected void sendFile(final ChannelHandlerContext ctx, final FullHttpRequest msg, final File file, final String mime) throws IOException {
		final RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException fnfe) {
			sendError(ctx, NOT_FOUND);
			return;
		}
		final long fileLength = raf.length();

		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);

		long modified = file.lastModified();
        setDateHeader(res, LAST_MODIFIED, new Date(modified));
        
        boolean isHTTP11 = isHTTP11(msg.getProtocolVersion());
        if (isHTTP11) {
            String hash = uuid().toString();
            res.headers().add(ETAG, hash);
            List<String> etag = msg.headers().getAll(IF_NONE_MATCH);
            if (!etag.isEmpty()) {
            	sendNotModified(ctx);
                return;
            }
        }
        long since = Long.valueOf( msg.headers().get(IF_MODIFIED_SINCE) );
        long time = System.currentTimeMillis();
        if (since < modified || since > time) {
        	setContentLength(res, fileLength);
        	if (isHTTP11) {
                res.headers().set(CACHE_CONTROL, "public, max-age=" + MAX_AGE);
            }
            setDateHeader(msg, EXPIRES, new Date(time + EXPIRES_IN));
            res.headers().set(CONTENT_TYPE, mime);

    		if (isKeepAlive(msg)) {
    			res.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
    		}

    		// Write the initial line and the header.
    		ctx.write(res);

    		// Write the content.
    		ChannelFuture writeFuture = ctx.write(new ChunkedFile(raf, 0, fileLength, 8192));

    		// Decide whether to close the connection or not.
    		if (!isKeepAlive(msg)) {
    			// Close the connection when the whole content is written out.
    			writeFuture.addListener(ChannelFutureListener.CLOSE);
    		}
        } else {
        	sendNotModified(ctx);
        }
	}

	/**
	 * Sets the Date and Cache headers for the HTTP Response
	 * 
	 * @param response
	 *            HTTP response
	 * @param fileToCache
	 *            file to extract content type
	 */
	private void setDateAndCacheHeaders(final HttpResponse response, final File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.headers().set(DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
		response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
		response.headers().set(LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
	}

}
