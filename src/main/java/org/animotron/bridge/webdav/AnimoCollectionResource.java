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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class AnimoCollectionResource implements CollectionResource {
	
	Node node;

	public AnimoCollectionResource(Node node) {
		this.node = node;
	}

	@Override
	public Resource child(String childName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() {
		// TODO Auto-generated method stub
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getUniqueId() {
		return String.valueOf( node.getId() );
	}

	@Override
	public String getName() {
		return String.valueOf( node.getId() );
	}

	@Override
	public Object authenticate(String user, String password) {
		// always allow
		return user;
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		// always allow
		return true;
	}

	@Override
	public String getRealm() {
		return AnimoResourceFactory.REALM;
	}

	@Override
	public Date getModifiedDate() {
		// unknown
		return null;
	}

	@Override
	public String checkRedirect(Request request) {
		// No redirects
		return null;
	}
}
