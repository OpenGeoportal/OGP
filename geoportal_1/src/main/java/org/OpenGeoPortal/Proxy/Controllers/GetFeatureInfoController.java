package org.OpenGeoPortal.Proxy.Controllers;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.OpenGeoPortal.Metadata.LayerInfoRetriever;
import org.OpenGeoPortal.Solr.SearchConfigRetriever;
import org.OpenGeoPortal.Solr.SolrRecord;
import org.OpenGeoPortal.Utilities.OgpUtils;
import org.OpenGeoPortal.Utilities.Http.HttpRequester;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/featureInfo")
public class GetFeatureInfoController {
	private static final int NUMBER_OF_FEATURES = 1;
	//private static final int BUFFER_MULTIPLIER = 5;
	private static final String RESPONSE_FORMAT = "application/vnd.ogc.gml";
	private static final String EXCEPTION_FORMAT = "application/vnd.ogc.se_xml";
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired @Qualifier("httpRequester.generic")
	private HttpRequester httpRequester;
	@Autowired
	private LayerInfoRetriever layerInfoRetriever;
	@Autowired
	private SearchConfigRetriever searchConfigRetriever;
	
	@RequestMapping(method=RequestMethod.GET)
	public void getFeatureInfo(@RequestParam("OGPID") String layerId, @RequestParam("bbox") String bbox, 
			@RequestParam("x") String xCoord,@RequestParam("y") String yCoord,
			@RequestParam("width") String width,@RequestParam("height") String height,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

	    Set<String> layerIds = new HashSet<String>();
	    layerIds.add(layerId);
	    SolrRecord layerInfo = null;
	    try {
			layerInfo = this.layerInfoRetriever.fetchAllLayerInfo(layerIds).get(0);
		} catch (Exception e) {
			e.printStackTrace();
			//response.sendError(500);
		}
	    
	    //remove any query string
	    String previewUrl = searchConfigRetriever.getWmsUrl(layerInfo);

	    /*
	     * http://www.example.com/wfs?
   service=wfs&
   version=1.1.0&
   request=GetFeature&
   typeName=layerName&
   maxFeatures=NUMBER_OF_FEATURES&srsName=EPSG:900913
   bbox=a1,b1,a2,b2
   bbox should be determined by the client.  the size of a pixel?
	     */
	    
	    String layerName = layerInfo.getName();
	    if (layerInfo.getWorkspaceName() != null && !layerInfo.getWorkspaceName().trim().isEmpty()){
	    	layerName = layerInfo.getWorkspaceName() + ":" + layerName;
	    }
	    

	    /*
	     * http://localhost:8399/arcgis/services/ihs_petroleum/MapServer/WMSServer?&service=WMS&version=1.1.1&request=GetFeatureInfo&layers=pipelines&query_layers=pipelines&styles=&bbox=47.119661,28.931116,48.593202,29.54223&srs=EPSG:4326&feature_count=10&x=389&y=120&height=445&width=1073&info_format=text/plain&xsl_template=http://server:8080/dev-summit-2010/resource/xsl/featureinfo_application_geojson.xsl
	     * 
	     */
	    
	    //in caps to support ogc services through arcgis server 9.x
	    String query = "SERVICE=WMS&VERSION=1.1.1&REQUEST=GetFeatureInfo&INFO_FORMAT=" + RESPONSE_FORMAT  
				+ "&SRS=EPSG:900913&FEATURE_COUNT=" + NUMBER_OF_FEATURES + "&STYLES=&HEIGHT=" + height + "&WIDTH=" + width +"&BBOX=" + bbox 
				+ "&X=" + xCoord + "&Y=" + yCoord +"&QUERY_LAYERS=" + layerName + "&LAYERS=" + layerName + "&EXCEPTIONS=" + EXCEPTION_FORMAT;
	   
	    String method = "GET";
		logger.info("executing WMS getFeatureInfo Request: " + previewUrl + query);

		
		if (!previewUrl.contains("http")){
			
			request.getRequestDispatcher(previewUrl + query).forward(request, response);
			
		} else {
			InputStream input = httpRequester.sendRequest(previewUrl, query, method);
			logger.info(httpRequester.getContentType());
			response.setContentType(httpRequester.getContentType());
			IOUtils.copy(input, response.getOutputStream());
		}
	}
	
	public LayerInfoRetriever getLayerInfoRetriever() {
		return layerInfoRetriever;
	}
	
	public void setLayerInfoRetriever(LayerInfoRetriever layerInfoRetriever) {
		this.layerInfoRetriever = layerInfoRetriever;
	}
}
