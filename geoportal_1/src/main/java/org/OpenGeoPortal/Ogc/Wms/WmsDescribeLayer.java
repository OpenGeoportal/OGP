package org.OpenGeoPortal.Ogc.Wms;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.OpenGeoPortal.Ogc.OgcInfoRequest;
import org.OpenGeoPortal.Utilities.OgpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class WmsDescribeLayer implements OgcInfoRequest {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	public final String VERSION = "1.1.1";
	
	@Override
	public String createRequest(String layerName){
		/*
		 * http://localhost:8080/geoserver/topp/wms?service=WMS
&version=1.1.1
&request=DescribeLayer
&layers=topp:coverage
		 */
	 	String describeFeatureRequest = "service=WMS&version=" + VERSION + "&request=DescribeLayer&layers=" + layerName;
	 	return describeFeatureRequest;
	}
	
	@Override
	public Map<String, String> parseResponse(InputStream inputStream) throws Exception {

		/*
		 * <?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE WMS_DescribeLayerResponse SYSTEM "http://localhost:8080/geoserver/schemas/wms/1.1.1/WMS_DescribeLayerResponse.dtd">
<WMS_DescribeLayerResponse version="1.1.1">
   <LayerDescription name="topp:coverage" owsURL="http://localhost:8080/geoserver/topp/wcs?" owsType="WCS">
      <Query typeName="topp:coverage"/>
   </LayerDescription>
</WMS_DescribeLayerResponse>
		 * 
		 * 
		 * 
		 */
		//Parse the document
		Document document = OgpUtils.getDocument(inputStream);
			//initialize return variable
			Map<String, String> describeLayerInfo = new HashMap<String, String>();

			//get the namespace info
			Node schemaNode = document.getFirstChild();
			OgpUtils.handleServiceException(schemaNode);
			
			try {
				Node layerDescription = document.getElementsByTagName("LayerDescription").item(0);
				
				NamedNodeMap attributes = layerDescription.getAttributes();
				String name = attributes.getNamedItem("name").getTextContent();
				logger.info(name);
				describeLayerInfo.put("qualifiedName", name);
				
				String url = attributes.getNamedItem("owsURL").getTextContent();
				describeLayerInfo.put("owsUrl", url);
				logger.info(url);
				
				String type = attributes.getNamedItem("owsType").getTextContent();
				describeLayerInfo.put("owsType", type);
				logger.info(type);
				
			} catch (Exception e){
				logger.error(document.getFirstChild().getTextContent());
				throw new Exception("Error getting layer info from DescribeLayer: "+ e.getMessage());
			}
			
			return describeLayerInfo;
		 }
	 

	@Override
	public String getMethod() {
		return "GET";
	}

	@Override
	public String getOgcProtocol() {
		return "wms";
	}

	@Override
	public String getVersion() {
		return VERSION;
	}
}
