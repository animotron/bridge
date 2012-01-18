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
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.query.ANY;
import org.animotron.statement.query.GET;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static org.animotron.bridge.web.WebSerializer.MIME_TYPE;
import static org.animotron.bridge.web.WebSerializer.TYPE;
import static org.animotron.expression.JExpression._;
import static org.animotron.expression.JExpression.value;
import static org.animotron.graph.Nodes.EXTENSION;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class MapServlet extends HttpServlet {

	private static final long serialVersionUID = 274993368953562096L;

	private File folder;

    public MapServlet(String uri) {
        this.folder = new File(uri);
    }
    
    private String mime(File file) throws IOException {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index > 0) {
            String mime = CachedSerializer.STRING.serialize(
                new JExpression(
                    _(GET._, TYPE, _(ANY._, MIME_TYPE, _(WITH._, EXTENSION, value(name.substring(index + 1)))))
                ),
                FileCache._
            );
            return mime.isEmpty() ? "application/octet-stream" : mime;
        }
        return "application/octet-stream";
    }

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        File file = new File(folder, req.getPathInfo());
        long modified = file.lastModified();
        res.setDateHeader("Last-Modified", modified);
        long since = req.getDateHeader("If-Modified-Since");
        if (since < modified || since > System.currentTimeMillis()) {
            FileInputStream is = null;
            try {
                is = new FileInputStream(file);
                res.setContentLength((int) file.length());
                if (req.getParameterNames().hasMoreElements()) {
                    res.setHeader("Cache-Control", "public, max-age=" + Integer.MAX_VALUE);
                    res.setDateHeader("Expires", Integer.MAX_VALUE);
                }
                OutputStream os = res.getOutputStream();
                res.setContentType(mime(file));
                byte [] buf = new byte[4096];
                int len;
                while((len=is.read(buf))>0) {
                    os.write(buf, 0, len);
                }
                os.close();
            } catch (Exception e) {
                ErrorHandler.doRequest(req, res, e);
            } finally {
                if (is != null) is.close();
            }
        } else {
            res.setStatus(SC_NOT_MODIFIED);
        }
	}

}
