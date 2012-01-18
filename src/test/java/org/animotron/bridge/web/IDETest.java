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
import org.animotron.expression.JExpression;
import org.animotron.statement.compare.WITH;
import org.animotron.statement.operator.AN;
import org.animotron.statement.query.ANY;
import org.junit.Test;

import static org.animotron.expression.JExpression._;
import static org.animotron.expression.JExpression.value;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class IDETest extends ATest {

    @Test
    public void test() throws Exception {

		FSBridge._.load("animo/");
        FSBridge._.load("apps/");

        (new ResourcesMap("/common")).load("common/");

    	JExpression s;

    	s = new JExpression(
//			_(
//				_(ANY._, "site",
//					_(WITH._, "server-name", value("localhost"))
//				),
				_(AN._, "animoIDE")
//			)
        );
    	
    	long time = System.currentTimeMillis();
    	assertXMLResult(s, 
		"<html>" +
			"<head>" +
				"<title/>" +
				"<link rel=\"shortcut icon\" href=\"\"/>" +
				"<script src=\"/common/ace-0.2.0/src/ace.js?ed2ca47b667f0cf978c4bcad33353a09c7f87d0c76540ba6ed277901b89b3965\"/>" +
				"<script src=\"/common/ace-0.2.0/textarea/src/ace.js?6967de4fa08f2e5c9ed5aa77fc6f0847d8130f265018736a1ac1d264eeca7c87\"/>" +
				"<script src=\"/common/jquery-1.7.1/jquery.min.js?88171413fc76dda23ab32baa17b11e4fff89141c633ece737852445f1ba6c1bd\"/>" +
			"</head>" +
			"<body>" +
				"<div class=\"ui-layout-north\">" +
					"<div id=\"header\">" +
						"<div id=\"login-info\">" +
							"<span id=\"user\"/>" +
							"<a id=\"login\" href=\"#\">Login</a>" +
						"</div>" +
					"</div>" +
				"</div>" +
				"<div class=\"ui-layout-center\">" +
					"<div class=\"editor-header\">" +
						"<div class=\"menu\">" +
							"<ul><li><a href=\"#\">File</a><ul><li><a href=\"#\" id=\"menu-file-open\">Open</a></li></ul></li></ul>" +
						"</div>" +
					"</div>" +
					"<span id=\"toolbar\"/>" +
					"<div id=\"tabs-container\">" +
						"<ul id=\"tabs\"><button id=\"tab-prev\">Previous</button><button id=\"tab-next\">Next</button></ul>" +
					"</div>" +
					"<pre class=\"content\" id=\"editor\"/>" +
					"<div id=\"status-bar\">" +
						"<input id=\"search-box\" type=\"text\"/>" +
						"<a href=\"#\"><id>error-status</id></a>" +
					"</div>" +
				"</div>" +
				"<div id=\"outline-container\" class=\"ui-layout-west\">" +
					"<h3>Outline</h3><ul id=\"outline\" class=\"content\"><li/></ul>" +
				"</div>" +
			"</body>" +
		"</html>");
    	System.out.println("Done in "+(System.currentTimeMillis() - time));
    }
    
}