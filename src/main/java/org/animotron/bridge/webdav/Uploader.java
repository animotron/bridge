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

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.animotron.expression.CommonExpression;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Uploader extends AResource implements CollectionResource, Resolvable, MakeCollectionableResource, ReplaceableResource {
	
	public Uploader() {
		super(UUID.randomUUID().toString(), "upld");
	}

	private Uploader(String name) {
		super(UUID.randomUUID().toString(), name);
	}

	private Map<String, Uploader> children = new FastMap<String, Uploader>(); 
	
	public Uploader newChild(String childName) {
		Uploader r = children.get(childName);
		if (r != null) return r;

		r = new Uploader(childName);
		children.put(childName, r);
		
		return r;
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
	public Resource resolve(Path path) {
		System.out.println("Uploader name = "+name+" path "+path);
//		return child(path.getName());
		String name = path.getFirst();
		
		Resource child = child(name);
		if (child == null) return null;
		
		path = path.getStripFirst();
		if (path.getStripFirst().getLength() == 0)
			return child;

		return ((Uploader) child).resolve(path);
	}

	@Override
	public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException {
		return (CollectionResource) newChild(newName);
	}

	@Override
	public void replaceContent(final InputStream in, Long length) {
        try {
            new CommonExpression(in, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
