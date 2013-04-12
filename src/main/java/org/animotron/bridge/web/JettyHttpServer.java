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

import org.animotron.Shell;
import org.animotron.bridge.FSBridge;
import org.animotron.bridge.websocket.WebSocketCreatorServlet;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;

import java.util.Arrays;

import static org.animotron.graph.AnimoGraph.startDB;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class JettyHttpServer {
	
	private static final String REALM = "animotron";

    private Server jetty;
    private static final int JettyPort = 7532;
    private int jettyPort;

    public JettyHttpServer() {
        this(JettyPort);
    }

	public JettyHttpServer(int port) {
    	jettyPort = port;
	}

    public void start() throws Exception {
    	
    	//initialize animo
    	if (startDB("data")) {
    		//if (!getSTART().hasRelationship(Direction.OUTGOING)) {
                new ResourcesMap("/animo").load("animo/");
                new ResourcesBridge("/binary").load("site/");
        	//}
    	}
    	
    	//setup servlet container
        jetty = new Server(jettyPort);
        jetty.setStopTimeout(1000);
        jetty.setStopAtShutdown(true);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        jetty.setHandler(context);

        context.addServlet(new ServletHolder(new AnimoServlet()),"/*");
        context.addServlet(new ServletHolder(new BridgeServlet()),"/binary/*");
        context.addServlet(new ServletHolder(new MapServlet("animo/")),"/animo/*");
        context.addServlet(new ServletHolder(new WebSocketCreatorServlet()),"/ws/*");

//        context.setSecurityHandler(getSecurityHandler());
        
        // ... and start it up
        try {
            jetty.start();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        System.out.println(Arrays.toString(jetty.getConnectors()));
    }
    
    private SecurityHandler getSecurityHandler() throws Exception {

        // add authentication
        Constraint constraint = new Constraint(Constraint.__BASIC_AUTH,"user");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"user","admin"});

        // map the security constraint to the root path.
        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        // create the security handler, set the authentication to Basic
        // and assign the realm.
        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName(REALM);
        csh.addConstraintMapping(cm);

        // set the login service
        csh.setLoginService(getLoginService());

        return csh;

    }
    
    private AnimoLoginService getLoginService() throws Exception {
    	AnimoLoginService ls = new AnimoLoginService();
    	ls.start();
    	return ls;
    }

    public void stop() {
        try {
            jetty.stop();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
    
    public static void main(String[] args) throws Exception {
    	JettyHttpServer server = new JettyHttpServer();
    	server.start();
        Shell.process();
	}
}