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
package org.animotron.bridge.web;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public abstract class AbstractHandler extends ChannelInboundMessageHandlerAdapter<FullHttpRequest> {

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;

	protected void sendError(final ChannelHandlerContext ctx, final HttpResponseStatus status) {
		FullHttpResponse response = 
			new DefaultFullHttpResponse(
				HTTP_1_1,
				status, 
				Unpooled.copiedBuffer(
					"Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8
				)
			);
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		// Close the connection as soon as the error message is sent.
		ctx.write(response).addListener(ChannelFutureListener.CLOSE);
	}

	protected void sendRedirect(final ChannelHandlerContext ctx, final String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
		response.headers().set(LOCATION, newUri);

		// Close the connection as soon as the error message is sent.
		ctx.write(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * When file timestamp is the same as what the browser is sending up, send a
	 * "304 Not Modified"
	 * 
	 * @param ctx
	 *            Context
	 */
	protected void sendNotModified(final ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
		setDateHeader(response);

		// Close the connection as soon as the error message is sent.
		ctx.write(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	/**
	 * Sets the Date header for the HTTP response
	 * 
	 * @param response
	 *            HTTP response
	 */
	private void setDateHeader(final FullHttpResponse response) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		Calendar time = new GregorianCalendar();
		response.headers().set(DATE, dateFormatter.format(time.getTime()));
	}

	protected boolean isHTTP11(final HttpVersion version) {
		return version.majorVersion() == 1 && version.minorVersion() == 1;
	}
}
