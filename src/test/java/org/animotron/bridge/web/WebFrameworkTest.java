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

import org.animotron.ATest;
import org.animotron.bridge.FSBridge;
import org.animotron.cache.FileCache;
import org.animotron.expression.JExpression;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.AN;
import org.animotron.statement.query.ANY;
import org.animotron.statement.query.GET;
import org.animotron.statement.relation.USE;
import org.junit.Assert;
import org.junit.Test;

import static org.animotron.expression.JExpression._;
import static org.animotron.expression.JExpression.value;
import static org.animotron.graph.Nodes.*;
import static org.animotron.bridge.web.WebSerializer.*;

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

    	JExpression s;

    	s = new JExpression(
            _(AN._, "rest",
                _(USE._, "root"),
                _(AN._, "uri", value("/")),
                _(AN._, "host", value("localhost"))
            )
        );

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
                                    "the theme-concrete-root-layout " +
                                        "(layout) (theme-concrete) (root) " +
                                        "(\\h1 title \"Welcome to Animo\") " +
                                        "(\\p content \"It is working!\") " +
                                        "(\\ul " +
                                            "(\\li \"Host: \" (\\strong host \"localhost\")) " +
                                            "(\\li \"URI: \" (\\strong uri \"/\")))))) (root) (localhost) (title) (content)).");

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
                _(GET._, "extension",
                        _(AN._, "rest",
                                _(USE._, "favicon"),
                                _(AN._, "uri", value("/favicon.ico")),
                                _(AN._, "host", value("localhost"))
                        )
                )
        );
        assertAnimoResult(s, "extension \"ico\".");

        s = new JExpression(
                _(GET._, "mime-type",
                        _(AN._, "rest",
                                _(USE._, "favicon"),
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
                                        _(USE._, "favicon"),
                                        _(AN._, "uri", value("/favicon.ico")),
                                        _(AN._, "host", value("localhost"))
                                )
                        )
                )
        );
        assertAnimoResult(s, "type \"image/vnd.microsoft.icon\".");

        s = new JExpression(
                _(GET._, "type",
                        _(AN._, "rest",
                                _(USE._, "favicon"),
                                _(AN._, "uri", value("/favicon.ico")),
                                _(AN._, "host", value("localhost"))
                        )
                )
        );
        assertAnimoResult(s, "type \"image/vnd.microsoft.icon\".");

	    String mime = CachedSerializer.STRING.serialize(
            new JExpression(
                _(GET._, TYPE, _(ANY._, MIME_TYPE, _(WITH._, EXTENSION, value("html"))))
            ),
            FileCache._
        );
	    Assert.assertEquals("text/html", mime);
    }

}