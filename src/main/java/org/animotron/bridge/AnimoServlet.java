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
package org.animotron.bridge;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.animotron.Statements;
import org.animotron.graph.GraphBuilder;
import org.animotron.graph.GraphSerializer;
import org.animotron.operator.AN;
import org.neo4j.graphdb.Relationship;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class AnimoServlet extends HttpServlet {

	private static final long serialVersionUID = 7276574723383015880L;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		AnimoRequest request = new AnimoRequest(req.getRequestURI());
		//String name = req.getParameter("name");
		
		Relationship r = request.getRelationship();
		
		res.setContentType("text/html");
		OutputStream out = res.getOutputStream();
		
		try {
			GraphSerializer.serialize(r, out);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	private static AN op = AN._;
	
	class AnimoRequest extends GraphBuilder {

		
		public AnimoRequest(String uri) {
			String[] parts = uri.split("/");
			System.out.println(Arrays.toString(parts));
			

			startGraph();
			for (String part : parts) {
				if (part.isEmpty()) continue;
				
				String prefix, ns, name;
				
				String[] s = part.split(":", 1);
				if (s.length == 1) {
					prefix = op.name();
					ns = op.namespace();
					name = s[0];
				} else {
					prefix = s[0];
					ns = Statements.prefix(prefix).namespace();
					name = part.substring(prefix.length());
				}

				start(prefix, ns, name, null);
			}
			for (String part : parts) {
				if (part.isEmpty()) continue;
				end();
			}
			endGraph();
		}
	}
}
