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

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Uploader extends AResource implements CollectionResource, Resolvable {
	
	public Uploader() {
		super(UUID.randomUUID().toString(), "upld");
	}

	@Override
	public Resource child(String childName) {
		return new UploadResource(childName);
	}

	@Override
	public List<? extends Resource> getChildren() {
		return java.util.Collections.EMPTY_LIST;
	}

	@Override
	public Resource resolve(Path path) {
		return child(path.getName());
//		String name = path.getFirst();
//		
//		if (path.getStripFirst().getLength() == 0)
//			return child(name);
//
//		return null;
	}
}
