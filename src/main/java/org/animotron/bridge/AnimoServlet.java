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
package org.animotron.bridge;

import org.animotron.AbstractExpression;
import org.animotron.exception.EBuilderTerminated;
import org.animotron.exception.ENotFound;
import org.animotron.graph.builder.CommonBuilder;
import org.animotron.graph.serializer.ResultSerializer;
import org.animotron.operator.AN;
import org.animotron.operator.relation.HAVE;
import org.animotron.operator.relation.USE;
import org.neo4j.graphdb.Relationship;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class AnimoServlet extends HttpServlet {

	private static final long serialVersionUID = 7276574723383015880L;

	private void writeResponse(Relationship r, HttpServletResponse res) throws IOException {
		res.setContentType("text/html");
		OutputStream out = res.getOutputStream();
		try {
            ResultSerializer.serialize(r, out);
        } catch (XMLStreamException e) {
			e.printStackTrace();
			throw new IOException(e);
        }
    }

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
	        AnimoRequest a = new AnimoRequest(req);
	        writeResponse(a, res);
		} catch (EBuilderTerminated e) {
			throw new IOException(e);
		}
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Relationship r = null;
        try {
            r = CommonBuilder.build(req.getInputStream(), req.getRequestURI());
        } catch (EBuilderTerminated e) {
            throw new ServletException(e);
        }
        writeResponse(r, res);
	}
	
	private class AnimoRequest extends AbstractExpression {

        private void makeRequest (HttpServletRequest req) {

            Enumeration names = req.getParameterNames();

            while (names.hasMoreElements()) {

                String name = (String) names.nextElement();
                String[] parts = name.split(":");

                if (parts.length > 1) {
                    if (USE._.name().equals(parts[0])) {
                        start(USE._, parts[1]);
                    } else {
                        start(HAVE._, parts[1]);
                    }
                } else {
                    start(HAVE._, name);
                }
                for  (String value : req.getParameterValues(name)) {
                    start(value);
                    end();
                }
                end();
            }

            start(HAVE._, "host");
                start(req.getServerName());
                end();
            end();

        }

		public AnimoRequest(HttpServletRequest req) throws EBuilderTerminated {

            super(false);

            try {

                startGraph();

                    start(AN._, "rest");

                        String uri = req.getRequestURI();
                        String[] parts = uri.split("/");

                        boolean isRoot = true;
                        for (String part : parts) {
                            if (part.isEmpty()) continue;
                            start(USE._, part);
                            end();
                            isRoot = false;
                        }

                        if (isRoot) {
                            start(USE._, "root");
                            end();
                        }

                        makeRequest(req);

                    end();

                endGraph();

            } catch (ENotFound e) {

                startGraph();
                    start(AN._, "rest");
                        start(USE._, "not-found");
                        end();
                        makeRequest(req);
                    end();
                endGraph();

            }

		}

	}
	
}
