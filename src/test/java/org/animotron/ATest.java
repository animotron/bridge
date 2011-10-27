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
package org.animotron;

import com.ctc.wstx.stax.WstxOutputFactory;
import junit.framework.Assert;
import org.animotron.exception.AnimoException;
import org.animotron.graph.GraphOperation;
import org.animotron.graph.serializer.*;
import org.animotron.manipulator.Evaluator;
import org.animotron.manipulator.PFlow;
import org.apache.log4j.helpers.NullEnumeration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.Principal;
import java.util.*;

import static org.animotron.graph.AnimoGraph.*;
import static org.junit.Assert.assertNotNull;


/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public abstract class ATest {

    public static final String DATA_FOLDER = "data-test";
	
	public static final WstxOutputFactory OUTPUT_FACTORY = new WstxOutputFactory();

	protected void toConsole(PFlow ch) throws IOException {
		//XXX: code
//		if (instream == null) return;
//		
//		Object n; 
//		while ((n = instream.read()) != null) {
//			System.out.print(n.toString());
//		} 
	}

	protected void toConsole(InputStream stream) throws IOException {
		if (stream == null) return;
		
		char[] buffer = new char[1024]; 
		try { 
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8")); 

			int n; 
			while ((n = reader.read(buffer)) != -1) {
				for (int i = 0; i < n; i++) {
					System.out.print(buffer[i]);
				}
			} 
		} finally { 
			stream.close(); 
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
        String result = AnimoSerializer.serialize(op, pretty);
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
        String result = AnimoResultSerializer.serialize(op, pretty);
        System.out.println(result);
        Assert.assertEquals("", expected, result);

        System.out.println();
    }

    protected void assertXMLResult(Relationship op, String expected) throws IOException {
        assertNotNull(op);

        System.out.println("XML Result serializer...");

        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        XMLResultSerializer.serialize(op, out);
        out.close();
        assertEquals(in, "<?xml version='1.0' encoding='UTF-8'?>"+expected);
        System.out.println();
    }

    protected void assertStringResult(Relationship op, String expected) throws IOException, InterruptedException {
        assertNotNull(op);

        System.out.println("VALUE result serializer...");
        String result = StringResultSerializer.serialize(new PFlow(Evaluator._), op);
        System.out.println(result);
        Assert.assertEquals("", expected, result);

        System.out.println();
    }

    protected void assertBinary(Relationship op, String expected) throws IOException {
        assertNotNull(op);
        System.out.println("Binary serializer...");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinarySerializer.serialize(op, out);
        String bin = out.toString();
        Assert.assertEquals("", expected, bin);
        System.out.println(bin);
        System.out.println();
    }

	//database cleaning (thanks to mh)
    public Map<String, Object> cleanDb() {
        return cleanDb(Long.MAX_VALUE);
    }

    public Map<String, Object> cleanDb(final long maxNodesToDelete) {
        Map<String, Object> result = execute(new GraphOperation<Map<String, Object>>() {
            @Override
            public Map<String, Object> execute() throws AnimoException {
                Map<String, Object> result = new HashMap<String, Object>();
                clearIndex(result);
                removeNodes(result,maxNodesToDelete);
                return result;
            }
        });
        initDB();
        return result;
    }

    private void removeNodes(Map<String, Object> result, long maxNodesToDelete) {
        Node refNode = getROOT();
        long nodes = 0, relationships = 0;
        for (Node node : getDb().getAllNodes()) {
        	boolean delete = true;
            for (Relationship rel : node.getRelationships()) {
            	if (rel.getStartNode().equals(refNode))
            		delete = false;
            	else
            		rel.delete();
                relationships++;
            }
            if (delete && !refNode.equals(node)) {
                node.delete();
                nodes++;
            }
            if (nodes >= maxNodesToDelete) break;
        }
        result.put("maxNodesToDelete", maxNodesToDelete);
        result.put("nodes", nodes);
        result.put("relationships", relationships);

    }

    private void clearIndex(Map<String, Object> result) throws AnimoException {
        IndexManager indexManager = getDb().index();
        result.put("node-indexes", Arrays.asList(indexManager.nodeIndexNames()));
        result.put("relationship-indexes", Arrays.asList(indexManager.relationshipIndexNames()));
        for (String ix : indexManager.nodeIndexNames()) {
            indexManager.forNodes(ix).delete();
        }
        for (String ix : indexManager.relationshipIndexNames()) {
            indexManager.forRelationships(ix).delete();
        }
    }

    @Before
    public void setup() {
    	cleanDb();
    }

    @After
    public void cleanup() {
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    
    @BeforeClass
    public static void start() {
    	deleteDir(new File(DATA_FOLDER));
        startDB(DATA_FOLDER);
    }

    @AfterClass
    public static void stop() {
    	shutdownDB();
    }

    protected class HttpRequest implements HttpServletRequest {

    	String requestURI;
    	String serverName;

    	public HttpRequest(String requestURI, String serverName) {
    		this.requestURI = requestURI;
    		this.serverName = serverName;
		}

		@Override
		public Object getAttribute(String name) {
			return null;
		}

		@Override
		public Enumeration getAttributeNames() {
			return null;
		}

		@Override
		public String getCharacterEncoding() {
			return null;
		}

		@Override
		public int getContentLength() {
			return 0;
		}

		@Override
		public String getContentType() {
			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public String getParameter(String name) {
			return null;
		}

		@Override
		public Locale getLocale() {
			return null;
		}

		@Override
		public Enumeration getLocales() {
			return null;
		}

		@Override
		public Map getParameterMap() {
			return null;
		}

		@Override
		public Enumeration getParameterNames() {
			return NullEnumeration.getInstance();
		}

		@Override
		public String[] getParameterValues(String name) {
			return null;
		}

		@Override
		public String getProtocol() {
			return null;
		}

		@Override
		public String getScheme() {
			return null;
		}

		@Override
		public String getServerName() {
			return serverName;
		}

		@Override
		public int getServerPort() {
			return 0;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return null;
		}

		@Override
		public String getRemoteAddr() {
			return null;
		}

		@Override
		public String getRemoteHost() {
			return null;
		}

		@Override
		public void setAttribute(String name, Object o) {}

		@Override
		public void removeAttribute(String name) {}

		@Override
		public boolean isSecure() {
			return false;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
			return null;
		}

		@Override
		public String getRealPath(String path) {
			return null;
		}

		@Override
		public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {}

		@Override
		public String getAuthType() {
			return null;
		}

		@Override
		public Cookie[] getCookies() {
			return null;
		}

		@Override
		public long getDateHeader(String name) {
			return 0;
		}

		@Override
		public String getHeader(String name) {
			return null;
		}

		@Override
		public Enumeration getHeaders(String name) {
			return null;
		}

		@Override
		public Enumeration getHeaderNames() {
			return null;
		}

		@Override
		public int getIntHeader(String name) {
			return 0;
		}

		@Override
		public String getMethod() {
			return null;
		}

		@Override
		public String getPathInfo() {
			return null;
		}

		@Override
		public String getPathTranslated() {
			return null;
		}

		@Override
		public String getContextPath() {
			return null;
		}

		@Override
		public String getQueryString() {
			return null;
		}

		@Override
		public String getRemoteUser() {
			return null;
		}

		@Override
		public boolean isUserInRole(String role) {
			return false;
		}

		@Override
		public Principal getUserPrincipal() {
			return null;
		}

		@Override
		public String getRequestURI() {
			return requestURI;
		}

		@Override
		public StringBuffer getRequestURL() {
			return null;
		}

		@Override
		public String getRequestedSessionId() {
			return null;
		}

		@Override
		public String getServletPath() {
			return null;
		}

		@Override
		public HttpSession getSession(boolean create) {
			return null;
		}

		@Override
		public HttpSession getSession() {
			return null;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			return false;
		}

    }

    protected class HttpResponse implements HttpServletResponse {

        OutputStream out;

        public HttpResponse() throws IOException {
            this(true);
		}
        
        public HttpResponse(boolean fantom) throws IOException {
        	if (fantom)
        		out = new FantomOutputStream();
        	else
        		out = new OutputStream();
		}


        public byte[] getResponse() {
        	return out.bos.toByteArray();
        }

        public String getResponseString() {
        	return out.sb.toString();
        }

        @Override
		public String getCharacterEncoding() {
			return null;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			return out;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			return null;
		}

		@Override
		public int getBufferSize() {
			return 0;
		}

		@Override
		public void flushBuffer() throws IOException {}

		@Override
		public boolean isCommitted() {
			return false;
		}

		@Override
		public void reset() {}

		@Override
		public Locale getLocale() {
			return null;
		}

		@Override
		public void resetBuffer() {}

		@Override
		public void setContentLength(int len) {}

		@Override
		public void setContentType(String type) {}

		@Override
		public void setBufferSize(int size) {}

		@Override
		public void setLocale(Locale loc) {}

		@Override
		public void addCookie(Cookie cookie) {}

		@Override
		public boolean containsHeader(String name) {
			return false;
		}

		@Override
		public String encodeURL(String url) {
			return null;
		}

		@Override
		public String encodeRedirectURL(String url) {
			return null;
		}

		@Override
		public String encodeUrl(String url) {
			return null;
		}

		@Override
		public String encodeRedirectUrl(String url) {
			return null;
		}

		@Override
		public void sendError(int sc, String msg) throws IOException {}

		@Override
		public void sendError(int sc) throws IOException {}

		@Override
		public void sendRedirect(String location) throws IOException {}

		@Override
		public void setDateHeader(String name, long date) {}

		@Override
		public void addDateHeader(String name, long date) {}

		@Override
		public void setHeader(String name, String value) {}

		@Override
		public void addHeader(String name, String value) {}

		@Override
		public void setIntHeader(String name, int value) {}

		@Override
		public void addIntHeader(String name, int value) {}

		@Override
		public void setStatus(int sc) {}

		@Override
		public void setStatus(int sc, String sm) {}

    }

    private class OutputStream extends ServletOutputStream {

    	StringBuilder sb = new StringBuilder(1024);
    	
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		DataOutputStream dos = new DataOutputStream(bos);  

		@Override
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

}
