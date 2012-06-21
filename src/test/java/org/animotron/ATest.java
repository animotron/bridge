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
import org.animotron.graph.serializer.BinarySerializer;
import org.animotron.graph.serializer.CachedSerializer;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.Relationship;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import java.io.*;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.animotron.graph.AnimoGraph.shutdownDB;
import static org.animotron.graph.AnimoGraph.startDB;
import static org.junit.Assert.assertNotNull;


/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public abstract class ATest {

    public static final String DATA_FOLDER = "data-test";

    protected byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            throw new RuntimeException("File too big ["+file.getPath()+"]");
        }

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

        is.close();
        return bytes;
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
        String result = pretty ? CachedSerializer.PRETTY_ANIMO.serialize(op) : CachedSerializer.ANIMO.serialize(op);
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
        String result = pretty ? CachedSerializer.PRETTY_ANIMO_RESULT.serialize(op) : CachedSerializer.ANIMO_RESULT.serialize(op);
        System.out.println(result);
        Assert.assertEquals("", expected, result);

        System.out.println();
    }

    protected void assertXMLResult(final Relationship op, String expected) throws IOException {
        assertNotNull(op);

        System.out.println("XML Result serializer...");

        PipedInputStream in = new PipedInputStream(1024);
        final PipedOutputStream out = new PipedOutputStream(in);

        Runnable runner = new Runnable() {
			@Override
			public void run() {
		        try {
					CachedSerializer.XML.serialize(op, out);
			        out.close();
				} catch (IOException e) {
					Assert.fail(e.getMessage());
				}
			}
        };
        Thread th = new Thread(runner);
        th.start();
        assertEquals(in, "<?xml version='1.0' encoding='UTF-8'?>"+expected);
        System.out.println();
    }

    protected void assertStringResult(String op, String expected) throws IOException, InterruptedException {
    	assertStringResult(new AnimoExpression(op), expected);
    }

    protected void assertStringResult(Relationship op, String expected) throws IOException, InterruptedException {
        assertNotNull(op);

        System.out.println("VALUE result serializer...");
        String result = CachedSerializer.STRING.serialize(op);
        System.out.println(result);
        Assert.assertEquals("", expected, result);

        System.out.println();
    }

    protected void assertBinary(Relationship op, String expected) throws IOException {
        assertNotNull(op);
        System.out.println("Binary serializer...");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinarySerializer._.serialize(op, out);
        String bin = out.toString();
        Assert.assertEquals("", expected, bin);
        System.out.println(bin);
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
		public Enumeration<String> getAttributeNames() {
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
		public Enumeration<Locale> getLocales() {
			return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return null;
		}

		@Override
		public Enumeration<String> getParameterNames() {
//			import java.util.Collections.EmptyEnumeration;
//			return Collections.emptyEnumeration();
			return emptyEnumeration();
		}

		@Override
		public String[] getParameterValues(String name) {
			return null;
		}

		@Override
		public String getProtocol() {
			return "HTTP/1.0";
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
		public Enumeration<String> getHeaders(String name) {
			return null;
		}

		@Override
		public Enumeration<String> getHeaderNames() {
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
			return requestURI;
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

		@Override
		public int getRemotePort() {
			return 0;
		}

		@Override
		public String getLocalName() {
			return null;
		}

		@Override
		public String getLocalAddr() {
			return null;
		}

		@Override
		public int getLocalPort() {
			return 0;
		}

		@Override
		public ServletContext getServletContext() {
			return null;
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {
			return null;
		}

		@Override
		public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
			return null;
		}

		@Override
		public boolean isAsyncStarted() {
			return false;
		}

		@Override
		public boolean isAsyncSupported() {
			return false;
		}

		@Override
		public AsyncContext getAsyncContext() {
			return null;
		}

		@Override
		public DispatcherType getDispatcherType() {
			return null;
		}

		@Override
		public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
			return false;
		}

		@Override
		public void login(String username, String password) throws ServletException {}

		@Override
		public void logout() throws ServletException {}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
			return null;
		}

		@Override
		public Part getPart(String name) throws IOException, ServletException {
			return null;
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


        public byte[] getResponse() throws IOException {
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

		@Override
		public String getContentType() {
			return null;
		}

		@Override
		public void setCharacterEncoding(String charset) {}

		@Override
		public int getStatus() {
			return 0;
		}

		@Override
		public String getHeader(String name) {
			return null;
		}

		@Override
		public Collection<String> getHeaders(String name) {
			return null;
		}

		@Override
		public Collection<String> getHeaderNames() {
			return null;
		}
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

    private static class EmptyEnumeration<E> implements Enumeration<E> {
        static final EmptyEnumeration<Object> EMPTY_ENUMERATION
            = new EmptyEnumeration<>();

        public boolean hasMoreElements() { return false; }
        public E nextElement() { throw new NoSuchElementException(); }
    }

    public static <T> Enumeration<T> emptyEnumeration() {
        return (Enumeration<T>) EmptyEnumeration.EMPTY_ENUMERATION;
    }
}
