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

import junit.framework.Assert;
import org.animotron.ATest;
import org.animotron.bridge.FSBridge;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class AnimoServletTest extends ATest {

    @Test
    public void test_0() throws Exception {
    	FSBridge._.load("src/test/resources/animo/");
    	AnimoServlet servlet = new AnimoServlet();
    	HttpRequest request = new HttpRequest("/","localhost");
    	HttpResponse response = new HttpResponse(false);
    	servlet.doGet(request, response);
        Assert.assertEquals(
            "<!DOCTYPE html>" +
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
            "</html>",
            response.getResponseString()
        );
    }

    private void test(AnimoServlet servlet, String uri) throws IOException, ServletException {
        HttpRequest request = new HttpRequest(uri, "localhost");
        HttpResponse response = new HttpResponse(false);
        servlet.doGet(request, response);
        assertArrayEquals(getBytesFromFile(new File("src/test/resources/site/localhost/favicon.ico")), response.getResponse());
    }

    @Test
    public void test_1() throws Exception {
        FSBridge._.load("src/test/resources/animo/");
        new SiteResourcesBridge("/binary").load("src/test/resources/site/");
        AnimoServlet servlet = new AnimoServlet();
        test(servlet, "/favicon");
        test(servlet, "/favicon/ico");
        test(servlet, "/favicon.ico");
        test(servlet, "/ico");
        test(servlet, "/ico/favicon");
        test(servlet, "/ico.favicon");
    }

}
