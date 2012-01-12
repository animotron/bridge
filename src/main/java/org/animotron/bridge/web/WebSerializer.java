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
import org.animotron.exception.ENotFound;
import org.animotron.expression.Expression;
import org.animotron.expression.JExpression;
import org.animotron.graph.serializer.BinarySerializer;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.statement.query.GET;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.animotron.expression.JExpression._;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebSerializer {

    public static final String TYPE = "type";
    public static final String MIME_TYPE = "mime-type";

    public static void serialize(Expression request, HttpServletResponse res) throws IOException, ENotFound {
        OutputStream os = res.getOutputStream();
        String mime = CachedSerializer.STRING.serialize(
                new JExpression(
                        _(GET._, TYPE, _(GET._, MIME_TYPE, _(request)))
                ),
                FileCache._
        );
        if ("text/html".equals(mime)) {
            res.setContentType("text/html");
            CachedSerializer.HTML.serialize(request, os, FileCache._);
            os.close();
        } else {
            res.setContentType(mime.isEmpty() ? "application/xml" : mime);
            try {
                CachedSerializer.XML.serialize(request, os, FileCache._);
                os.close();
            } catch (IOException e) {
                OutputStreamWrapper osw = new OutputStreamWrapper(os);
                res.setContentType(mime.isEmpty() ? "application/octet-stream" : mime);
                BinarySerializer._.serialize(request, osw);
                if (osw.isEmpty()) {
                    throw new ENotFound(request);
                }
                osw.close();
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
