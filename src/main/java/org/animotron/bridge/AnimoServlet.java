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
import org.animotron.Executor;
import org.animotron.Expression;
import org.animotron.exception.EBuilderTerminated;
import org.animotron.exception.ENotFound;
import org.animotron.graph.builder.CommonBuilder;
import org.animotron.graph.handler.GraphHandler;
import org.animotron.graph.handler.PipedGraphHandler;
import org.animotron.graph.serializer.StringResultSerializer;
import org.animotron.graph.traverser.GraphAnimoResultTraverser;
import org.animotron.io.PipedInput;
import org.animotron.io.PipedOutput;
import org.animotron.operator.AN;
import org.animotron.operator.THE;
import org.animotron.operator.query.GET;
import org.animotron.operator.relation.HAVE;
import org.animotron.operator.relation.USE;
import org.neo4j.graphdb.Relationship;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Statement;
import java.util.Enumeration;

import static org.animotron.Expression._;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class AnimoServlet extends HttpServlet {

	private static final long serialVersionUID = 7276574723383015880L;

	private void writeResponse(Relationship r, HttpServletResponse res) throws IOException {
        WebSerializer.serialize(r, res);
    }

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		try {
	        AnimoRequest a = new AnimoRequest(req);
	        writeResponse(a, res);
		} catch (EBuilderTerminated e) {
			throw new IOException(e);
		}
		
		System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
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

                        Enumeration names = req.getParameterNames();

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

            } catch (ENotFound e) {

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

	}

    private static class WebSerializer {

        public static void serialize(final Relationship r, HttpServletResponse res) throws IOException {

            res.setContentType("text/html");
            OutputStream out = res.getOutputStream();

            try {

                String mime = getMimeType(r);

                final PipedInput pipe = new PipedInput();

                Executor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            GraphHandler handler = null;
                            try {
                                handler = new PipedGraphHandler(new PipedOutput(pipe));
                                GraphAnimoResultTraverser._.traverse(handler, r);
                            } catch (InterruptedException e) {
                                try {
                                    pipe.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                );

                for (Object o : pipe) {
                    Statement s = (Statement) o;
                    Relationship op = (Relationship) pipe.read();
                }

            } catch (EBuilderTerminated e) {
                new IOException(e);
            } catch (InterruptedException e) {
                new IOException(e);
            }


        }

        private static String getMimeType(Relationship r) throws EBuilderTerminated, InterruptedException {
            String m =  StringResultSerializer.serialize(
                new Expression(
                    _(GET._, "mime-type",
                        _(AN._, THE._.name(r))
                    )
                )
            );
            return "".equals(m) ? "aplication/animo+xml" : m;
        }

    }
	
}
