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

import java.util.UUID;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Root extends ACollection implements Resolvable {
	
	public Root() {
		super(UUID.randomUUID().toString(), "");
		
//		addChild(new ACollection(UUID.randomUUID().toString(), "repo"));
//		addChild(new ACollection(UUID.randomUUID().toString(), "reso"));
//		addChild(new ACollection(UUID.randomUUID().toString(), "upld"));
		addChild(new TheNodes());
	}

	public Resource resolve(Path path) {
		String direction = path.getFirst();
		
		Resource child = child(direction);
		if (child == null)
			return null;
		
		path = path.getStripFirst();
		if (path.getLength() == 0)
			return child;
		
		if (child instanceof Resolvable)
			return ((Resolvable) child).resolve(path);

		return null;
	}
}