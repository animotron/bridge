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
import org.animotron.statement.value.STREAM;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import static org.animotron.expression.JExpression._;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class AnimoServlet extends HttpServlet {

	private static final long serialVersionUID = 7276574723383015880L;

    private static final Node REST = reference("rest");
    private static final Node MIME = reference("mime-type");
    private static final Node URI = reference("uri");
    private static final Node CONTENT = reference("content");
    private static final Node NOTFOUND = reference("not-found");
    private static final Node ROOT = reference("root");
    private static final Node HOST = reference("host");

    private static Node reference (String name) {
        try {
            return THE._.getOrCreate(name, true).getEndNode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
        try {
            writeResponse(new CommonExpression(req.getInputStream(), req.getRequestURI()), res);
        } catch (Exception e) {
            throw new ServletException(e);
        }
	}
	
	private void writeResponse(Expression e, HttpServletResponse res) throws Exception {
        WebSerializer.serialize(e, res);
    }

    private abstract class RequestExpression extends AbstractExpression {
        protected final HttpServletRequest req;
        public RequestExpression(HttpServletRequest req) throws Exception {
            super(new FastGraphBuilder());
            this.req = req;
            builder.build(this);
        }
    }

    private class AnimoRequest extends RequestExpression {

        public AnimoRequest(HttpServletRequest req) throws Exception {
            super(req);
        }

        @Override
        public void build() throws Exception {
            
            builder.start(AN._, REST);

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
                    builder.start(USE._, ROOT);
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

                builder.start(HAVE._, HOST);
                    builder.start(req.getServerName());
                    builder.end();
                builder.end();
                builder.start(HAVE._, URI);
                    builder.start(req.getRequestURI());
                    builder.end();
                builder.end();

            builder.end();
        }
    }

    private class AnimoNotFound extends RequestExpression {

        public AnimoNotFound(HttpServletRequest req) throws Exception {
            super(req);
        }

        @Override
        public void build() throws Exception {
            builder.start(AN._, REST);
                builder.start(USE._, NOTFOUND);
                builder.end();
                builder.start(HAVE._, HOST);
                    builder.start(req.getServerName());
                    builder.end();
                builder.end();
                builder.start(HAVE._, URI);
                    builder.start(req.getRequestURI());
                    builder.end();
                builder.end();
            builder.end();
        }
    }

    private static class WebSerializer {

        public static void serialize(final Expression request, HttpServletResponse res) throws Exception {
            final OutputStream out = res.getOutputStream();
            try {
                Relationship mime =  get(request, MIME);
                Relationship content = get(request, CONTENT);
                String mimes = StringResultSerializer.serialize(mime);
                if (content != null) {
                    res.setContentType(mimes == null ? "application/xml" : mimes);
                    XMLResultSerializer.serialize(content, out);
                } else {
                    res.setContentType(mimes == null ? "application/octet-stream" : mimes);
                    final boolean[] isNotFound = {true};
                    //UNDERSTAND: why it here?
                    AnimoResultTraverser._.traverse(
                        new BinaryGraphHandler(out){
                            @Override
                            public void start(Statement statement, Relationship r, int level, boolean isOne) throws IOException {
                                if (statement instanceof STREAM) {
                                    isNotFound[0] = false;
                                    write(r.getEndNode(), out);
                                }
                            }
                        }, new PFlow(Evaluator._, request), request
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

        private static Relationship get(Expression context, Node anything) throws Exception {
            Expression get = new JExpression(
                _(GET._, anything, _(context))
            );
            PipedInput pipe = Evaluator._.execute(new PFlow(Evaluator._, context), get);
            for (Object o : pipe) {
                pipe.close();
                return (Relationship) o;
            }
            return null;
        };


    }
	
}
