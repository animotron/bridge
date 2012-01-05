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
import org.animotron.expression.BinaryExpression;
import org.animotron.expression.JExpression;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.statement.operator.THE;
import org.animotron.statement.query.GET;
import org.neo4j.graphdb.Relationship;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.animotron.expression.JExpression._;
import static org.animotron.graph.Nodes.TYPE;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class BinaryServlet extends HttpServlet {

    private String mime(Relationship r) throws IOException {
        String mime = CachedSerializer.STRING.serialize(new JExpression(_(GET._, TYPE, _(r))),FileCache._);
        return mime.isEmpty() ? "application/octet-stream" : mime;
    }

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		try {
            String hash = req.getPathInfo().substring(1);
            File file = BinaryExpression.getFile(hash);
            InputStream is = new FileInputStream(file);
            res.setContentLength((int) file.length());
            OutputStream os = res.getOutputStream();
            res.setContentType(mime(THE.__(hash)));
            byte [] buf = new byte[4096];
            int len;
            while((len=is.read(buf))>0) {
                os.write(buf, 0, len);
            }
		} catch (Exception e) {
            throw new IOException(e);
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
	}

}
