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

//import com.meterware.httpunit.GetMethodWebRequest;
//import com.meterware.httpunit.WebConversation;
//import com.meterware.httpunit.WebRequest;
//import com.meterware.httpunit.WebResponse;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class SimpleTest {
	
//	private ServletUnitClient client;
//	
//	public SimpleTest() {
//		ServletRunner sr = new ServletRunner();
//        sr.registerServlet("greeter", "org.animotron.bridge.AnimoServlet");
//        client = sr.newClient();	
//    }
//
//	@Test
//	public void test() throws IOException, SAXException {
//		WebResponse response = client.getResponse("http://localhost/greeter");
//        String greeting = ((TextBlock[]) response.getTextBlocks())[0].getText();
//        assertEquals("Incorrect greeting returned", "Bonjour, tout le monde!", greeting.trim());	
//    }
	
	@Test
	public void test2() throws IOException, SAXException {
		JettyHttpServer server = new JettyHttpServer(7770);
		
		server.start();
		
//		WebConversation wc = new WebConversation();
//	    WebRequest     req = new GetMethodWebRequest( "http://localhost:7770/" );
//	    WebResponse   resp = wc.getResponse( req );
//	    System.out.println(resp.toString());

		server.stop();
	}

}
