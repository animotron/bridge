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

import java.io.InputStream;
import java.util.Date;

import javax.xml.stream.XMLStreamException;

import org.animotron.graph.AnimoGraph;
import org.animotron.graph.CommonGraphBuilder;
import org.animotron.graph.GraphOperation;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class UploadResource implements ReplaceableResource {
	
	String name;
	
	public UploadResource(Path path) {
		name = path.getName();
	}

	public UploadResource(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#getUniqueId()
	 */
	@Override
	public String getUniqueId() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public Object authenticate(String user, String password) {
		// always allow
		return user;
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#authorise(com.bradmcevoy.http.Request, com.bradmcevoy.http.Request.Method, com.bradmcevoy.http.Auth)
	 */
	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		// always allow
		return true;
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#getRealm()
	 */
	@Override
	public String getRealm() {
		return AnimoResourceFactory.REALM;
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#getModifiedDate()
	 */
	@Override
	public Date getModifiedDate() {
		// unknown
		return null;
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#checkRedirect(com.bradmcevoy.http.Request)
	 */
	@Override
	public String checkRedirect(Request request) {
		// No redirects
		return null;
	}

	@Override
	public void replaceContent(final InputStream in, Long length) {
		AnimoGraph.execute( new GraphOperation<XMLStreamException>() {
			@Override
			public XMLStreamException execute() {
				try {
					CommonGraphBuilder.build(in);
			        
			        return null;
				} catch (XMLStreamException e) {
					e.printStackTrace();
					return e;
				}
			}
		});
	}

}
