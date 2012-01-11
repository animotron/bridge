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

import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;

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

        private static final String REST_ERROR = "rest-error";
        private static final String STATUS = "status";
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
