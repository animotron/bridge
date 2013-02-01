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
package org.animotron.bridge.websocket;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class WebSocketServlet extends HttpServlet {

	private static final long serialVersionUID = -1773286872178450453L;

	private WebSocketFactory factory;
    
	@Override
	public void init() throws ServletException {

		// Create and configure WS factory
		factory = new WebSocketFactory(new WebSocketFactory.Acceptor() {
			public boolean checkOrigin(HttpServletRequest request, String origin) {
				System.out.println("checkOrigin "+origin);
				// Allow all origins
				return true;
			}

			public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {

                if ("src".equals(protocol))
                    return new SourceAnimo();

                if ("search".equals(protocol))
                    return new SearchAnimo();

                if ("save".equals(protocol))
                    return new SaveAnimo();

                if ("eval".equals(protocol))
                    return new EvalAnimo();

                if ("graph".equals(protocol))
                    return new AnimoSubGraph();
				
                if ("animoIMS".equals(protocol))
                    return new AnimoIMS();

                return null;
			}
		});

		factory.setBufferSize(4096);
		factory.setMaxIdleTime(60000);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (factory.acceptWebSocket(request, response)) {
			return;
        }
		response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Websocket only");
	}
}