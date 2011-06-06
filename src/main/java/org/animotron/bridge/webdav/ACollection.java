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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class ACollection extends AResource implements CollectionResource {
	
	private Map<String, Resource> children = new FastMap<String, Resource>();
	
	public ACollection(String id, String name) {
		super(id, name);
	}
	
	public void addChild(Resource child) {
		children.put(child.getName(), child);
	}

	@Override
	public Resource child(String childName) {
		return children.get(childName);
	}

	@Override
	public List<? extends Resource> getChildren() {
		return new FastList<Resource>(children.values()); 
	}

	@Override
	public Date getCreateDate() {
		// unknown
		return null;
	}
}
