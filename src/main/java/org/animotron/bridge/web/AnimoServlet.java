/*
 *  Copyright (C) 2011 The Animo Project
 *  http://animotron.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.animotron.bridge.web;

import org.animotron.exception.ENotFound;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.operator.THE;
import org.animotron.statement.relation.USE;
import org.neo4j.graphdb.Node;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import static org.animotron.bridge.web.WebSerializer.serialize;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class AnimoServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		try {
	        serialize(new AnimoRequest(req), res);
        } catch (ENotFound e) {
            try {
                ErrorHandler.doGet(req, res, ErrorHandler.NOT_FOUND);
            } catch (Exception eBuilderTerminated) {
                e.printStackTrace();
                throw new IOException(e);
            }
		} catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
	}

    protected static class AnimoRequest extends AbstractRequestExpression {

        protected static final Node REST = THE._("rest");
        protected static final Node ROOT = THE._("root");

        public AnimoRequest(HttpServletRequest req) throws Exception {
            super(req);
        }

        @Override
        public void build() throws Exception {
            builder.start(AN._);
                builder._(REF._, REST);
                String uri = getRequest().getRequestURI();
                boolean isRoot = true;
                for (String part : uri.split(Pattern.quote("/"))) {
                    if (part.isEmpty()) continue;
                    String[] parts = part.split(Pattern.quote("."));
                    if (parts.length > 0) {
                        for(String sub : parts) {
                            if (sub.isEmpty()) continue;
                            builder._(USE._, sub);
                        }
                    } else {
                        builder._(USE._, part);
                    }
                    isRoot = false;
                }
                if (isRoot) {
                    builder._(USE._, ROOT);
                }
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
