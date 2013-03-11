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
package org.animotron.bridge;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.security.MappedLoginService.UserPrincipal;
import org.neo4j.graphdb.Relationship;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Principal implements UserPrincipal {
	
	private static final long serialVersionUID = -8124711283293498187L;

	private Relationship def;
	
	public Principal(Relationship account) {
		assert account != null;
		
		this.def = account;
	}

	@Override
	public String getName() {
		System.out.println(def.toString());
		return def.toString();
	}

	public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Principal) {
        	Principal that = (Principal) o;
            if (def == null ? that.def == null : def.equals(that.def)) {
                return true;
            }
        }
        return false;
	}

	@Override
	public boolean authenticate(Object credentials) {
		System.out.println("authenticate");
		System.out.println(credentials);
		System.out.println();
		
		byte[] result = getMac().doFinal(credentials.toString().getBytes());

		System.out.println(Base64.encodeBase64String(result));
		
		return false;
	}

	@Override
	public boolean isAuthenticated() {
		System.out.println("isAuthenticated");
		return false;
	}
	
	private final SecretKeySpec keySpec = new SecretKeySpec(
	        "qnscAdgRlkIhAUPY44oiexBKtQbGY0orf7OV1I50".getBytes(),
	        "HmacSHA1");

	//XXX: RIPEMD160
	private Mac getMac() {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(keySpec);
			return mac;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}