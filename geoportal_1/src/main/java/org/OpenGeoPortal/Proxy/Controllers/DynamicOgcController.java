package org.OpenGeoPortal.Proxy.Controllers;

/**
 * Adapted from David Smiley's HTTP reverse proxy/gateway servlet
 */
/**
* Copyright MITRE
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import org.OpenGeoPortal.Metadata.*;
import org.OpenGeoPortal.Solr.*;
import org.OpenGeoPortal.Utilities.ParseJSONSolrLocationField;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Formatter;

@Controller
@RequestMapping("/dynamic")
public class DynamicOgcController {
	  /* INIT PARAMETER NAME CONSTANTS */

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	  /* MISC */
	protected URI targetUri;
	protected HttpClient proxyClient;
	
	@Autowired
	private LayerInfoRetriever layerInfoRetriever;
	private String layerIds;





/**
* An HTTP reverse proxy/gateway servlet. It is designed to be extended for customization
* if desired. Most of the work is handled by
* <a href="http://hc.apache.org/httpcomponents-client-ga/">Apache HttpClient</a>.
* <p>
* There are alternatives to a servlet based proxy such as Apache mod_proxy if that is available to you. However
* this servlet is easily customizable by Java, secure-able by your web application's security (e.g. spring-security),
* portable across servlet engines, and is embeddable into another web application.
* </p>
* <p>
* Inspiration: http://httpd.apache.org/docs/2.0/mod/mod_proxy.html
* </p>
*
* @author David Smiley dsmiley@mitre.org>
*/


  DynamicOgcController() {
    HttpParams hcParams = new BasicHttpParams();
    hcParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
    proxyClient = createHttpClient(hcParams);
  }

  /** Called from {@link #init(javax.servlet.ServletConfig)}. HttpClient offers many opportunities for customization.
* @param hcParams*/
  @SuppressWarnings("deprecation")
protected HttpClient createHttpClient(HttpParams hcParams) {
    return new DefaultHttpClient(new ThreadSafeClientConnManager(),hcParams);
  }

  public void destroy() {
    //shutdown() must be called according to documentation.
    if (proxyClient != null)
      proxyClient.getConnectionManager().shutdown();
  }

