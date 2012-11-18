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

import org.animotron.Executor;
import org.animotron.cache.FileCache;
import org.animotron.expression.AnimoExpression;
import org.animotron.expression.Expression;
import org.animotron.graph.AnimoGraph;
import org.animotron.graph.serializer.CachedSerializer;
import org.animotron.io.Pipe;
import org.animotron.manipulator.Evaluator;
import org.animotron.manipulator.QCAVector;
import org.animotron.statement.operator.DEF;
import org.animotron.statement.operator.Utils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class SearchAnimo extends OnTextMessage {

    private Pipe pipe = null;

    @Override
    public void onMessage(final String data) {

        if (data.isEmpty())
            return;

        Executor.execute(new Runnable() {

            private void sendThes (Relationship  r) throws Throwable {
                Iterator<Path> it =  Utils.THES.traverse(r.getEndNode()).iterator();
                while(it.hasNext()) {
                    cnn.sendMessage(CachedSerializer.PRETTY_ANIMO.serialize(it.next().lastRelationship(), FileCache._));
                }
            }

            private void sendThes (Node n) throws Throwable {
                for (Relationship r : n.getRelationships(Direction.INCOMING)) {
                    sendThes(r);
                }
            }

            private void sendThes (Expression  e) throws Throwable {
                int i = 0;
                QCAVector v;
                pipe = Evaluator._.execute(null, e);
                while ((v = pipe.take()) != null && i < 100) {
//                    sendThes(v.getClosest());
                    cnn.sendMessage(CachedSerializer.PRETTY_ANIMO.serialize(v.getClosest(), FileCache._));
                    i++;
                }
            }

            @Override
            public void run() {
                String exp = data.trim();
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
                            cnn.sendMessage(CachedSerializer.PRETTY_ANIMO.serialize(r, FileCache._));
                        } else {
                            if (exp.indexOf(" ") > 0) {
                                sendThes(new AnimoExpression(exp));
                            }
                        }
                    } catch (Throwable t) {}
                } catch (Throwable t) {}
            }
        });
    }
}
