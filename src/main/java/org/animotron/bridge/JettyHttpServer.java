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

import org.animotron.bridge.webdav.WebDAVServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;

import static org.animotron.graph.AnimoGraph.startDB;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class JettyHttpServer {

	private Server jetty;
    private int jettyPort = 8080;
    
    public JettyHttpServer() {}

	public JettyHttpServer(int port) {
    	jettyPort = port;
	}

    public void start() throws IOException {
    	
    	//initialize animo
    	startDB("data");

//        if (!getSTART().hasRelationship(Direction.OUTGOING)) {
    	    FSBridge.load("src/main/animo/");
//        }
    	
    	//setup servlet container
        jetty = new Server(jettyPort);
        jetty.setStopAtShutdown(true);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        jetty.setHandler(context);
 
        context.addServlet(new ServletHolder(new AnimoServlet()),"/*");
        context.addServlet(new ServletHolder(new WebDAVServlet()),"/webdav/*");
        
        //context.getSecurityHandler().setLoginService(new AnimoLoginService());
        
        // ... and start it up
        try {
            jetty.start();
            jetty.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            jetty.stop();
            jetty.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws IOException {
    	JettyHttpServer server = new JettyHttpServer();
    	
    	server.start();
    	
    	while (true) {
    		try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
    	}
	}
}
