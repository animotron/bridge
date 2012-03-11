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
package org.animotron.bridge.websocket;

import org.animotron.cache.FileCache;
import org.animotron.expression.AnimoExpression;
import org.animotron.graph.serializer.CachedSerializer;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class AnimoIMS extends OnTextMessage {

	@Override
	public void onMessage(String data) {
        if (data.isEmpty())
            return;

        try {
			cnn.sendMessage(CachedSerializer.HTML_PART.serialize(new AnimoExpression(data), FileCache._));
		} catch (Throwable e) {
        	//XXX: send error message, if it come from serializer
		}
	}
}