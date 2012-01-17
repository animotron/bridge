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
import org.animotron.expression.BinaryExpression;
import org.animotron.expression.DefaultDescription;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.animotron.bridge.web.AbstractRequestExpression.URI;
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
            __(
                    new BinaryExpression(is, true) {

                        @Override
                        protected String id () {
                            return hash();
                        }

                        @Override
                        protected void description() throws AnimoException, IOException {
                            DefaultDescription.create(builder, path(file));
                            builder.start(AN._);
                                builder._(REF._, URI);
                                builder._(uriContext + id());
                            builder.end();
                        }

                    }
            );
        }
    }

}
