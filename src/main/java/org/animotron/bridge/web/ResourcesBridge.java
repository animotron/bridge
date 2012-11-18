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

import org.animotron.exception.AnimoException;
import org.animotron.expression.AbstractExpression;
import org.animotron.expression.AnimoExpression;
import org.animotron.expression.BinaryExpression;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.DEF;
import org.animotron.statement.operator.NONSTOP;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Pattern;

import static org.animotron.bridge.web.AbstractRequestExpression.URI;
import static org.animotron.bridge.web.WebSerializer.EXTENSION;
import static org.animotron.bridge.web.WebSerializer.MIME_TYPE;
import static org.animotron.expression.Expression.__;

/**
* @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
*
*/
public class ResourcesBridge extends AbstractResourcesBridge {

    public ResourcesBridge(String uriContext) {
        super(uriContext);
    }

    @Override
    protected void loadFile(final File file) throws IOException {

        InputStream is = new FileInputStream(file);

        if (file.getName().endsWith(".animo")) {

            __(new AnimoExpression(is));

        } else {

            final String[] a = file.getName().split(Pattern.quote("."));

            final BinaryExpression e =  new BinaryExpression(is, true) {

                @Override
                protected void description() throws AnimoException, IOException {

                    builder.start(AN._);
                        builder._(REF._, URI);
                        builder._(uriContext + id());
                    builder.end();

                    builder.start(ANY._);
                        builder._(REF._, MIME_TYPE);
                        builder.start(WITH._);
                            builder._(REF._, EXTENSION);
                            builder._(a[a.length - 1]);
                        builder.end();
                    builder.end();

                }
            };

            __(
                new AbstractExpression() {

                    private void is(String s) throws AnimoException, IOException {
                        builder.start(AN._);
                        builder._(REF._, s);
                        builder.end();
                    }

                    @Override
                    public void build() throws Throwable {

                        builder.start(DEF._);

                            builder.start(NONSTOP._);
                                builder._(REF._, e);
                            builder.end();

                            Iterator<String> it = new StringArrayIterator(path(file).split(Pattern.quote("/")));
                            while (it.hasNext()) {
                                String i = it.next();
                                is(i);
                                if (!it.hasNext()) {
                                    for (String s : new StringArrayIterator(a)) {
                                        is(s);
                                    }
                                }
                            }

                        builder.end();

                    }

                }
            );

        }

    }

    private class StringArrayIterator implements Iterable<String>, Iterator<String> {

        private String[] a;
        private String c = null;
        int i = 0;

        public StringArrayIterator(String[] a) {
            this.a = a;
            next();
        }

        @Override
        public Iterator<String> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return c != null;
        }

        @Override
        public String next() {
            String next = c;
            c = step();
            return next;
        }

        private String step() {
            if (i < a.length) {
                String s = a[i]; i++;
                if (s == null || s.isEmpty()) {
                   return step();
                } else {
                    return s;
                }
            } else {
                return null;
            }
        }

        @Override
        public void remove() {}

    }

}
