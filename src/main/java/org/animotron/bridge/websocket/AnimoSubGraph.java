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

import java.io.IOException;

import org.animotron.graph.index.Order;
import org.animotron.statement.operator.THE;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;


/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class AnimoSubGraph extends OnTextMessage {

	@Override
	public void onMessage(String data) {
        if (data.isEmpty())
            return;
        
        Relationship r = THE._.get(data);
        if (r == null) return; //XXX: send error message
        
        IndexHits<Relationship> hits = Order._.queryDown(r.getEndNode());
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
