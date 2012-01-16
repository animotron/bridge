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
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;
import org.animotron.statement.relation.USE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.animotron.bridge.web.WebSerializer.serialize;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ErrorHandler {

    public final static int NOT_FOUND = 404;
    public final static int INTERNAL_SERVER_ERROR = 500;

	public static void doGet(HttpServletRequest req, HttpServletResponse res, int status) {
        doGet(req, res, status, null);
	}

    public static void doGet(HttpServletRequest req, HttpServletResponse res, int status, Exception x) {
        long startTime = System.currentTimeMillis();
        res.setStatus(status);
        try {
            serialize(new AnimoRequest(req, status, x), res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
    }

    private static class AnimoRequest extends AbstractRequestExpression {

        private static final String STACK_TRACE = "stack-trace";
        private static final String ERROR = "error";
        private static final String CODE = "code";
        private Exception x;
        private int status;

        public AnimoRequest(HttpServletRequest req, int status, Exception x) throws Exception {
            super(req);
            this.status = status;
            this.x = x;
        }

        @Override
        public void context() throws AnimoException, IOException {
            builder._(REF._, HTML_PAGE);
            builder.start(USE._);
                builder._(REF._, ERROR);
            builder.end();
            builder.start(ANY._);
                builder._(REF._, RESOURCE);
                builder.start(WITH._);
                    builder._(REF._, CODE);
                    builder._(status);
                builder.end();
                if (x != null) {
                    builder.start(AN._);
                        builder._(REF._, STACK_TRACE);
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        x.printStackTrace(pw);
                        builder._(sw.toString());
                    builder.end();
                }
            builder.end();
        }

    }

}
