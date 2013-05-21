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
import org.animotron.expression.Expression;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;
import org.animotron.statement.query.GET;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders.Names;

import java.io.IOException;
import java.util.Enumeration;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public abstract class AbstractRequestExpression extends Expression {

    public static final String URI = "uri";
    public static final String HOST = "host";
    public static final String SITE = "site";
    public static final String SERVER_NAME = "server-name";

    protected final FullHttpRequest req;

    public AbstractRequestExpression(FullHttpRequest req) throws Throwable {
        this.req = req;
    }
    
    private String serverName() {
    	return req.headers().get(Names.HOST);
    }

    @Override
    public void build() throws AnimoException, IOException {
        builder.start(AN._);
            builder.start(GET._);
                service();
                builder.start(ANY._);
                    builder._(REF._, SITE);
                    builder.start(WITH._);
                        builder._(REF._, SERVER_NAME);
                        builder._(serverName());
                    builder.end();
                builder.end();
            builder.end();
            context();
            params();
            uri();
            // TODO add sorted request parameters, headers, attributes, cookies and etc
        builder.end();
    }

    protected abstract void service() throws AnimoException, IOException;

    protected abstract void context() throws AnimoException, IOException;

    private void uri() throws AnimoException, IOException {
        builder.start(AN._);
            builder._(REF._, HOST);
            builder._(serverName());
        builder.end();
        builder.start(AN._);
            builder._(REF._, URI);
            builder._(serverName());
        builder.end();
    }

    private void params() throws AnimoException, IOException {
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
    }

}
