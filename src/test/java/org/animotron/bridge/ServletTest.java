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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.animotron.ATest;
import org.apache.log4j.helpers.NullEnumeration;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.animotron.expression.JExpression.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ServletTest extends ATest {

    @Test
    public void test() throws Exception {

    	FSBridge.load("src/main/animo/");
    	
    	AnimoServlet servlet = new AnimoServlet();
    	
    	HttpRequest request = new HttpRequest("/","localhost");
    	HttpResponse response = new HttpResponse();
    	
//    	Expression request = servlet.new AnimoRequest();
//    	String mime = StringResultSerializer.serialize(AnimoServlet.WebSerializer.get(request, AnimoServlet.MIME));
//    	assertTrue("".equals(mime));
    	
    	servlet.doGet(request, response);

//        assertAnimoResult(s,
//            "have content " +
//                "\\html " +
//                    "(\\head " +
//                    	"(\\title have title \"Welcome to Animo\") " +
//                    	"(\\meta (@name \"keywords\") (@content \"get keywords\")) " +
//                    	"(\\meta (@name \"description\") (@content \"get description\"))) " +
//                    "(\\body the theme-concrete-root-layout (is root-layout) " +
//                        "(\\h1 have title \"Welcome to Animo\") " +
//                        "(\\p have content \"It is working!\") " +
//                        "(\\ul " +
//                            "(\\li (\"Host: \") (\\strong have host \"localhost\")) " +
//                            "(\\li (\"URI: \") (\\strong have uri \"/\"))))");
//
//
        Assert.assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<html>" +
                "<head>" +
                    "<title>Welcome to Animo</title>" +
                    "<meta name=\"keywords\" content=\"get keywords\"/>" +
                    "<meta name=\"description\" content=\"get description\"/>" +
                "</head>" +
                "<body>" +
                    "<h1>Welcome to Animo</h1>" +
                    "<p>It is working!</p>" +
                    "<ul>" +
                        "<li>Host: <strong>localhost</strong></li>" +
                        "<li>URI: <strong>/</strong></li>" +
                    "</ul>" +
                "</body>" +
            "</html>",
            response.getResponse()
        );
        
    	request = new HttpRequest("/favicon.ico","localhost");
    	response = new HttpResponse();

    	servlet.doGet(request, response);
    	
//        assertAnimoResult(ss, "");
    }
    
    private class HttpRequest implements HttpServletRequest {
    	
    	String requestURI;
    	String serverName;
    	
    	public HttpRequest(String requestURI, String serverName) {
    		this.requestURI = requestURI;
    		this.serverName = serverName;
		}

		@Override
		public Object getAttribute(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getAttributeNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getCharacterEncoding() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getContentLength() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getContentType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getParameter(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getLocales() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map getParameterMap() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getParameterNames() {
			return NullEnumeration.getInstance();
		}

		@Override
		public String[] getParameterValues(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getProtocol() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getScheme() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServerName() {
			return serverName;
		}

		@Override
		public int getServerPort() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRemoteAddr() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRemoteHost() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setAttribute(String name, Object o) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeAttribute(String name) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isSecure() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRealPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setCharacterEncoding(String arg0)
				throws UnsupportedEncodingException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getAuthType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Cookie[] getCookies() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getDateHeader(String name) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getHeader(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getHeaders(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getHeaderNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getIntHeader(String name) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getMethod() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getPathInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getPathTranslated() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getContextPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getQueryString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRemoteUser() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isUserInRole(String role) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Principal getUserPrincipal() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRequestURI() {
			return requestURI;
		}

		@Override
		public StringBuffer getRequestURL() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRequestedSessionId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServletPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HttpSession getSession(boolean create) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HttpSession getSession() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			// TODO Auto-generated method stub
			return false;
		}
    	
    }
    
    private class HttpResponse implements HttpServletResponse {
    	
        OutputStream out;

        public HttpResponse() throws IOException {
            out = new OutputStream();
		}
        
        protected String getResponse() {
        	return out.builder.toString();
        }

		@Override
		public String getCharacterEncoding() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			return out;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getBufferSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void flushBuffer() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isCommitted() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void resetBuffer() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setContentLength(int len) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setContentType(String type) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setBufferSize(int size) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setLocale(Locale loc) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addCookie(Cookie cookie) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean containsHeader(String name) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String encodeURL(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeRedirectURL(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeUrl(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeRedirectUrl(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void sendError(int sc, String msg) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendError(int sc) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendRedirect(String location) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setDateHeader(String name, long date) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addDateHeader(String name, long date) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setHeader(String name, String value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addHeader(String name, String value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setIntHeader(String name, int value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addIntHeader(String name, int value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setStatus(int sc) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setStatus(int sc, String sm) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    private class OutputStream extends ServletOutputStream {
    	
    	StringBuilder builder = new StringBuilder(1024); 

		@Override
		public void write(int b) throws IOException {
			builder.append((char)b);
		}
    }
}