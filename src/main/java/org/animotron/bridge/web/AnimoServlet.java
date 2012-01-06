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

import org.animotron.cache.FileCache;
import org.animotron.exception.AnimoException;
import org.animotron.exception.ENotFound;
import org.animotron.expression.AbstractExpression;
import org.animotron.expression.Expression;
import org.animotron.expression.JExpression;
import org.animotron.graph.builder.FastGraphBuilder;
import org.animotron.graph.serializer.BinarySerializer;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.operator.THE;
import org.animotron.statement.query.GET;
import org.animotron.statement.relation.USE;
import org.neo4j.graphdb.Node;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import static org.animotron.expression.JExpression._;
import static org.animotron.graph.Nodes.TYPE;
import static org.animotron.graph.Nodes.URI;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class AnimoServlet extends HttpServlet {

    protected static final Node REST = THE._("rest");
    protected static final Node NOT_FOUND = THE._("not-found");
    protected static final Node ROOT = THE._("root");
    protected static final Node HOST = THE._("host");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		try {
	        writeResponse(new AnimoRequest(req), res);
        } catch (ENotFound e) {
            try {
                writeResponse(new AnimoNotFound(req), res);
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

	private void writeResponse(Expression e, HttpServletResponse res) throws Exception {
        WebSerializer.serialize(e, res);
    }

    private abstract class RequestExpression extends AbstractExpression {
        protected final HttpServletRequest req;
        public RequestExpression(HttpServletRequest req) throws Exception {
            super(new FastGraphBuilder());
            this.req = req;
        }
    }

    protected class AnimoRequest extends RequestExpression {

        public AnimoRequest(HttpServletRequest req) throws Exception {
            super(req);
        }

        @Override
        public void build() throws Exception {
            
            builder.start(AN._);

                builder._(REF._, REST);

                String uri = req.getRequestURI();

                boolean isRoot = true;
                for (String part : uri.split("/")) {
                    if (part.isEmpty()) continue;
                    String[] parts = part.split("\\.");
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

                Enumeration<String> names = req.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    builder.start(AN._);
                    builder._(REF._, name);
                    for  (String value : req.getParameterValues(name)) {
                        builder._(value);
                    }
                    builder.end();
                }

                builder.start(AN._);
                    builder._(REF._, HOST);
                    builder._(req.getServerName());
                builder.end();
                builder.start(AN._);
                    builder._(REF._, URI);
                    builder._(uri);
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
            builder.start(AN._);
                builder._(REF._, REST);
                builder._(USE._, NOT_FOUND);
                builder.start(AN._);
                    builder._(REF._, HOST);
                    builder._(req.getServerName());
                builder.end();
                builder.start(AN._);
                    builder._(REF._, URI);
                    builder._(req.getRequestURI());
                builder.end();
            builder.end();
        }
    }

    protected static class WebSerializer {

        public static void serialize(final Expression request, HttpServletResponse res) throws Exception {
            OutputStream os = res.getOutputStream();
            String mime = CachedSerializer.STRING.serialize(
                    new JExpression(
//                            _(GET._, TYPE, _(GET._, MIME-TYPE, _(request)))
                            _(GET._, TYPE, _(request))
                    ),
                    FileCache._
            );
            if ("text/html".equals(mime)) {
                res.setContentType("text/html");
                CachedSerializer.HTML.serialize(request, os, FileCache._);
            } else {
                res.setContentType(mime.isEmpty() ? "application/xml" : mime);
                try {
                    CachedSerializer.XML.serialize(request, os, FileCache._);
                } catch (IOException e) {
                    OutputStreamWrapper osw = new OutputStreamWrapper(os);
                    res.setContentType(mime.isEmpty() ? "application/octet-stream" : mime);
                    BinarySerializer._.serialize(request, osw);
                    if (osw.isEmpty()) {
                        throw new AnimoException(null, "Resource not found");
                    }
                }
            }
        }

        private static class OutputStreamWrapper extends OutputStream {

            private OutputStream os;
            private boolean empty = true;

            public OutputStreamWrapper(OutputStream os) {
                this.os = os;
            }

            @Override
            public void write(int b) throws IOException{
                os.write(b);
                empty = false;
            }

            @Override
            public void write(byte b[]) throws IOException {
                os.write(b);
                empty = false;
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                os.write(b, off, len);
                empty = false;
            }

            @Override
            public void flush() throws IOException {
                os.flush();
            }

            @Override
            public void close() throws IOException {
                os.close();
            }

            public boolean isEmpty() {
                return empty;
            }

        }

    }

}
