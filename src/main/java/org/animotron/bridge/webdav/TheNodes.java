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
package org.animotron.bridge.webdav;

import java.util.List;
import java.util.UUID;

import javolution.util.FastList;

import org.animotron.graph.AnimoGraph;
import org.animotron.graph.RelationshipTypes;
import org.animotron.operator.THE;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class TheNodes extends AResource implements CollectionResource, Resolvable {
	
	public TheNodes() {
		super(UUID.randomUUID().toString(), "thes");
	}

	@Override
	public Resource child(String childName) {
		//remove ".xml"
		String name = childName.substring(1, childName.length() - 4);

		Transaction tx = AnimoGraph.beginTx();
		try {
			Relationship r = THE.getInstance().relationship(name);
			if (r == null)
				return null;
			
			return new AnimoResource(r);
		
		} catch (NotFoundException e) {
			return null;
		} finally {
			tx.finish();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends Resource> getChildren() {
		Transaction tx = AnimoGraph.beginTx();
		try {
			Node node = AnimoGraph.getOrCreateNode(AnimoGraph.getROOT(), RelationshipTypes.THE);
			
			List<AnimoResource> children = new FastList<AnimoResource>();
			
			for (Relationship r : node.getRelationships(Direction.OUTGOING)) {
				children.add(new AnimoResource(r));
			}
			return children;
		} catch (NotFoundException e) {
			e.printStackTrace();
		} finally {
			tx.finish();
		}

		return java.util.Collections.EMPTY_LIST;
	}

	@Override
	public Resource resolve(Path path) {
		String name = path.getFirst();
		
		if (path.getStripFirst().getLength() == 0)
			return child(name);

		return null;
	}
}
