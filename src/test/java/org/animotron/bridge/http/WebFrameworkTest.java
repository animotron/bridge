/*
 *  Copyright (C) 2011-2013 The Animo Project
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
package org.animotron.bridge.http;

import org.animotron.ATest;
import org.animotron.bridge.FSBridge;
import org.animotron.bridge.ResourcesBridge;
import org.animotron.expression.AnimoExpression;
import org.animotron.expression.Expression;
import org.junit.Test;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebFrameworkTest extends ATest {

    @Test
    public void test() throws Throwable {

    	FSBridge._.load("src/test/resources/animo/");
        new ResourcesBridge("/binary").load("src/test/resources/site/");

    	Expression s;

    	s = new AnimoExpression("rest (use root) (uri '/') (host 'localhost')");

        assertAnimoResult(s,
            "rest " +
                "(the localhost-site (site) (server-name) (use theme-concrete) (use localhost)) " +
                "(the it-working " +
                    "(html-service " +
                            "(service resource) " +
                            "(mime-type) " +
                            "(\\html " +
                                "(\\head " +
                                    "(\\title title \"Welcome to Animo\") " +
                                    "(\\meta (@name \"keywords\") (@content)) " +
                                    "(\\meta (@name \"description\") (@content))) " +
                                "(\\body " +
                                    "def theme-concrete-root-layout " +
                                        "(layout) (theme-concrete) (root) " +
                                        "(\\h1 title \"Welcome to Animo\") " +
                                        "(\\p content \"It is working!\") " +
                                        "(\\ul " +
                                            "(\\li \"Host: \" (\\strong host \"localhost\")) " +
                                            "(\\li \"URI: \" (\\strong uri \"/\")))))) (root) (localhost) (title) (content)).");

        assertStringResult(s,
            "<html>" +
                "<head>" +
                    "<title>Welcome to Animo</title>" +
                    "<meta name=\"keywords\" content=\"\"/>" +
                    "<meta name=\"description\" content=\"\"/>" +
                "</head>" +
                "<body>" +
                    "<h1>Welcome to Animo</h1>" +
                    "<p>It is working!</p>" +
                    "<ul>" +
                        "<li>Host: <strong>localhost</strong></li>" +
                        "<li>URI: <strong>/</strong></li>" +
                    "</ul>" +
                "</body>" +
            "</html>");

    }

}