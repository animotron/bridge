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

import org.animotron.exception.ENotFound;
import org.animotron.expression.JExpression;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.statement.operator.AREV;
import org.animotron.statement.operator.DEF;
import org.animotron.statement.query.GET;
import org.animotron.statement.value.STREAM;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;

import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static org.animotron.expression.JExpression._;
import static org.animotron.graph.Nodes.EXTENSION;
import static org.animotron.graph.Properties.HASH;
import static org.animotron.graph.Properties.VALUE;
import static org.animotron.utils.MessageDigester.byteArrayToHex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class BridgeServlet extends HttpServlet {

	private static final long serialVersionUID = 6702513972501476806L;

    private String mime(Relationship r) throws Throwable {
        return WebSerializer.mime(CachedSerializer.STRING.serialize(new JExpression(_(GET._, EXTENSION, _(r)))));
    }

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        InputStream is = null;
        try {
            Relationship r = DEF._.get(req.getPathInfo().substring(1));
            if (r == null) {
                throw new ENotFound(null);
            }
            Node n = AREV._.actualNode(r).getSingleRelationship(STREAM._, Direction.OUTGOING).getEndNode();
            File file = new File((String) VALUE.get(n));
            is = new FileInputStream(file);
            long modified = file.lastModified();
            res.setDateHeader("Last-Modified", modified);
            boolean isHTTP11 = req.getProtocol().endsWith("1.1");
            if (isHTTP11) {
                String hash = byteArrayToHex((byte[]) HASH.get(r));
                res.setHeader("ETag", hash);
                Enumeration<String> etag = req.getHeaders("If-None-Match");
                if (etag.hasMoreElements()) {
                    res.setStatus(SC_NOT_MODIFIED);
                    return;
                }
            }
            long since = req.getDateHeader("If-Modified-Since");
            long time = System.currentTimeMillis();
            if (since < modified || since > time) {
                res.setContentLength((int) file.length());
                if (isHTTP11) {
                    res.setHeader("Cache-Control", "public, max-age=" + (long) 365 * 24 * 60 * 60);
                }
                res.setDateHeader("Expires", time + (long) 365 * 24 * 60 * 60 * 1000);
                res.setContentType(mime(r));
                OutputStream os = res.getOutputStream();
                byte [] buf = new byte[4096];
                int len;
                while((len=is.read(buf))>0) {
                    os.write(buf, 0, len);
                }
                os.close();
            } else {
                res.setStatus(SC_NOT_MODIFIED);
            }
        } catch (Throwable t) {
            ErrorHandler.doRequest(req, res, t);
        } finally {
            if (is != null) is.close();
        }
	}

}
