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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.animotron.Shell;

import static org.animotron.graph.AnimoGraph.startDB;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class HttpServer {
	
    private static final int HTTP_PORT = 7532;

    public static void run(int port) throws Exception {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpServerInitializer())
                .bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void init() throws Exception {
    	//initialize animo
    	if (startDB("data")) {
    		//if (!getSTART().hasRelationship(Direction.OUTGOING)) {
                new ResourcesMap("/animo").load("animo/");
                new ResourcesBridge("/binary").load("site/");
        	//}
    	}
    }
    	
//    	//setup servlet container
//        jetty = new Server(port);
//        jetty.setStopTimeout(1000);
//        jetty.setStopAtShutdown(true);
//
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        jetty.setHandler(context);
//
//        context.addServlet(new ServletHolder(new AnimoServlet()),"/*");
//        context.addServlet(new ServletHolder(new BridgeServlet()),"/binary/*");
//        context.addServlet(new ServletHolder(new MapServlet("animo/")),"/animo/*");
//        context.addServlet(new ServletHolder(new WebSocketCreatorServlet()),"/ws/*");
//
////        context.setSecurityHandler(getSecurityHandler());
//
//        // ... and start it up
//        try {
//            jetty.start();
//        } catch (Throwable t) {
//            throw new RuntimeException(t);
//        }
//
//        System.out.println(Arrays.toString(jetty.getConnectors()));
//    }

    public static void main(String[] args) throws Exception {
    	run(HTTP_PORT);
        Shell.process();
	}
}