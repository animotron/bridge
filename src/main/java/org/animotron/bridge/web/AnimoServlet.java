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
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;
import org.animotron.statement.relation.USE;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.animotron.bridge.web.WebSerializer.serialize;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class AnimoServlet extends HttpServlet {

	private static final long serialVersionUID = -7842813965460705795L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
        try {
            serialize(new AnimoRequest(req), res);
        } catch (Exception e) {
            ErrorHandler.doGet(req, res, ErrorHandler.NOT_FOUND);
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
	}

    protected static class AnimoRequest extends AbstractRequestExpression {

        private static final int MAX_PARTS_COUNT = 12;
        private List<String> list = new ArrayList<String>(MAX_PARTS_COUNT);
        private static final String ROOT = "root";

        public AnimoRequest(HttpServletRequest req) throws Exception {
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
        public void request() throws AnimoException, IOException {
            if (list.isEmpty()) {
                builder.start(AN._);
                    builder._(REF._, HTML_PAGE);
                    builder.start(USE._);
                        builder._(REF._, ROOT);
                    builder.end();
                    builder.start(ANY._);
                        builder._(REF._, RESOURCE);
                    builder.end();
                builder.end();
            } else {
                builder.start(AN._);
                    builder._(REF._, list.get(0));
                    if (list.size() > 1) {
                        for (int i = 1; i < list.size() - 1; i++) {
                            builder.start(USE._);
                                builder._(REF._, list.get(i));
                            builder.end();
                        }
                        builder.start(AN._);
                            builder._(REF._, list.get(list.size() - 1));
                        builder.end();
                    }
                builder.end();
            }
        }

    }

}
