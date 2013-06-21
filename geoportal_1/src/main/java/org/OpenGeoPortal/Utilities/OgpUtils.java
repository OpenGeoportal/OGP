package org.OpenGeoPortal.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OgpUtils {
	final static Logger logger = LoggerFactory.getLogger(OgpUtils.class.getName());

	public static Boolean isWellFormedEmailAddress(String emailAddress){
		//a very basic check of the email address
		emailAddress = emailAddress.trim();
		if (emailAddress.contains(" ")){
			return false;
		}
		String[] arr = emailAddress.split("@");
		if (arr.length != 2){
			return false;
		}
		if (!arr[1].contains(".")){
			return false;
		}

		return true;
	}
	
	
	public static String filterQueryString(String url){
	    if (url.contains("?")){
	    	//can happen with generic ows endpoint
	    	//get rid of everything after the query param 
	    	url = url.substring(0,url.indexOf("?"));
	    }
	    return url;
	}
	
	public static String getLayerNameNS(String workspaceName, String layerName) throws Exception{
		workspaceName = workspaceName.trim();
		layerName = layerName.trim();
		
		String embeddedWSName = "";
		if (layerName.contains(":")){
			String[] layerNameArr = layerName.split(":");
			if (layerNameArr.length > 2){
				throw new Exception("Invalid layer name ['" + layerName + "']");
			}
			embeddedWSName = layerNameArr[0];
			layerName = layerNameArr[1];
		}
		if (!workspaceName.isEmpty()){
			//prefer the explicit workspaceName?
			return workspaceName + ":" + layerName;
		} else {
			if (embeddedWSName.isEmpty()){
				return layerName;
			} else {
				return embeddedWSName + ":" + layerName;
			}
		}
	}
	
	public static String combinePathWithQuery(String path, String requestString) throws MalformedURLException{
		if (requestString.startsWith("?")){
			requestString = requestString.substring(requestString.indexOf("?"));
		}

		int count = StringUtils.countMatches(path, "?");
		if (count == 0){
			//we're good
		} else if (count == 1){

			//there are some embedded params
			String[] urlArr = path.split("\\?");
			path = urlArr[0];
			
			List<String> embeddedParams = new ArrayList<String>(Arrays.asList(urlArr[1].split("\\&")));
			List<String> queryParams = new ArrayList<String>(Arrays.asList(requestString.split("\\&")));
			List<String> duplicates = new ArrayList<String>();
			
			for (String mParam: embeddedParams){
				String mKey = mParam.split("=")[0];
				for (String qParam: queryParams){
					String qKey = qParam.split("=")[0];
					if (mKey.equalsIgnoreCase(qKey)){
						duplicates.add(mParam);
					}
				}
			}
			embeddedParams.removeAll(duplicates);
			queryParams.addAll(embeddedParams);
			requestString = StringUtils.join(queryParams, "&");
		} else if (count > 1){
			//something's really wrong here, or the path has parameters embedded in the path
			throw new MalformedURLException("This path is problematic: ['" + path + "']");
		}


		String combined = path + "?" + requestString;
		logger.info("Combined URL: " + combined);
		return combined;
	}
	
	public static Document getDocument(InputStream inputStream) throws SAXException, IOException, ParserConfigurationException{
		//parse the returned XML and return needed info as a map
		// Create a factory
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		
		documentBuilderFactory.setValidating(false);  // dtd isn't available; would be nice to attempt to validate
		documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		//documentBuilderFactory.setNamespaceAware(true);
		
		// Use document builder factory
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		//Parse the document
		Document document = builder.parse(inputStream);
		return document;
	}

	 public static void handleServiceException(Node baseNode) throws Exception{
		 /*
		  * 
		  * <ows:ExceptionReport version="1.0.0"
  xsi:schemaLocation="http://www.opengis.net/ows http://data.fao.org/maps/schemas/ows/1.0.0/owsExceptionReport.xsd"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ows="http://www.opengis.net/ows">
  <ows:Exception exceptionCode="NoApplicableCode">
    <ows:ExceptionText>java.lang.NullPointerException
null</ows:ExceptionText>
  </ows:Exception>
</ows:ExceptionReport>
		  */
			String errorMessage = "";
			
			if (baseNode.getNodeName().toLowerCase().contains("serviceexception")){
				for (int i = 0; i < baseNode.getChildNodes().getLength(); i++){
					String nodeName = baseNode.getChildNodes().item(i).getNodeName();
					if (nodeName.equals("ServiceException")){
						errorMessage += baseNode.getChildNodes().item(i).getTextContent().trim();
					} 
				}
			} else if (baseNode.getNodeName().toLowerCase().contains("exception")){
				try{
					errorMessage += baseNode.getFirstChild().getAttributes().getNamedItem("exceptionCode").getTextContent();
				} catch (Exception e){
					errorMessage += "Full response: " + baseNode.getTextContent().trim();
				}
			} else {
				return;
			}
			throw new Exception(errorMessage);
	 }
	 
	public static Map<String,String> getDesiredChildrenValues (Node parent, Set<String> childTags){
			Map<String,String> responseMap = new HashMap<String,String>();
			NodeList children = parent.getChildNodes();
			for (int i=0; i < children.getLength(); i++){
				Node child = children.item(i);
				responseMap.putAll(getSiblingValues(child, childTags));					
			}
			return responseMap;
		}
		
	public static Map<String,String> getSiblingValues(Node currentNode, Set<String> siblingTags){
			Map<String,String> responseMap = new HashMap<String,String>();
			String testString = currentNode.getNodeName().toLowerCase();
			for (String tagName: siblingTags){
				if (testString.contains(tagName.toLowerCase())){
					responseMap.put(tagName, currentNode.getTextContent().trim());
					return responseMap;
				} 
			}
			
			return responseMap;
		}
}
