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
import org.animotron.expression.AnimoExpression;
import org.animotron.expression.BinaryMapExpression;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.utils.MessageDigester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import static org.animotron.bridge.web.AbstractRequestExpression.URI;
import static org.animotron.expression.Expression.__;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ResourcesMap extends AbstractResourcesBridge {

    public ResourcesMap(String uriContext) {
        super(uriContext);
    }

    @Override
    protected void loadFile(final File file) throws IOException {
        if (file.getName().endsWith(".animo")) {
            __(new AnimoExpression(new FileInputStream(file)));
        } else {
            __(
                new BinaryMapExpression(file) {
                    @Override
                    protected void description() throws AnimoException, IOException {
                        int index;
                        byte buf[] = new byte[1024 * 4];
                        int len;
                        InputStream is = new FileInputStream(file);
                        MessageDigest md = MessageDigester.md();
                        while((len=is.read(buf))>0) {
                            md.update(buf,0,len);
                        }
                        is.close();
                        String name = file.getName();
                        index = name.lastIndexOf(".");
                        if (index > 0) {
                            String extension = name.substring(index + 1);
                            builder.start(AN._);
                                builder._(REF._, extension);
                            builder.end();
                        }
                        builder.start(AN._);
                            builder._(REF._, name);
                        builder.end();
                        builder.start(AN._);
                            builder._(REF._, URI);
                            StringBuilder s = new StringBuilder(4);
                            s.append(uriContext);
                            s.append(path(file));
                            s.append("?");
                            s.append(MessageDigester.byteArrayToHex(md.digest()));
                            builder._(s.toString());
                        builder.end();
                    }
                }
            );
        }
    }

}
