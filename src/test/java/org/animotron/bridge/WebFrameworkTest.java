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
import org.animotron.statement.relation.HAVE;
import org.animotron.statement.relation.USE;
import org.junit.Test;

import static org.animotron.expression.JExpression.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebFrameworkTest extends ATest {

    @Test
    public void test() throws Exception {

    	FSBridge.load("src/main/animo/");

        JExpression s = new JExpression(
            _(GET._, "content",
                _(AN._, "rest",
                    _(USE._, "root"),
                    _(HAVE._, "uri", text("/")),
                    _(HAVE._, "host", text("localhost"))
                )
            )
        );

        assertAnimoResult(s,
            "have content " +
                "\\html " +
                    "(\\head " +
                    	"(\\title have title \"Welcome to Animo\") " +
                    	"(\\meta (@name \"keywords\") (@content \"get keywords\")) " +
                    	"(\\meta (@name \"description\") (@content \"get description\"))) " +
                    "(\\body the theme-concrete-root-layout (is root-layout) " +
                        "(\\h1 have title \"Welcome to Animo\") " +
                        "(\\p have content \"It is working!\") " +
                        "(\\ul " +
                            "(\\li (\"Host: \") (\\strong have host \"localhost\")) " +
                            "(\\li (\"URI: \") (\\strong have uri \"/\"))))");


        assertXMLResult(s,
            "<html>" +
                "<head>" +
                    "<title>Welcome to Animo</title>" +
                    "<meta name=\"keywords\" content=\"get keywords\"/>" +
                    "<meta name=\"description\" content=\"get description\"/>" +
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
                _(GET._, "mime-type",
                    _(AN._, "favicon.ico",
                        _(USE._, "root"),
                        _(HAVE._, "uri", text("/favicon.ico")),
                        _(HAVE._, "host", text("localhost"))
                    )
                )
            );
        //XXX:wrong
        assertAnimoResult(s, "have mime-type \"text/html\"");

        //XXX:wrong
        assertXMLResult(s,
                "<html>" +
                    "<head>" +
                        "<title>Welcome to Animo</title>" +
                        "<meta name=\"keywords\" content=\"get keywords\"/>" +
                        "<meta name=\"description\" content=\"get description\"/>" +
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