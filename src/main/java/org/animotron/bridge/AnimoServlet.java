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

import org.animotron.exception.AnimoException;
import org.animotron.exception.EBuilderTerminated;
import org.animotron.exception.ENotFound;
import org.animotron.expression.AbstractExpression;
import org.animotron.expression.CommonExpression;
import org.animotron.expression.Expression;
import org.animotron.expression.JExpression;
import org.animotron.graph.builder.FastGraphBuilder;
import org.animotron.graph.handler.BinaryGraphHandler;
import org.animotron.graph.serializer.StringResultSerializer;
import org.animotron.graph.serializer.XMLResultSerializer;
import org.animotron.graph.traverser.AnimoResultTraverser;
import org.animotron.io.PipedInput;
import org.animotron.manipulator.Evaluator;
import org.animotron.manipulator.PFlow;
import org.animotron.statement.Statement;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.THE;
import org.animotron.statement.query.GET;
import org.animotron.statement.relation.HAVE;
import org.animotron.statement.relation.USE;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import static org.animotron.Properties.BIN;
import static org.animotron.expression.JExpression._;

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
            } catch (Exception eBuilderTerminated) {
                throw new IOException(e);
            }
		} catch (Exception e) {
            throw new IOException(e);
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Relationship r = null;
        try {
            r = new CommonExpression(req.getInputStream(), req.getRequestURI());
            writeResponse(r, res);
        } catch (Exception e) {
            throw new ServletException(e);
        }
	}
	
	private void writeResponse(Relationship r, HttpServletResponse res) throws Exception {
        WebSerializer.serialize(r, res);
    }

    private abstract class RequestExpression extends AbstractExpression {
        protected final HttpServletRequest req;
        public RequestExpression(HttpServletRequest req) throws AnimoException, IOException {
            super(new FastGraphBuilder());
            this.req = req;
        }
    }

    private class AnimoRequest extends RequestExpression {

        public AnimoRequest(HttpServletRequest req) throws AnimoException, IOException {
            super(req);
        }

        @Override
        public void build() throws Exception {
            
            builder.start(AN._, "rest");

                String uri = req.getRequestURI();
                String[] parts = uri.split("/");

                boolean isRoot = true;
                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    builder.start(USE._, part);
                    builder.end();
                    isRoot = false;
                }

                if (isRoot) {
                    builder.start(USE._, "root");
                    builder.end();
                }

                Enumeration<?> names = req.getParameterNames();

                while (names.hasMoreElements()) {

                    String name = (String) names.nextElement();
                    parts = name.split(":");

                    if (parts.length > 1) {
                        if (USE._.name().equals(parts[0])) {
                            builder.start(USE._, parts[1]);
                        } else {
                            builder.start(HAVE._, parts[1]);
                        }
                    } else {
                        builder.start(HAVE._, name);
                    }
                    for  (String value : req.getParameterValues(name)) {
                        builder.start(value);
                        builder.end();
                    }
                    builder.end();
                }

                builder.start(HAVE._, "host");
                    builder.start(req.getServerName());
                    builder.end();
                builder.end();
                builder.start(HAVE._, "uri");
                    builder.start(req.getRequestURI());
                    builder.end();
                builder.end();

            builder.end();
        }
    }

    private class AnimoNotFound extends RequestExpression {

        public AnimoNotFound(HttpServletRequest req) throws AnimoException, IOException {
            super(req);
        }

        @Override
        public void build() throws Exception {
            builder.start(AN._, "rest");
                builder.start(USE._, "not-found");
                builder.end();
                builder.start(HAVE._, "host");
                    builder.start(req.getServerName());
                    builder.end();
                builder.end();
                builder.start(HAVE._, "uri");
                    builder.start(req.getRequestURI());
                    builder.end();
                builder.end();
            builder.end();
        }
    }

    private static class WebSerializer {

        public static void serialize(final Relationship r, HttpServletResponse res) throws Exception {

            final OutputStream out = res.getOutputStream();

            try {

                Relationship mime =  get(r, "mime-type");
                Relationship content = get(r, "content");
                String mimes = StringResultSerializer.serialize(new PFlow(null, r), mime);

                if (content != null) {

                    res.setContentType(mimes == null ? "application/xml" :mimes);
                    XMLResultSerializer.serialize(new PFlow(null, r), content, out);

                } else {

                    res.setContentType(mimes == null ? "application/octet-stream" :mimes);

                    final boolean[] isNotFound = {true};

                    AnimoResultTraverser._.traverse(
                        new BinaryGraphHandler(out){
                            @Override
                            public void start(Statement statement, Relationship r, int level, boolean isOne) throws IOException {
                                Node n = r.getEndNode();
                                if (BIN.has(n)) {
                                    isNotFound[0] = false;
                                    write(n, out);
                                }
                            }
                        }, new PFlow(null, r), r
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

        private static Relationship get(Relationship r, String have) throws Exception {
            Expression get =  new JExpression(
                _(GET._, have,
                    _(AN._, THE._.reference(r))
                )
            );
            PipedInput pipe = Evaluator._.execute(new PFlow(Evaluator._, r), get);
            for (Object o : pipe) {
                pipe.close();
                return (Relationship) o;
            }
            return null;
        };


    }
	
}
