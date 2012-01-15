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

import org.animotron.bridge.FSBridge;
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
    	if (startDB("data")) {
    		//if (!getSTART().hasRelationship(Direction.OUTGOING)) {
                FSBridge._.load("animo/");
                FSBridge._.load("apps/");
                FSBridge._.load("etc/");
                new ResourcesBridge("/binary").load("site/");
                new ResourcesMap("/common").load("common/");
                new ResourcesMap("/theme").load("theme/");
        	//}
    	}
    	
    	//setup servlet container
        jetty = new Server(jettyPort);
        jetty.setStopAtShutdown(true);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        jetty.setHandler(context);

        context.addServlet(new ServletHolder(new AnimoServlet()),"/*");
        context.addServlet(new ServletHolder(new BridgeServlet()),"/binary/*");
        context.addServlet(new ServletHolder(new MapServlet("common/")),"/common/*");
        context.addServlet(new ServletHolder(new MapServlet("theme/")),"/theme/*");
        context.addServlet(new ServletHolder(new WebSocketServlet()),"/ws/*");
        
        //context.getSecurityHandler().setLoginService(new AnimoLoginService());
        
        // ... and start it up
        try {
            jetty.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            jetty.stop();
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
