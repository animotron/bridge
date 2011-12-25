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
package org.animotron.bridge;

import org.animotron.ATest;
import org.animotron.expression.JExpression;
import org.animotron.statement.operator.AN;
import org.animotron.statement.query.GET;
import org.animotron.statement.relation.USE;
import org.junit.Test;

import static org.animotron.expression.JExpression._;
import static org.animotron.expression.JExpression.value;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebFrameworkTest extends ATest {

    @Test
    public void test() throws Exception {

    	FSBridge.load("src/test/animo/");
    	JExpression s;

        s = new JExpression(
            _(AN._, "rest",
                _(USE._, "root"),
                _(AN._, "uri", value("/")),
                _(AN._, "host", value("localhost"))
            )
        );
        assertAnimoResult(s, "rest (the localhost-site (site) (server-name) (use theme-concrete) (use localhost)) (the it-working (html-service (service resource) (use text-html) (result)) (root) (localhost) (title) (content)).");

    	s = new JExpression(
            _(GET._, "result",
                _(AN._, "rest",
                    _(USE._, "root"),
                    _(AN._, "uri", value("/")),
                    _(AN._, "host", value("localhost"))
                )
            )
        );

        assertAnimoResult(s,
            "result " +
                "\\html " +
                    "(\\head " +
                        "(\\title title \"Welcome to Animo\") " +
                        "(\\meta (@name \"keywords\") (@content)) " +
                        "(\\meta (@name \"description\") (@content))) " +
                    "(\\body " +
                        "the theme-concrete-root-layout " +
                            "(layout) " +
                            "(theme-concrete) " +
                            "(root) " +
                            "(\\h1 title \"Welcome to Animo\") " +
                            "(\\p content \"It is working!\") " +
                            "(\\ul " +
                                "(\\li \"Host: \" (\\strong host \"localhost\")) " +
                                "(\\li \"URI: \" (\\strong uri \"/\")))).");

        assertXMLResult(s,
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

        s = new JExpression(
            _(GET._, "type",
                _(GET._, "mime-type",
                    _(AN._, "rest",
                        _(USE._, "root"),
                        _(AN._, "uri", value("/")),
                        _(AN._, "host", value("localhost"))
                    )
                )
            )
        );
        assertAnimoResult(s, "type \"text/html\".");

        s = new JExpression(
            _(AN._, "rest",
                _(USE._, "favicon.ico"),
                _(AN._, "uri", value("/favicon.ico")),
                _(AN._, "host", value("localhost"))
            )
        );
        assertAnimoResult(s, "rest (the localhost-site (site) (server-name) (use theme-concrete) (use localhost)) (the ed1e7377fac692253e213e7bf57f11a1781581328e7814bca9102c45d7ef3757 (file (resource) (the image-vnd-microsoft-icon (mime-type) (image) (type) (name) (extension))) (stream 52b798f0e91fc2b64bd4d7921c9f2836453616cfc4bf4e291161512ad4c810b9000000000000057e) (localhost) (favicon.ico) (name) (extension)).");

        s = new JExpression(
            _(GET._, "mime-type",
                _(AN._, "rest",
                    _(USE._, "favicon.ico"),
                    _(AN._, "uri", value("/favicon.ico")),
                    _(AN._, "host", value("localhost"))
                )
            )
        );
        assertAnimoResult(s, "the image-vnd-microsoft-icon (mime-type) (image) (type) (name) (extension).");

        s = new JExpression(
            _(GET._, "type",
                _(GET._, "mime-type",
                    _(AN._, "rest",
                        _(USE._, "favicon.ico"),
                        _(AN._, "uri", value("/favicon.ico")),
                        _(AN._, "host", value("localhost"))
                    )
                )
            )
        );
        assertAnimoResult(s, "type \"image/vnd.microsoft.icon\".");

    }

}