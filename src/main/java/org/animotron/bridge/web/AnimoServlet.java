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

import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

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

        protected static final String REST = "rest";

        public AnimoRequest(HttpServletRequest req) throws Exception {
            super(req);
        }

        @Override
        public void build() throws Exception {
            builder.start(AN._);
                builder._(REF._, REST);
                Enumeration<String> names = getRequest().getParameterNames();
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    builder.start(AN._);
                    builder._(REF._, name);
                    for  (String value : getRequest().getParameterValues(name)) {
                        builder._(value);
                    }
                    builder.end();
                }
                processRequest();
            builder.end();
        }

    }

}
