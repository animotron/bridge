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
import org.animotron.expression.JExpression;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.query.ANY;
import org.animotron.statement.query.GET;
import org.neo4j.graphdb.Relationship;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

import static org.animotron.expression.JExpression._;
import static org.animotron.expression.JExpression.value;
import static org.animotron.graph.serializer.CachedSerializer.*;
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

    public static void serialize(Expression request, HttpServletResponse res) throws Throwable, ENotFound {
        serialize(request, res, uuid().toString());
    }

    public static void serialize(Expression request, HttpServletResponse res, String uuid) throws Throwable, ENotFound {
        String mime = mime(request);
        if (mime.isEmpty()) {
            throw new ENotFound(request);
        } else {
            res.setCharacterEncoding("UTF-8");
            res.setContentType(mime);
            res.setContentLength(-1);
            CachedSerializer cs =
                mime.equals("text/html") ? HTML : mime.endsWith("xml") ? XML : STRING;
            OutputStream os = res.getOutputStream();
            cs.serialize(request, os, CACHE, uuid);
            os.close();
        }
    }

    public static  String mime (Relationship r) throws Throwable {
        return STRING.serialize(
            new JExpression(
                    _(GET._, TYPE, _(GET._, MIME_TYPE, _(r)))
            ),
            CACHE
        );
    }

    public static String mime(String ext) throws Throwable {
        return STRING.serialize(
                new JExpression(
                        _(GET._, TYPE, _(ANY._, MIME_TYPE, _(WITH._, EXTENSION, value(ext))))
                ),
                CACHE
        );
    }

}
