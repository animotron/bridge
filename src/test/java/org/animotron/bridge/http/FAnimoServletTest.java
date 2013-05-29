/*
 *  Copyright (C) 2011-2013 The Animo Project
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
package org.animotron.bridge.http;

import org.animotron.ATest;
import org.animotron.bridge.FSBridge;
import org.animotron.bridge.ResourcesBridge;
import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class FAnimoServletTest extends ATest {

    private void test(AnimoHandler servlet, String uri) throws IOException {
//        HttpRequest request = new HttpRequest(uri, "localhost");
//        HttpResponse response = new HttpResponse(false);
//        servlet.doGet(request, response);
//        assertArrayEquals(getBytesFromFile(new File("src/test/resources/site/localhost/favicon.ico")), response.getResponse());
    }

    @Test
    public void test() throws Throwable {
        FSBridge._.load("src/test/resources/animo/");
        new ResourcesBridge("/binary").load("src/test/resources/site/");
        AnimoHandler servlet = new AnimoHandler();
        test(servlet, "/favicon");
        test(servlet, "/favicon/ico");
        test(servlet, "/favicon.ico");
        test(servlet, "/ico");
        test(servlet, "/ico/favicon");
        test(servlet, "/ico.favicon");
    }

}
