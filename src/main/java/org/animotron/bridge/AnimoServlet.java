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
import org.animotron.Expression;
import org.animotron.Statement;
import org.animotron.exception.AnimoException;
import org.animotron.exception.EBuilderTerminated;
import org.animotron.exception.ENotFound;
import org.animotron.graph.builder.CommonBuilder;
import org.animotron.graph.handler.BinaryGraphHandler;
import org.animotron.graph.serializer.ResultSerializer;
import org.animotron.graph.serializer.StringResultSerializer;
import org.animotron.graph.traverser.GraphAnimoResultTraverser;
import org.animotron.io.PipedInput;
import org.animotron.manipulator.Evaluator;
import org.animotron.operator.AN;
import org.animotron.operator.THE;
import org.animotron.operator.query.GET;
import org.animotron.operator.relation.HAVE;
import org.animotron.operator.relation.USE;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import static org.animotron.Expression._;
import static org.animotron.Properties.BIN;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class AnimoServlet extends HttpServlet {

	private static final long serialVersionUID = 7276574723383015880L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		try {
	        writeResponse(new AnimoRequest(req), res);
        } catch (ENotFound e) {
            try {
                writeResponse(new AnimoNotFound(req), res);
            } catch (AnimoException eBuilderTerminated) {
                throw new IOException(e);
            }
        } catch (AnimoException e) {
			throw new IOException(e);
		}
		
		System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Relationship r = null;
        try {
            r = CommonBuilder.build(req.getInputStream(), req.getRequestURI());
            writeResponse(r, res);
        } catch (AnimoException e) {
            throw new ServletException(e);
        }
	}
	
	private void writeResponse(Relationship r, HttpServletResponse res) throws IOException, AnimoException {
        WebSerializer.serialize(r, res);
    }

    private class AnimoRequest extends AbstractExpression {

        public AnimoRequest(HttpServletRequest req) throws AnimoException {

            super(false);

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

                    Enumeration<?> names = req.getParameterNames();

                    while (names.hasMoreElements()) {

                        String name = (String) names.nextElement();
                        parts = name.split(":");

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
                    start(HAVE._, "uri");
                        start(req.getRequestURI());
                        end();
                    end();

                end();

            endGraph();

        }

    }

    private static class AnimoNotFound extends AbstractExpression {

        public AnimoNotFound(HttpServletRequest req) throws AnimoException {

            startGraph();
                start(AN._, "rest");
                    start(USE._, "not-found");
                    end();
                    start(HAVE._, "host");
                        start(req.getServerName());
                        end();
                    end();
                    start(HAVE._, "uri");
                        start(req.getRequestURI());
                        end();
                    end();
                end();
            endGraph();

        }

    }

    private static class WebSerializer {

        public static void serialize(final Relationship r, HttpServletResponse res) throws IOException, AnimoException {

            final OutputStream out = res.getOutputStream();

            try {

                Relationship mime = (Relationship) get(r, "mime-type");
                Relationship content = (Relationship) get(r, "content");
                String mimes = StringResultSerializer.serialize(r, mime);

                if (content != null) {

                    res.setContentType(mimes == null ? "application/xml" :mimes);
                    ResultSerializer.serialize(r, content, out);

                } else {

                    res.setContentType(mimes == null ? "application/octet-stream" :mimes);

                    final boolean[] isNotFound = {true};

                    GraphAnimoResultTraverser._.traverse(
                        new BinaryGraphHandler(out){
                            @Override
                            public void start(Statement statement, Relationship r) throws IOException {
                                Node n = r.getEndNode();
                                if (BIN.has(n)) {
                                    isNotFound[0] = false;
                                    write(n, out);
                                }
                            }
                        }, r
                    );

                    if (isNotFound[0])
                         throw new AnimoException(null, "Resource not found"); //TODO: replace null by ?
                }

            } catch (ENotFound e) {
                throw e;
            } catch (EBuilderTerminated e) {
                new IOException(e);
            }


        }

        private static Relationship get(Relationship r, String have) throws IOException, AnimoException {
            Expression get =  new Expression(
                _(GET._, have,
                    _(AN._, THE._.name(r))
                )
            );
            PipedInput pipe = Evaluator._.execute(r, get);
            for (Object o : pipe) {
                pipe.close();
                return (Relationship) o;
            }
            return null;
        };


    }
	
}
