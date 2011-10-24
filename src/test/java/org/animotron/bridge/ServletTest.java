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

import junit.framework.Assert;
import org.animotron.ATest;
import org.junit.Test;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ServletTest extends ATest {

    @Test
    public void test() throws Exception {

    	FSBridge.load("src/main/animo/");
    	
    	AnimoServlet servlet = new AnimoServlet();
    	
    	HttpRequest request = new HttpRequest("/","localhost");
    	HttpResponse response = new HttpResponse();
    	
//    	Expression request = servlet.new AnimoRequest();
//    	String mime = StringResultSerializer.serialize(AnimoServlet.WebSerializer.get(request, AnimoServlet.MIME));
//    	assertTrue("".equals(mime));
    	
    	servlet.doGet(request, response);

//        assertAnimoResult(s,
//            "have content " +
//                "\\html " +
//                    "(\\head " +
//                    	"(\\title have title \"Welcome to Animo\") " +
//                    	"(\\meta (@name \"keywords\") (@content \"get keywords\")) " +
//                    	"(\\meta (@name \"description\") (@content \"get description\"))) " +
//                    "(\\body the theme-concrete-root-layout (is root-layout) " +
//                        "(\\h1 have title \"Welcome to Animo\") " +
//                        "(\\p have content \"It is working!\") " +
//                        "(\\ul " +
//                            "(\\li (\"Host: \") (\\strong have host \"localhost\")) " +
//                            "(\\li (\"URI: \") (\\strong have uri \"/\"))))");
//
//
        Assert.assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>" +
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
            "</html>",
            response.getResponse()
        );
        
    	request = new HttpRequest("/favicon.ico","localhost");
    	response = new HttpResponse();

    	servlet.doGet(request, response);
    	
//        assertAnimoResult(ss, "");
    }
    
}