package org.OpenGeoPortal.Ogc.Wcs;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.OpenGeoPortal.Ogc.OgcInfoRequest;
import org.OpenGeoPortal.Utilities.OgpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WcsDescribeCoverage1_0_0 implements OgcInfoRequest {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	String layerName;
	public final String VERSION = "1.0.0";
	
	@Override
	public String createRequest(String layerName) {
		String describeCoverageRequest = "service=WCS&version=" + VERSION + "&REQUEST=DescribeCoverage&Identifiers=" + layerName;
		this.layerName = layerName;
		return describeCoverageRequest;
	}


	
	@Override
	public Map<String, String> parseResponse(InputStream inputStream) throws Exception {

		//Parse the document
		Document document = OgpUtils.getDocument(inputStream);
		//initialize return variable
		Map<String, String> describeLayerInfo = new HashMap<String, String>();

		//get the needed nodes
		Node schemaNode = document.getFirstChild();
		OgpUtils.handleServiceException(schemaNode);
		

		/*
		 * <wcs:CoverageOffering>
		 * <wcs:description>Generated from GeoTIFF</wcs:description>
		 * <wcs:name>AQUASTAT:aridity_37040</wcs:name>
		 * <wcs:label>Global map of aridity - 10 arc minutes</wcs:label>
		 * <wcs:lonLatEnvelope srsName="urn:ogc:def:crs:OGC:1.3:CRS84">
		 * <gml:pos>-180.00000000000003 -89.99999999999996</gml:pos><gml:pos>180.00072000000003 90.00036000000007</gml:pos></wcs:lonLatEnvelope>
		 * <wcs:keywords><wcs:keyword>WCS</wcs:keyword><wcs:keyword>GeoTIFF</wcs:keyword><wcs:keyword>aridity_37040</wcs:keyword></wcs:keywords>
		 * <wcs:domainSet><wcs:spatialDomain><gml:Envelope srsName="EPSG:4326"><gml:pos>-180.00000000000003 -89.99999999999996</gml:pos><gml:pos>180.00072000000003 90.00036000000007</gml:pos></gml:Envelope>
		 * 	<gml:RectifiedGrid dimension="2" srsName="EPSG:4326"><gml:limits><gml:GridEnvelope><gml:low>0 0</gml:low><gml:high>2159 1079</gml:high></gml:GridEnvelope></gml:limits><gml:axisName>x</gml:axisName><gml:axisName>y</gml:axisName>
		 * <gml:origin><gml:pos>-179.91666650000002 89.91702650000008</gml:pos></gml:origin><gml:offsetVector>0.16666700000000004 0.0</gml:offsetVector><gml:offsetVector>0.0 -0.16666700000000004</gml:offsetVector></gml:RectifiedGrid>
		 * </wcs:spatialDomain></wcs:domainSet>
		 * <wcs:rangeSet><wcs:RangeSet><wcs:name>aridity_37040</wcs:name><wcs:label>Global map of aridity - 10 arc minutes</wcs:label>
		 * <wcs:axisDescription><wcs:AxisDescription><wcs:name>Band</wcs:name><wcs:label>Band</wcs:label><wcs:values><wcs:singleValue>1</wcs:singleValue></wcs:values></wcs:AxisDescription></wcs:axisDescription></wcs:RangeSet></wcs:rangeSet>
		 * <wcs:supportedCRSs><wcs:requestResponseCRSs>EPSG:4326</wcs:requestResponseCRSs></wcs:supportedCRSs>
		 * <wcs:supportedFormats nativeFormat="GeoTIFF"><wcs:formats>GeoTIFF</wcs:formats><wcs:formats>GIF</wcs:formats><wcs:formats>JPEG</wcs:formats><wcs:formats>PNG</wcs:formats><wcs:formats>TIFF</wcs:formats></wcs:supportedFormats>
		 * <wcs:supportedInterpolations><wcs:interpolationMethod>bilinear</wcs:interpolationMethod><wcs:interpolationMethod>bicubic</wcs:interpolationMethod></wcs:supportedInterpolations></wcs:CoverageOffering>
		 * 
		 * 
		 * 
		 * 
		 */
		NodeList coverageOfferings = document.getElementsByTagName("wcs:CoverageOffering");
		Node desiredNode = null;
		if (coverageOfferings.getLength() > 1){
			for (int i = 0 ; i < coverageOfferings.getLength(); i++){
				Node currentCoverage = coverageOfferings.item(i);
				NodeList currentChildren = currentCoverage.getChildNodes();
				for (int j = 0; j < currentChildren.getLength(); j++){
					Node currentChild = currentChildren.item(j);
					if (currentChild.getNodeName().equalsIgnoreCase("wcs:name")){
						if (currentChild.getTextContent().contains(layerName)){
							desiredNode = currentChild;
							break;
						}
					}
				}
				if (desiredNode != null){
					break;
				}
			}
		} else {
			desiredNode = coverageOfferings.item(0);
		}
		
		NodeList layerDetails = desiredNode.getChildNodes();
		
		//needed to form WCS getCoverage request
		// wcs:domainSet/wcs:spatialDomain/gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:low & gml:high
		// wcs:domainSet/wcs:spatialDomain/gml:RectifiedGrid/gml:axisName (multiple)
		// wcs:supportedCRSs/wcs:requestResponseCRSs
		
		//
		try{
			NodeList supportedCRSs = document.getElementsByTagName("wcs:supportedCRS");
			//NodeList supportedCRSs = document.getElementsByTagNameNS("wcs","requestResponseCRSs");
			describeLayerInfo.put("SRS", supportedCRSs.item(0).getTextContent().trim());
		} catch (Exception e){
			//throw new Exception("error getting SRS info: "+ e.getMessage());
			logger.error("error getting SRS info: "+ e.getMessage());

		}
		try {
			NodeList gridEnvelopeLow = document.getElementsByTagName("gml:low");
			describeLayerInfo.put("gridEnvelopeLow", gridEnvelopeLow.item(0).getTextContent().trim());
			NodeList gridEnvelopeHigh = document.getElementsByTagName("gml:high");
			describeLayerInfo.put("gridEnvelopeHigh", gridEnvelopeHigh.item(0).getTextContent().trim());
		} catch (Exception e){
			//throw new Exception("error getting Grid Envelope info: "+ e.getMessage());
			logger.error("error getting Grid Envelope info: "+ e.getMessage());
		}
		try{
			NodeList axes = document.getElementsByTagName("gml:axisName");
			axes.getLength();
			for (int i = 0; i < axes.getLength(); i++){
				describeLayerInfo.put("axis" + i, axes.item(i).getTextContent().trim());
			}
			//NodeList supportedFormats = document.getElementsByTagName("wcs:supportedFormats");
			//NodeList supportedFormats = document.getElementsByTagName("wcs:supportedCRS");
			//describeLayerInfo.put("nativeFormat", supportedFormats.item(0).getTextContent().trim());
		} catch (Exception e){
			//throw new Exception("error getting Axis info: "+ e.getMessage());
			logger.error("error getting Axis info: "+ e.getMessage());
		}
		return describeLayerInfo;
	}

	@Override
	public String getMethod() {
		return "GET";
	}

	@Override
	public String getOgcProtocol() {
		return "wcs";
	}



	@Override
	public String getVersion() {
		return VERSION;
	}

}
