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
package org.animotron.bridge.http.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.animotron.expression.AnimoExpression;
import org.animotron.expression.Expression;
import org.animotron.graph.AnimoGraph;
import org.animotron.io.Pipe;
import org.animotron.manipulator.Evaluator;
import org.animotron.manipulator.QCAVector;
import org.animotron.statement.operator.DEF;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;

import static org.animotron.Executor.execute;
import static org.animotron.bridge.http.HttpServer.CACHE;
import static org.animotron.graph.serializer.Serializer.PRETTY_ANIMO;
import static org.animotron.statement.operator.Utils.THES;
import static org.neo4j.graphdb.Direction.INCOMING;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class SearchAnimo {

        public static void handle(final ChannelHandlerContext ctx, final TextWebSocketFrame frame) {

            if (frame.text().isEmpty())
                return;

            execute(new Runnable() {

                Pipe pipe = null;

                private void sendThes(Relationship r) throws Throwable {
                    Iterator<Path> it = THES.traverse(r.getEndNode()).iterator();
                    while (it.hasNext()) {
                        ctx.channel().write(
                                new TextWebSocketFrame(
                                        PRETTY_ANIMO.serialize(it.next().lastRelationship(), CACHE)));
                    }
                }

                private void sendThes(Node n) throws Throwable {
                    for (Relationship r : n.getRelationships(INCOMING)) {
                        sendThes(r);
                    }
                }

                private void sendThes(Expression e) throws Throwable {
                    int i = 0;
                    QCAVector v;
                    pipe = Evaluator._.execute(null, e);
                    while ((v = pipe.take()) != null && i < 100) {
//                    sendThes(v.getClosest());
                        ctx.channel().write(
                                new TextWebSocketFrame(
                                        PRETTY_ANIMO.serialize(v.getClosest(), CACHE)));
                        i++;
                    }
                }

                @Override
                public void run() {
                    String exp = frame.text().trim();
                    try {
                        String e = exp.toLowerCase();
                        if (e.startsWith("node ")) {
                            sendThes(AnimoGraph.getDb().getNodeById(Long.valueOf(e.substring(5))));
                        } else {
                            if (e.startsWith("relationship ")) {
                                e = e.substring(13);
                            }
                            sendThes(AnimoGraph.getDb().getRelationshipById(Long.valueOf(e)));
                        }
                    } catch (NumberFormatException nfe) {
                        Relationship r = DEF._.get(exp);
                        try {
                            if (r != null) {
                                ctx.channel().write(
                                        new TextWebSocketFrame(
                                                PRETTY_ANIMO.serialize(r, CACHE)));
                            } else {
                                if (exp.indexOf(" ") > 0) {
                                    sendThes(new AnimoExpression(exp));
                                }
                            }
                        } catch (Throwable t) {
                        }
                    } catch (Throwable t) {
                    }
                }
            });


        }


}
