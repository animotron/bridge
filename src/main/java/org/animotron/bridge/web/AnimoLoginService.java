/*
 *  Copyright (C) 2011-2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.animotron.bridge.web;

import org.animotron.bridge.Principal;
import org.animotron.statement.operator.DEF;
import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.neo4j.graphdb.Relationship;

import javax.security.auth.Subject;
import java.io.IOException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class AnimoLoginService extends MappedLoginService {

	/* (non-Javadoc)
	 * @see org.eclipse.jetty.security.MappedLoginService#loadUser(java.lang.String)
	 */
	@Override
	protected UserIdentity loadUser(String username) {
		System.out.println("loadUser(String username)");
		
		Relationship account = DEF._.get(username);

		if (account == null)
			return null;
		
		return new UI(account);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jetty.security.MappedLoginService#loadUsers()
	 */
	@Override
	protected void loadUsers() throws IOException {
		System.out.println("loadUsers()");
	}
	
	class UI implements UserIdentity {

		Relationship account;
		
		public UI(Relationship account) {
			this.account = account;
		}

		@Override
		public Subject getSubject() {
			System.out.println("getSubject");
			return null;
		}

		@Override
		public Principal getUserPrincipal() {
			return new Principal(account);
		}

		@Override
		public boolean isUserInRole(String role, Scope scope) {
			System.out.println("isUserInRole");
			return false;
		}
	}

}
