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

import org.animotron.cache.Cache;
import org.animotron.cache.FileCache;
import org.animotron.exception.ENotFound;
import org.animotron.expression.Expression;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;
import org.animotron.statement.query.GET;
import org.neo4j.graphdb.Relationship;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.animotron.graph.serializer.Serializer.STRING;
import static org.animotron.utils.MessageDigester.uuid;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebSerializer {

    public static final String TYPE = "type";
    public static final String MIME_TYPE = "mime-type";
    public static final String EXTENSION = "extension";

    private static Cache CACHE = FileCache._;

    public static void serialize(Expression request, FullHttpResponse res) throws Throwable, ENotFound {
        serialize(request, res, uuid().toString());
    }

    public static void serialize(Expression request, FullHttpResponse res, String uuid) throws Throwable, ENotFound {
        String mime = mime(request);
        if (mime.isEmpty()) {
            throw new ENotFound(request);
        } else {
        	//res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        	res.headers().set(CONTENT_TYPE, mime+"; charset=UTF-8");
        	
            //res.setContentLength(-1);
        	
        	StringBuilder os = new StringBuilder();
            //OutputStream os = res.getOutputStream();

            STRING.serialize(request, os, CACHE, uuid);
            
            res.content().writeBytes(Unpooled.copiedBuffer(os, CharsetUtil.UTF_8));
            //os.close();
        }
    }

    public static  String mime (final Relationship r) throws Throwable {
        return STRING.serialize(
                new Expression() {
                    @Override
                    public void build() throws Throwable {
                        builder.start(GET._);
                            builder._(REF._,  TYPE);
                            builder.start(GET._);
                                builder._(REF._, MIME_TYPE);
                                builder.bind(r);
                            builder.end();
                        builder.end();
                    }
                },
                CACHE
        );
    }

    public static String mime(final String ext) throws Throwable {
        return STRING.serialize(
                new Expression() {
                    @Override
                    public void build() throws Throwable {
                        builder.start(GET._);
                            builder._(REF._,  TYPE);
                            builder.start(ANY._);
                                builder._(REF._, MIME_TYPE);
                                builder.start(WITH._);
                                    builder._(REF._, EXTENSION);
                                    builder._(ext);
                                builder.end();
                            builder.end();
                        builder.end();
                    }
                },
                CACHE
        );
    }

}
