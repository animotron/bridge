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
import org.animotron.expression.Expression;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.GET;
import org.animotron.statement.relation.USE;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static org.animotron.bridge.web.WebSerializer.serialize;
import static org.animotron.graph.Properties.RUUID;
import static org.animotron.utils.MessageDigester.uuid;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class AnimoServlet extends HttpServlet {

	private static final long serialVersionUID = -7842813965460705795L;
    
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Expression request = new AnimoRequest(req);
            String uuid = RUUID.has(request)? (String) RUUID.get(request) : uuid();
            long modified = uuid(uuid).getTime();
            res.setDateHeader("Last-Modified", modified);
            boolean isHTTP11 = req.getProtocol().endsWith("1.1");
            if (isHTTP11) {
                res.setHeader("ETag", uuid);
                Enumeration<String> etag = req.getHeaders("If-None-Match");
                while (etag.hasMoreElements()) {
                    if (uuid.equals(etag.nextElement())) {
                        res.setStatus(SC_NOT_MODIFIED);
                        return;
                    }
                }
            }
            long since = req.getDateHeader("If-Modified-Since");
            if (since < modified || since > System.currentTimeMillis()) {
                if (isHTTP11) {
                    res.setHeader("Cache-Control", "no-cache");
                }
                res.setDateHeader("Expires", modified);
                serialize(request, res, uuid);
            } else {
                res.setStatus(SC_NOT_MODIFIED);
            }
        } catch (Throwable t) {
            ErrorHandler.doRequest(req, res, t);
        }
	}

    protected static class AnimoRequest extends AbstractRequestExpression {

        private static final int MAX_PARTS_COUNT = 12;
        private List<String> list = new ArrayList<String>(MAX_PARTS_COUNT);
        private static final String ROOT = "root";

        public AnimoRequest(HttpServletRequest req) throws Throwable {
            super(req);
            String [] parts = req.getPathInfo().split("/");
            if (parts.length > MAX_PARTS_COUNT) {
                throw new ENotFound(this);
            }
            for (String part : parts) {
                if (!part.isEmpty()) {
                    if (list.contains(part)) {
                        throw new ENotFound(this);
                    }
                    list.add(part);
                }
            }
        }

        @Override
        public void context() throws AnimoException, IOException {
            if (list.isEmpty()) {
                builder.start(GET._);
                    builder._(REF._, ROOT);
                    //TODO
                    //builder.start(THIS._);
                    //    builder._(SITE);
                    //builder.end();
                builder.end();
            } else {
                builder._(REF._, list.get(0));
                if (list.size() > 1) {
                    builder.start(USE._);
                        for (int i = 1; i < list.size() - 1; i++) {
                            builder._(REF._, list.get(i));
                        }
                    builder.end();
                    builder.start(AN._);
                        builder._(REF._, list.get(list.size() - 1));
                    builder.end();
                }
            }
            // TODO add sorted request parametrs, headers, attributes, cookies and etc
        }

    }

}
