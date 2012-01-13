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
import org.animotron.graph.builder.FastGraphBuilder;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.link.LINK;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public abstract class AbstractRequestExpression extends AbstractExpression {

    public static final String URI = "uri";
    public static final String HOST = "host";
    public static final String SITE = "site";
    public static final String SERVER_NAME = "server-name";
    public static final String HTML_PAGE = "html-page";
    public static final String RESOURCE = "resource";
    
    protected final HttpServletRequest req;

    public AbstractRequestExpression(HttpServletRequest req) throws Exception {
        super(new FastGraphBuilder(true));
        this.req = req;
    }

    @Override
    public void build() throws AnimoException, IOException {
        builder.start(LINK._);
            builder.start(AN._);
                context();
                params();
                uri();
            builder.end();
        site();
        builder.end();
    }

    protected abstract void context() throws AnimoException, IOException;

    private void uri() throws AnimoException, IOException {
        builder.start(AN._);
            builder._(REF._, HOST);
            builder._(req.getServerName());
        builder.end();
        builder.start(AN._);
            builder._(REF._, URI);
            builder._(req.getRequestURI());
        builder.end();
    }

    private final void site() throws AnimoException, IOException {
        builder.start(ANY._);
            builder._(REF._, SITE);
            builder.start(WITH._);
                builder._(REF._, SERVER_NAME);
                builder._(req.getServerName());
            builder.end();
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
