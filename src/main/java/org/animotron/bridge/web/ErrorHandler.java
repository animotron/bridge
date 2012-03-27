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
import org.animotron.exception.ENotFound;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
import org.animotron.statement.query.ANY;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.animotron.bridge.web.WebSerializer.serialize;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ErrorHandler {

    public static void doRequest(HttpServletRequest req, HttpServletResponse res, Throwable x) {
        long startTime = System.currentTimeMillis();
        try {
            if (x instanceof ENotFound || x instanceof FileNotFoundException) {
                res.setStatus(SC_NOT_FOUND);
                serialize(new AnimoRequest(req, SC_NOT_FOUND, null), res);
            } else {
                res.reset();
                res.setStatus(SC_INTERNAL_SERVER_ERROR);
                serialize(new AnimoRequest(req, SC_INTERNAL_SERVER_ERROR, x), res);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
    }

    public static void doRequest(HttpServletRequest req, HttpServletResponse res, int status) {
        long startTime = System.currentTimeMillis();
        try {
            res.setStatus(status);
            serialize(new AnimoRequest(req, status, null), res);
        } catch (Throwable t) {
            doRequest(req, res, t);
        }
        System.out.println("Generated in "+(System.currentTimeMillis() - startTime));
    }

    private static class AnimoRequest extends AbstractRequestExpression {

        private static final String STACK_TRACE = "stack-trace";
        private static final String ERROR = "error";
        private static final String CODE = "code";
        private Throwable x;
        private int status;

        public AnimoRequest(HttpServletRequest req, int status, Throwable x) throws Throwable {
            super(req);
            this.status = status;
            this.x = x;
        }

        @Override
        protected void service() throws AnimoException, IOException {
            builder.start(ANY._);
                builder._(REF._, ERROR);
                builder.start(WITH._);
                    builder._(REF._, CODE);
                    builder._(status);
                builder.end();
            builder.end();
        }

        @Override
        public void context() throws AnimoException, IOException {
            builder.start(AN._);
                builder._(REF._, CODE);
                builder._(status);
            builder.end();
            if (x != null) {
                builder.start(AN._);
                    builder._(REF._, STACK_TRACE);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    x.printStackTrace(pw);
                    BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
                    String s;
                    while ((s = br.readLine()) != null) {
                        builder._(s);
                    }
                builder.end();
            }
        }

    }

}