@RequestMapping(value="/wfs", method=RequestMethod.GET, params="request=GetCapabilities")
	public void doWfsGetCapabilities(@RequestParam("typeName") String layerIds, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {

	//return getCapabilities doc use a jsp to resolve view, populate with data from describe feature type requests
  }

@RequestMapping(value="/wfs", method=RequestMethod.GET, params="request=GetFeature")
public void doWfsGetFeature(@RequestParam("typeName") String layerIds, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
	SolrRecord solrRecord = null;
	try {
		solrRecord = this.layerInfoRetriever.getAllLayerInfo(layerIds);
	} catch (Exception e) {
		e.printStackTrace();
		throw new ServletException("Unable to retrieve layer info.");
	}
	doProxy(solrRecord, servletRequest, servletResponse);		
}

@RequestMapping(value="/wfs", method=RequestMethod.GET, params="request=DescribeFeatureType")
public void doWfsDescribeFeatureType(@RequestParam("typeName") String layerIds, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
	//replace layerId with typeName, get wfs location from solr
	SolrRecord solrRecord = null; 
	try {
		solrRecord = layerInfoRetriever.getAllLayerInfo(layerIds);
	} catch (Exception e) {
		e.printStackTrace();
		throw new ServletException("Unable to retrieve layer info.");
	}
	doProxy(solrRecord, servletRequest, servletResponse);
}

@SuppressWarnings("deprecation")
  private void doProxy(SolrRecord solrRecord, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
	    // Make the Request
	    //note: we won't transfer the protocol version because I'm not sure it would truly be compatible
	try {
		this.targetUri = new URI(ParseJSONSolrLocationField.getWmsUrl(solrRecord.getLocation()));
	} catch (URISyntaxException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
	String layerName = "";
	if (solrRecord.getWorkspaceName().length() > 0){
		layerName += solrRecord.getWorkspaceName() + ":" + solrRecord.getName();
	} else {
		layerName += solrRecord.getName();
	}
	    BasicHttpEntityEnclosingRequest proxyRequest =
	        new BasicHttpEntityEnclosingRequest(servletRequest.getMethod(), rewriteUrlFromRequest(layerName, servletRequest));
	    
	    copyRequestHeaders(servletRequest, proxyRequest);

	    // Add the input entity (streamed) then execute the request.
	    HttpResponse proxyResponse = null;
	    InputStream servletRequestInputStream = servletRequest.getInputStream();
	    try {
	      try {
	        proxyRequest.setEntity(new InputStreamEntity(servletRequestInputStream, servletRequest.getContentLength()));

	        // Execute the request
	        logger.debug("proxy " + servletRequest.getMethod() + " uri: " + servletRequest.getRequestURI() + " -- " + proxyRequest.getRequestLine().getUri());
	        proxyResponse = proxyClient.execute(URIUtils.extractHost(targetUri), proxyRequest);
	      } finally {
	        closeQuietly(servletRequestInputStream);
	      }

	      // Process the response
	      int statusCode = proxyResponse.getStatusLine().getStatusCode();

	      if (doResponseRedirectOrNotModifiedLogic(servletRequest, servletResponse, proxyResponse, statusCode)) {
	        EntityUtils.consume(proxyResponse.getEntity());
	        return;
	      }

	      // Pass the response code. This method with the "reason phrase" is deprecated but it's the only way to pass the
	      // reason along too.
	      //noinspection deprecation
	      servletResponse.setStatus(statusCode, proxyResponse.getStatusLine().getReasonPhrase());

	      copyResponseHeaders(proxyResponse, servletResponse);

	      // Send the content to the client
	      copyResponseEntity(proxyResponse, servletResponse);

	    } catch (Exception e) {
	      //abort request, according to best practice with HttpClient
	      if (proxyRequest instanceof AbortableHttpRequest) {
	        AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
	        abortableHttpRequest.abort();
	      }
	      if (e instanceof RuntimeException)
	        throw (RuntimeException)e;
	      if (e instanceof ServletException)
	        throw (ServletException)e;
	      throw new RuntimeException(e);
	    }
  }
  private boolean doResponseRedirectOrNotModifiedLogic(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpResponse proxyResponse, int statusCode) throws ServletException, IOException {
    // Check if the proxy response is a redirect
    // The following code is adapted from org.tigris.noodle.filters.CheckForRedirect
    if (statusCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */
        && statusCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {
      Header locationHeader = proxyResponse.getLastHeader(HttpHeaders.LOCATION);
      if (locationHeader == null) {
        throw new ServletException("Received status code: " + statusCode
            + " but no " + HttpHeaders.LOCATION + " header was found in the response");
      }
      // Modify the redirect to go to this proxy servlet rather that the proxied host
      String locStr = rewriteUrlFromResponse(servletRequest, locationHeader.getValue());

      servletResponse.sendRedirect(locStr);
      return true;
    }
    // 304 needs special handling. See:
    // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
    // We get a 304 whenever passed an 'If-Modified-Since'
    // header and the data on disk has not changed; server
    // responds w/ a 304 saying I'm not going to send the
    // body because the file has not changed.
    if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
      servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
      servletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return true;
    }
    return false;
  }

  protected void closeQuietly(Closeable closeable) {
    try {
      closeable.close();
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
    }
  }

  /** These are the "hop-by-hop" headers that should not be copied.
* http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html
* I use an HttpClient HeaderGroup class instead of Set<String> because this
* approach does case insensitive lookup faster.
*/
  private static final HeaderGroup hopByHopHeaders;
  static {
    hopByHopHeaders = new HeaderGroup();
    String[] headers = new String[] {
        "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
        "TE", "Trailers", "Transfer-Encoding", "Upgrade" };
    for (String header : headers) {
      hopByHopHeaders.addHeader(new BasicHeader(header, null));
    }
  }

  /** Copy request headers from the servlet client to the proxy request. */
  protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
    // Get an Enumeration of all of the header names sent by the client
    Enumeration enumerationOfHeaderNames = servletRequest.getHeaderNames();
    while (enumerationOfHeaderNames.hasMoreElements()) {
      String headerName = (String) enumerationOfHeaderNames.nextElement();
      //TODO why?
      if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH))
        continue;
      if (hopByHopHeaders.containsHeader(headerName))
        continue;
      // As per the Java Servlet API 2.5 documentation:
      // Some headers, such as Accept-Language can be sent by clients
      // as several headers each with a different value rather than
      // sending the header as a comma separated list.
      // Thus, we get an Enumeration of the header values sent by the client
      Enumeration headers = servletRequest.getHeaders(headerName);
      while (headers.hasMoreElements()) {
        String headerValue = (String) headers.nextElement();
        // In case the proxy host is running multiple virtual servers,
        // rewrite the Host header to ensure that we get content from
        // the correct virtual server
        if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
          HttpHost host = URIUtils.extractHost(this.targetUri);
          headerValue = host.getHostName();
          if (host.getPort() != -1)
            headerValue += ":"+host.getPort();
        }
        proxyRequest.addHeader(headerName, headerValue);
      }
    }
  }

  /** Copy proxied response headers back to the servlet client. */
  protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletResponse servletResponse) {
    for (Header header : proxyResponse.getAllHeaders()) {
      if (hopByHopHeaders.containsHeader(header.getName()))
        continue;
      servletResponse.addHeader(header.getName(), header.getValue());
    }
  }

  /** Copy response body data (the entity) from the proxy to the servlet client. */
  private void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse) throws IOException {
    HttpEntity entity = proxyResponse.getEntity();
    if (entity != null) {
      OutputStream servletOutputStream = servletResponse.getOutputStream();
      try {
        entity.writeTo(servletOutputStream);
      } finally {
        closeQuietly(servletOutputStream);
      }
    }
  }
  
  private String rewriteUrlFromRequest(String layerName, HttpServletRequest servletRequest) {
    StringBuilder uri = new StringBuilder(500);
    uri.append(this.targetUri.toString());
    // Handle the path given to the servlet
    /*if (servletRequest.getPathInfo() != null) {//ex: /my/path.html
      uri.append(servletRequest.getPathInfo());
    }*/
    // Handle the query string
    String queryString = servletRequest.getQueryString();//ex:(following '?'): name=value&foo=bar#fragment
    queryString = queryString.replace(layerIds, layerName);
    if (queryString != null && queryString.length() > 0) {
      uri.append('?');
      int fragIdx = queryString.indexOf('#');
      String queryNoFrag = (fragIdx < 0 ? queryString : queryString.substring(0,fragIdx));
      uri.append(encodeUriQuery(queryNoFrag));
      if (fragIdx >= 0) {
        uri.append('#');
        uri.append(encodeUriQuery(queryString.substring(fragIdx + 1)));
      }
    }
    return uri.toString();
  }

  private String rewriteUrlFromResponse(HttpServletRequest servletRequest, String theUrl) {
    //TODO document example paths
    if (theUrl.startsWith(this.targetUri.toString())) {
      String curUrl = servletRequest.getRequestURL().toString();//no query
      String pathInfo = servletRequest.getPathInfo();
      if (pathInfo != null) {
        assert curUrl.endsWith(pathInfo);
        curUrl = curUrl.substring(0,curUrl.length()-pathInfo.length());//take pathInfo off
      }
      theUrl = curUrl+theUrl.substring(this.targetUri.toString().length());
    }
    return theUrl;
  }

  /**
* <p>Encodes characters in the query or fragment part of the URI.
*
* <p>Unfortunately, an incoming URI sometimes has characters disallowed by the spec. HttpClient
* insists that the outgoing proxied request has a valid URI because it uses Java's {@link URI}. To be more
* forgiving, we must escape the problematic characters. See the URI class for the spec.
*
* @param in example: name=value&foo=bar#fragment
*/
  static CharSequence encodeUriQuery(CharSequence in) {
    //Note that I can't simply use URI.java to encode because it will escape pre-existing escaped things.
    StringBuilder outBuf = null;
    Formatter formatter = null;
    for(int i = 0; i < in.length(); i++) {
      char c = in.charAt(i);
      boolean escape = true;
      if (c < 128) {
        if (asciiQueryChars.get((int)c)) {
          escape = false;
        }
      } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {//not-ascii
        escape = false;
      }
      if (!escape) {
        if (outBuf != null)
          outBuf.append(c);
      } else {
        //escape
        if (outBuf == null) {
          outBuf = new StringBuilder(in.length() + 5*3);
          outBuf.append(in,0,i);
          formatter = new Formatter(outBuf);
        }
        //leading %, 0 padded, width 2, capital hex
        formatter.format("%%%02X",(int)c);//TODO
        formatter.close();
      }
    }
    return outBuf != null ? outBuf : in;
  }


  static final BitSet asciiQueryChars;
  static {
    char[] c_unreserved = "_-!.~'()*".toCharArray();//plus alphanum
    char[] c_punct = ",;:$&+=".toCharArray();
    char[] c_reserved = "?/[]@".toCharArray();//plus punct

    asciiQueryChars = new BitSet(128);
    for(char c = 'a'; c <= 'z'; c++) asciiQueryChars.set((int)c);
    for(char c = 'A'; c <= 'Z'; c++) asciiQueryChars.set((int)c);
    for(char c = '0'; c <= '9'; c++) asciiQueryChars.set((int)c);
    for(char c : c_unreserved) asciiQueryChars.set((int)c);
    for(char c : c_punct) asciiQueryChars.set((int)c);
    for(char c : c_reserved) asciiQueryChars.set((int)c);

    asciiQueryChars.set((int)'%');//leave existing percent escapes in place
  }
  
}