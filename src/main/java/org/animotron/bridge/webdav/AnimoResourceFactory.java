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

import org.animotron.exist.index.AnimoIndex;
import org.neo4j.graphdb.GraphDatabaseService;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class AnimoResourceFactory implements ResourceFactory {
	
	protected static final String NAME = "org.animotron.bridge.webdav.AnimoResourceFactory";
	
	protected static final String REALM = "animo"; 
	
	//private final SessionFactory sessionFactory;
	
	GraphDatabaseService graphDb;
	
	public AnimoResourceFactory() {
		graphDb = AnimoIndex.graphDb;
		
		System.out.println("running AnimoResourceFactory");
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.ResourceFactory#getResource(java.lang.String, java.lang.String)
	 */
	@Override
	public Resource getResource(String host, String p) {
		Path path = Path.path(p).getStripFirst();
		
		//Session session = sessionFactory.openSession();
		
		if( path.isRoot() ) {
            return new AnimoCollectionResource(graphDb.getReferenceNode()); 
        } else {
            return null;
        }
	}
}
