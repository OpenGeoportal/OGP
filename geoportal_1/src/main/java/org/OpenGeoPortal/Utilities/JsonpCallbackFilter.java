package org.OpenGeoPortal.Utilities;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonpCallbackFilter implements Filter {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void init(FilterConfig fConfig) throws ServletException {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

	System.out.println("In JsonpCallbackFilter");

	HttpServletRequest httpRequest = (HttpServletRequest) request;
	HttpServletResponse httpResponse = (HttpServletResponse) response;

	@SuppressWarnings("unchecked")
	Map<String, String[]> parms = httpRequest.getParameterMap();

	System.out.println("In JsonpCallbackFilter, the size of pmap is: " + parms.size());

	Set<String> keys = parms.keySet();

	Iterator it = keys.iterator();

	while(it.hasNext()) {
	    String key = (String)it.next();

	    System.out.println("Params in JsonpCallbackFilter:\n");
	    System.out.println("\tKey: " + key);
	    String[] vals = parms.get(key);
	    for(int i = 0;i < vals.length;i++) {
		System.out.println("\t\tValue: " + vals[i] + "\n");
	    }
	} 

	if(parms.containsKey("callback")) {
	    //				if(logger.isDebugEnabled())
	    //					logger.debug("Wrapping response with JSONP callback '" + parms.get("callback")[0] + "'");
	    System.out.println("Wrapping response with JSONP callback '" + parms.get("callback")[0] + "'");

	    OutputStream out = httpResponse.getOutputStream();

	    GenericResponseWrapper wrapper = new GenericResponseWrapper(httpResponse);

	    chain.doFilter(request, wrapper);

	    out.write(new String(parms.get("callback")[0] + "(").getBytes());
	    out.write(wrapper.getData());
	    out.write(new String(");").getBytes());

	    wrapper.setContentType("text/javascript;charset=UTF-8");

	    out.close();
	} else {
	    chain.doFilter(request, response);
	}
    }

    public void destroy() {}
}

