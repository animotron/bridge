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

import org.animotron.expression.AnimoExpression;
import org.animotron.expression.Expression;
import org.animotron.expression.JExpression;
import org.animotron.graph.AnimoGraph;
import org.animotron.graph.index.Order;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.io.Pipe;
import org.animotron.manipulator.Evaluator;
import org.animotron.manipulator.QCAVector;
import org.animotron.statement.operator.THE;
import org.animotron.statement.operator.Utils;
import org.animotron.statement.query.ALL;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import static org.animotron.expression.JExpression._;

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

                else if ("save".equals(protocol))
                    return new SaveAnimo();

                else if ("graph".equals(protocol))
                    return new AnimoSubGraph();
				
                else if ("animoIMS".equals(protocol))
                    return new AnimoIMS();

                return null;
			}
		});

		factory.setBufferSize(4096);
		factory.setMaxIdleTime(7 * 60 * 1000);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (factory.acceptWebSocket(request, response)) {
			return;
        }
		response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Websocket only");
	}

    abstract class OnTextMessage implements WebSocket.OnTextMessage {
        Connection cnn;
        @Override
        public void onOpen(Connection connection) {
            cnn = connection;
        }
        @Override
        public void onClose(int closeCode, String message) {
        }
        public void sendError(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            try {
                cnn.sendMessage(sw.toString());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

	private class SourceAnimo extends OnTextMessage {
        @Override
        public void onMessage(String data) {
            if (data.isEmpty())
                return;
            try {
                Relationship r = THE._.get(data);
                if (r != null) {
                    cnn.sendMessage(CachedSerializer.PRETTY_ANIMO.serialize(r));
                } else {
                    //XXX: send error message
                }
            } catch (IOException e) {
            	sendError(e);
            }
        }
    }

    private class AnimoSubGraph extends OnTextMessage {
		@Override
		public void onMessage(String data) {
            if (data.isEmpty())
                return;
            Relationship r = THE._.get(data);
            if (r == null) return; //XXX: send error message
            IndexHits<Relationship> hits = Order.queryDown(r.getEndNode());
            try {
            	for (Relationship rr : hits) {
					cnn.sendMessage(rr.getType().name());
            	}
			} catch (IOException e) {
            } finally {
            	hits.close();
            }
		}
    }

    private class AnimoIMS extends OnTextMessage {

		@Override
		public void onMessage(String data) {
			System.out.println("AnimoIMS "+data);
            try {
				cnn.sendMessage(CachedSerializer.HTML_PART.serialize(new AnimoExpression(data)));
			} catch (IOException e) {
            	//XXX: send error message, if it come from serializer
			}
		}
    	
    }

    private class SaveAnimo extends OnTextMessage {
        @Override
        public void onMessage(String data) {
            if (data.isEmpty())
                return;
            try {
                Expression e = new AnimoExpression(data);
                cnn.sendMessage(CachedSerializer.PRETTY_ANIMO.serialize(e));
            } catch (IOException e) {
                //XXX: send error message
            }
        }
    }

    private class SearchAnimo extends OnTextMessage {

        Thread thread = null;

        private void sendThes (Relationship  r) throws IOException {
            if (r == null)
                return;
            Iterator<Path> it = Utils.THES.traverse(r.getEndNode()).iterator();
            while(it.hasNext()) {
                cnn.sendMessage(CachedSerializer.ANIMO_RESULT_ONE_STEP.serialize(it.next().lastRelationship()));
            }
        }

        private void sendThes (Expression  e) throws IOException {
            int i = 0;
            QCAVector v;
            Pipe pipe = Evaluator._.execute(null, e);
            while ((v = pipe.take()) != null && i < 100) {
                sendThes(v.getClosest());
                i++;
            }
        }

        @Override
        public void onMessage(final String data) {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            if (data.isEmpty())
                return;
            thread = new Thread() {
                @Override
                public void run() {
                    String exp = data.trim();
                    try {
                        long rid = Long.valueOf(exp);
                        sendThes(AnimoGraph.getDb().getRelationshipById(rid));
                    } catch (NumberFormatException nfe) {
                        Relationship r = THE._.get(exp);
                        try {
                            if (r != null) {
                                cnn.sendMessage(CachedSerializer.ANIMO_RESULT_ONE_STEP.serialize(r));
                                sendThes(new JExpression(_(ALL._, r)));
                            } else {
                                if (exp.indexOf(" ") > 0) {
                                    sendThes(new AnimoExpression(exp));
                                }
                            }
                        } catch (IOException e) {
                            sendError(e);
                        }
                    } catch (NotFoundException e) {
                        sendError(e);
                    } catch (IOException e) {
                        sendError(e);
                    }
                }
            };
        }
        
    }
    
}
