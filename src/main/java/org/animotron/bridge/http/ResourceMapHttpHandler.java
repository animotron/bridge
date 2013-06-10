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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.animotron.bridge.http.helper.HttpErrorHelper;

import java.io.File;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static org.animotron.bridge.http.helper.HttpHelper.sendFile;
import static org.animotron.bridge.http.helper.MimeHelper.mime;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ResourceMapHttpHandler implements HttpHandler {

    private String uriContext;
    private File folder;

    public ResourceMapHttpHandler(String uriContext, File folder){
        this.uriContext = uriContext;
        this.folder = folder;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable {
        if (!request.getUri().startsWith(uriContext))
            return false;
        if (!request.getMethod().equals(GET)) {
            HttpErrorHelper.handle(ctx, request, METHOD_NOT_ALLOWED);
            return true;
        }
        QueryStringDecoder uri = new QueryStringDecoder(request.getUri());
        File file = new File(folder, uri.path().replaceFirst(Pattern.quote(uriContext), ""));
        sendFile(ctx, request, file, mime(file), "no-cache");
        return true;
    }

}