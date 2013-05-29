/*
 *  Copyright (C) 2011-2013 The Animo Project
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
package org.animotron.bridge.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.animotron.Shell;
import org.animotron.bridge.ResourcesBridge;
import org.animotron.bridge.ResourcesMap;
import org.animotron.cache.Cache;
import org.animotron.cache.FileCache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.animotron.graph.AnimoGraph.startDB;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class HttpServer {
	
    private static final int HTTP_PORT = 8888;

    public static final Cache CACHE = FileCache._;

    protected static final String ANIMO_CONTEXT_URI = "/animo/";
    protected static final String BINARY_CONTEXT_URI = "/binary/";

    protected static final File ANIMO_FOLDER = new File("animo");
    protected static final File SITE_FOLDER = new File("site");

    private static void run(int port) throws Throwable {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new Initializer())
                .bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static void init() throws Exception {
    	if (startDB("data")) {
    		//if (!getSTART().hasRelationship(Direction.OUTGOING)) {
                new ResourcesMap(ANIMO_CONTEXT_URI).load(ANIMO_FOLDER);
                new ResourcesBridge(BINARY_CONTEXT_URI).load(SITE_FOLDER);
        	//}
    	}
    }
    	
	private static void start() throws Throwable {
    	init();
    	run(HTTP_PORT);
	}

	public static void main(String[] args) {
        try {
            start();
            Shell.process();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
	}

    private static class Initializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(
                    new HttpRequestDecoder(),
                    new HttpObjectAggregator(1048576),
                    new HttpResponseEncoder(),
                    new ChunkedWriteHandler(),
                    //new HttpContentCompressor(),
                    new HttpServerHandler()
            );
        }
    }

}