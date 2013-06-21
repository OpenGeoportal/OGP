package org.OpenGeoPortal.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ParseJSONSolrLocationField {
	final static Logger logger = LoggerFactory.getLogger(ParseJSONSolrLocationField.class.getName());

	public static String getWmsUrl(String locationField) throws JsonParseException{
		return parseLocationFromPath(locationField, "wms").get(0);

	}
	
	public static String getTilecacheUrl(String locationField) throws JsonParseException{
		return parseLocationFromPath(locationField, "tilecache").get(0);

	}
	
	public static String getWfsUrl(String locationField) throws JsonParseException{
		return parseLocationFromPath(locationField, "wfs").get(0);

	}
	
	private static List<String> parseLocationFromPath(String locationField, String path) throws JsonParseException{
		JsonNode rootNode = parseLocationField(locationField);
		JsonNode pathNode = rootNode.path(path);
		Set<String> url = new HashSet<String>();
		if (pathNode.isMissingNode()){
			
			throw new JsonParseException("The Object '" + path + "' could not be found.", null);
			
		} else if (pathNode.isArray()){
			
			ArrayNode urls = (ArrayNode) rootNode.path(path);
			for(JsonNode currentUrl: urls){
				url.add(currentUrl.getTextValue());
			}
			
		} else if (pathNode.isTextual()){
			url.add(pathNode.getTextValue());
		}

		if (url == null || url.isEmpty()){
			
			throw new JsonParseException("The Object '" + path + "' is empty.", null);

		}
		List<String> urlList = new ArrayList<String>();
		urlList.addAll(url);
		return urlList;
	}
	
	public static String getWcsUrl(String locationField) throws JsonParseException{

		return parseLocationFromPath(locationField, "wcs").get(0);
	}
	
	public static String getServiceStartUrl(String locationField) throws JsonParseException{
		return parseLocationFromPath(locationField, "serviceStart").get(0);

	}
	
	public static List<String> getDownloadUrl(String locationField) throws JsonParseException{
			return parseLocationFromPath(locationField, "fileDownload");

	}
	
	private static JsonNode parseLocationField(String locationField){
		locationField.replaceAll("(?i)\"wms\"", "\"wms\"");
		locationField.replaceAll("(?i)\"wcs\"", "\"wcs\"");
		locationField.replaceAll("(?i)\"wfs\"", "\"wfs\"");
		locationField.replaceAll("(?i)\"serviceStart\"", "\"serviceStart\"");
		locationField.replaceAll("(?i)\"download\"", "\"fileDownload\"");
		locationField.replaceAll("(?i)\"fileDownload\"", "\"fileDownload\"");
		locationField.replaceAll("(?i)\"tilecache\"", "\"tilecache\"");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readTree(locationField);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rootNode;
		
	}
}
