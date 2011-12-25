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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ServletTest extends ATest {

    @Test
    public void test() throws Exception {

    	FSBridge.load("src/test/animo/");
    	
    	AnimoServlet servlet = new AnimoServlet();
    	
    	HttpRequest request = new HttpRequest("/","localhost");
    	HttpResponse response = new HttpResponse();

    	servlet.doGet(request, response);

        Assert.assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>" +
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
    
    @Test
    public void testBinay() throws Exception {
    	FSBridge.load("src/test/animo/");
    	
    	AnimoServlet servlet = new AnimoServlet();
    	
    	HttpRequest request = new HttpRequest("/favicon.ico","localhost");
    	HttpResponse response = new HttpResponse();

    	servlet.doGet(request, response);
    	
    	org.junit.Assert.assertArrayEquals(getBytesFromFile(new File("src/test/animo/localhost/favicon.ico")),response.getResponse());
    }
    
    
    private byte[] getBytesFromFile(File file) throws IOException {
    	InputStream is = new FileInputStream(file);

        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            throw new RuntimeException("File too big ["+file.getPath()+"]");
        }

        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }
    
}