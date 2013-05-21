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
package org.animotron;

import junit.framework.Assert;
import org.animotron.expression.AnimoExpression;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.Relationship;

import java.io.*;
import java.util.*;

import static org.animotron.graph.AnimoGraph.shutdownDB;
import static org.animotron.graph.AnimoGraph.startDB;
import static org.animotron.graph.serializer.Serializer.*;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public abstract class ATest {

    public static final String DATA_FOLDER = "data-test";

    protected byte[] getBytesFromFile(File file) throws IOException {
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            throw new RuntimeException("File too big ["+file.getPath()+"]");
        }

        InputStream is = new FileInputStream(file);

        try {
	        byte[] bytes = new byte[(int)length];
	
	        int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length
	                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	            offset += numRead;
	        }
	
	        if (offset < bytes.length) {
	            throw new IOException("Could not completely read file "+file.getName());
	        }
	        return bytes;
        } finally {
        	is.close();
        }
    }

	protected void assertEquals(InputStream stream, String expecteds) throws IOException {
		if (stream == null) return;
		
		StringBuilder b = new StringBuilder(expecteds.length()); 
		
		char[] buffer = new char[1024]; 
		try { 
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8")); 

			int n; 
			while ((n = reader.read(buffer)) != -1) {
				for (int i = 0; i < n; i++) {
					System.out.print(buffer[i]);
					b.append(buffer[i]);
				}
			} 
		} finally { 
			stream.close(); 
		}
		
		Assert.assertEquals("check evaluation result", expecteds, b.toString());
	}

    protected void assertAnimo(Relationship op, String expected) throws IOException {
        assertAnimo(op, expected, false);
    }

    protected void assertAnimo(Relationship op, String expected, boolean pretty) throws IOException {
        assertNotNull(op);

        System.out.println("Animo serializer...");
        String result = pretty ? PRETTY_ANIMO.serialize(op) : ANIMO.serialize(op);
        System.out.println(result);
        Assert.assertEquals("", expected, result);

        System.out.println();
    }

    protected void assertAnimoResult(Relationship op, String expected) throws IOException {
        assertAnimoResult(op, expected, false);
    }

    protected void assertAnimoResult(Relationship op, String expected, boolean pretty) throws IOException {
        assertNotNull(op);

        System.out.println("Animo result serializer...");
        String result = pretty ? PRETTY_ANIMO_RESULT.serialize(op) : ANIMO_RESULT.serialize(op);
        System.out.println(result);
        Assert.assertEquals("", expected, result);

        System.out.println();
    }

    protected void assertStringResult(String op, String expected) throws IOException, InterruptedException {
    	assertStringResult(new AnimoExpression(op), expected);
    }

    protected void assertStringResult(Relationship op, String expected) throws IOException, InterruptedException {
        assertNotNull(op);

        System.out.println("VALUE result serializer...");
        String result = STRING.serialize(op);
        System.out.println(result);
        Assert.assertEquals("", expected, result);

        System.out.println();
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (String aChildren : dir.list()) {
                deleteDir(new File(dir, aChildren));
            }
        }
        dir.delete();
    }

    public void cleanDB() {
        shutdownDB();
        deleteDir(new File(DATA_FOLDER));
    }

    @Before
    public void start() {
        cleanDB();
        startDB(DATA_FOLDER);
    }

    @After
    public void stop() {
        shutdownDB();
    }

    private class OutputStream {

    	StringBuilder sb = new StringBuilder(1024);
    	
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		DataOutputStream dos = new DataOutputStream(bos);  

		public void write(int b) throws IOException {
			dos.write(b);
			sb.append((char)b);
		}
    }
    
    private class FantomOutputStream extends OutputStream {
		public void write(int b) throws IOException {
			;
		}
    }

    private static class EmptyEnumeration<E> implements Enumeration<E> {
        static final EmptyEnumeration<Object> EMPTY_ENUMERATION
            = new EmptyEnumeration<Object>();

        public boolean hasMoreElements() { return false; }
        public E nextElement() { throw new NoSuchElementException(); }
    }

    @SuppressWarnings("unchecked")
	public static <T> Enumeration<T> emptyEnumeration() {
        return (Enumeration<T>) EmptyEnumeration.EMPTY_ENUMERATION;
    }
}
