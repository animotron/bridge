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

import org.animotron.exception.AnimoException;
import org.animotron.expression.AbstractExpression;
import org.animotron.graph.builder.FastGraphBuilder;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.operator.THE;
import org.neo4j.graphdb.Node;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import static org.animotron.graph.Nodes.URI;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public abstract class AbstractRequestExpression extends AbstractExpression {

    private static final Node HOST = THE._("host");

    private final HttpServletRequest req;

    public AbstractRequestExpression(HttpServletRequest req) throws Exception {
        super(new FastGraphBuilder());
        this.req = req;
    }
    
    protected void processRequest() throws AnimoException, IOException {
        builder.start(AN._);
            builder._(REF._, HOST);
            builder._(req.getServerName());
        builder.end();
        builder.start(AN._);
            builder._(REF._, URI);
            builder._(req.getRequestURI());
        builder.end();
    }

    public HttpServletRequest getRequest() {
        return req;
    }

}
