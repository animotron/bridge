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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.animotron.graph.AnimoGraph;
import org.animotron.graph.stax.StAXGraphSerializer;
import org.animotron.operator.THE;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class AnimoResource implements GetableResource {
	
	Relationship r;
	
	public AnimoResource(Relationship r) {
		this.r = r;
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#getUniqueId()
	 */
	@Override
	public String getUniqueId() {
		Transaction tx = AnimoGraph.beginTx();
		try {
			return String.valueOf( r.getId() );
		} finally {
			tx.finish();
		}
	}

	/* (non-Javadoc)
	 * @see com.bradmcevoy.http.Resource#getName()
	 */
	@Override
	public String getName() {
		Transaction tx = AnimoGraph.beginTx();
		try {
			return THE.getInstance().name(r);
		} finally {
			tx.finish();
		}
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
	public void sendContent(OutputStream out, Range range,
			Map<String, String> params, String contentType) throws IOException,
			NotAuthorizedException, BadRequestException {
		
		Transaction tx = AnimoGraph.beginTx();
		try {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
	        XMLStreamWriter writer = factory.createXMLStreamWriter(out);
	        
	        StAXGraphSerializer serializer = new StAXGraphSerializer(writer);
	        serializer.serialize(r);
	        
		} catch (XMLStreamException e) {
			throw new IOException(e);
		} finally {
			tx.finish();
		}
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		// caching not allowed
		return null;
	}

	@Override
	public String getContentType(String accepts) {
		return "application/xml";
	}

	@Override
	public Long getContentLength() {
		// unknown
		return null;
	}

}
