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

import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.operator.THE;
import org.neo4j.graphdb.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.animotron.bridge.web.WebSerializer.serialize;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ErrorHandler {
    
    public final static int NOT_FOUND = 404;

	public static void doGet(HttpServletRequest req, HttpServletResponse res, int status) {
		long startTime = System.currentTimeMillis();
        res.setStatus(status);
		try {
            serialize(new AnimoRequest(req, status), res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
	}

    private static class AnimoRequest extends AbstractRequestExpression {

        private static final Node REST_ERROR = THE._("rest-error");
        private static final Node STATUS = THE._("status");
        private int status;

        public AnimoRequest(HttpServletRequest req, int status) throws Exception {
            super(req);
            this.status = status;
        }

        @Override
        public void build() throws Exception {
            builder.start(AN._);
                builder._(REF._, REST_ERROR);
                builder.start(AN._);
                    builder._(REF._, STATUS);
                    builder._(status);
                builder.end();
                processRequest();
            builder.end();
        }

    }

}
