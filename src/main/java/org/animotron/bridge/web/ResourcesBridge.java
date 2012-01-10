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

import org.animotron.bridge.AbstractFSBridge;

import java.io.File;
import java.io.IOException;

/**
* @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
*
*/
public abstract class ResourcesBridge extends AbstractFSBridge {

    private String uriContext;

    public ResourcesBridge(String uriContext) {
        this.uriContext = uriContext;
    }

    private int root = 0;

    @Override
    public void load(String path) throws IOException {
        File f = new File(path);
        if (f.isDirectory()) {
            root = f.getPath().length();
        }
        super.load(path);
    }
    
    protected String path (File file) {
        return file.getPath().substring(root);
    }


}
