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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.File;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class MappedFileHandler extends BridgeHandler {

	private File folder;

    public MappedFileHandler(String uri) {
        this.folder = new File(uri);
    }
    
    private String mime(File file) throws Throwable {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index > 0) {
            return WebSerializer.mime(name.substring(index + 1));
        }
        return "application/octet-stream";
    }


	@Override
	public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

		if (!msg.getDecoderResult().isSuccess()) {
			sendError(ctx, BAD_REQUEST);
			return;
		}

		if (msg.getMethod() != GET) {
			sendError(ctx, METHOD_NOT_ALLOWED);
			return;
		}

		File file = new File(folder, msg.getUri());
		
		String mime;
		try {
			mime = mime(file);
		} catch (Throwable e) {
			sendError(ctx, UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		sendFile(ctx, msg, file, mime);
	}
}