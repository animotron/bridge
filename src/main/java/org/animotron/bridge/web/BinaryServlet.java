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

import org.animotron.cache.FileCache;
import org.animotron.expression.JExpression;
import org.animotron.graph.Properties;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.statement.operator.THE;
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

import static org.animotron.bridge.web.WebSerializer.*;
import static org.animotron.expression.JExpression._;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class BinaryServlet extends HttpServlet {

	private static final long serialVersionUID = 6702513972501476806L;

    private String mime(Relationship r) throws IOException {
        String mime = CachedSerializer.STRING.serialize(new JExpression(_(GET._, TYPE, _(r))),FileCache._);
        return mime.isEmpty() ? "application/octet-stream" : mime;
    }

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		InputStream is = null;
		OutputStream os = null;
		long startTime = System.currentTimeMillis();
		try {
            Relationship r = THE._.get(req.getPathInfo().substring(1));
            Node n = r.getEndNode().getSingleRelationship(STREAM._, Direction.OUTGOING).getEndNode();
            
            File file = new File((String) Properties.VALUE.get(n));
            is = new FileInputStream(file);
            res.setContentLength((int) file.length());
            
            os = res.getOutputStream();
            res.setContentType(mime(r));
            
            byte [] buf = new byte[4096];
            int len;
            while((len=is.read(buf))>0) {
                os.write(buf, 0, len);
            }
		} catch (Exception e) {
            ErrorHandler.doGet(req, res, ErrorHandler.NOT_FOUND);
        } finally {
        	if (is != null) is.close();
        	if (os != null) os.close();
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
	}

}
