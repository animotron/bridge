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
import org.animotron.statement.operator.AN;
import org.animotron.statement.query.ANY;
import org.animotron.statement.relation.USE;
import org.junit.Test;

import static org.animotron.expression.JExpression._;
import static org.animotron.expression.JExpression.value;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class AnimotronTest extends ATest {

    @Test
    public void test() throws Exception {

		FSBridge._.load("animo/");
        FSBridge._.load("site/");

        (new CommonResourcesMap("/common")).load("common/");

    	JExpression s;

    	s = new JExpression(
			_(
			    _(USE._, "root"),
			    _(USE._, "theme-concrete"),
			    _(AN._, "html-page",
		    		_(ANY._, "resource")
	    		)
			)
        );
    	assertXMLResult(s, 
			"<?xml version='1.0' encoding='UTF-8'?>" +
			"<html>" +
				"<head>" +
					"<title>Welcome to Animo</title>" +
					"<meta name=\"keywords\" content=\"\"/>" +
					"<meta name=\"description\" content=\"\"/>" +
					"<link rel=\"shortcut icon\" href=\"\"/>" +
				"</head>" +
				"<body>" +
					"<h1>Welcome to Animo</h1>" +
					"<p>It is working!</p>" +
					"<ul>" +
						"<li>Host: <strong/></li>" +
						"<li>URI: <strong/></li>" +
					"</ul>" +
				"</body>" +
			"</html>");
    }
    
}