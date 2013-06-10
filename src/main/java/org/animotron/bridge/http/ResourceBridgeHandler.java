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
import org.animotron.bridge.http.helper.ErrorHandlerHelper;
import org.animotron.exception.ENotFound;
import org.animotron.expression.BinaryExpression;
import org.animotron.statement.operator.DEF;
import org.neo4j.graphdb.Relationship;

import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static org.animotron.bridge.http.helper.HttpHandlerHelper.sendFile;
import static org.animotron.bridge.http.helper.MimeHelper.mime;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ResourceBridgeHandler implements HttpHandler {

    private String uriContext;
	
	private final static long MAX_AGE = 31536000;

    public ResourceBridgeHandler (String uriContext) {
        this.uriContext = uriContext;
    }

    @Override
	public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable{
        if (!request.getUri().startsWith(uriContext))
            return false;
        if (!request.getMethod().equals(GET)) {
            ErrorHandlerHelper.handle(ctx, request, METHOD_NOT_ALLOWED);
            return true;
        }
        QueryStringDecoder uri = new QueryStringDecoder(request.getUri());
        String id = uri.path().replaceFirst(Pattern.quote(uriContext), "");
        Relationship r = DEF._.get(id);
        if (r == null) {
            throw new ENotFound(null);
        }
        sendFile(ctx, request, BinaryExpression.getFile(id), mime(r), "private, max-age=" + MAX_AGE);
        return true;
    }

}
