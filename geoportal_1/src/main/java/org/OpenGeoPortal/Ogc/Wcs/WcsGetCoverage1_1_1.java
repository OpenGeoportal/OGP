package org.OpenGeoPortal.Ogc.Wcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.OpenGeoPortal.Download.Types.BoundingBox;

public class WcsGetCoverage1_1_1 {
	
	public static String getMethod(){
		return "POST";
	}
	
	public static String createWfsGetFeatureRequest(String layerName, Map<String,String> describeCoverageMap, BoundingBox bounds, int epsgCode, String outputFormat) throws Exception {
		
		//--generate POST message

		String getCoverageRequest = "<GetCoverage version=\"1.1.1\" service=\"WCS\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xmlns=\"http://www.opengis.net/wcs/1.1.1\" " +
				"xmlns:ows=\"http://www.opengis.net/ows/1.1\" " +
				"xmlns:gml=\"http://www.opengis.net/gml\" " +
				"xmlns:ogc=\"http://www.opengis.net/ogc\" " +
				"xsi:schemaLocation=\"http://www.opengis.net/wcs/1.1.1 http://schemas.opengis.net/wcs/1.1.1/wcsAll.xsd\">" +
				"<ows:Identifier>" + layerName + "</ows:Identifier>" +
				"<DomainSubset>" +
				bounds.generateOWSBoundingBox(epsgCode) +
				"</DomainSubset>" +
				"<Output store=\"true\" format=\"" + outputFormat + "\">" +
				generateGridCRS(describeCoverageMap) +
				"</Output>" +
			"</GetCoverage>";
		 

    	return getCoverageRequest;
	}
	
	
	
	private static String generateGridCRS(Map<String,String> describeCoverageMap){
		//should check which are required elements
		List<String> gridtagList = new ArrayList<String>();
		gridtagList.add("GridBaseCRS");
		gridtagList.add("GridType");
		gridtagList.add("GridOffsets");
		gridtagList.add("GridCS");

		String gridCRS = "<GridCRS>";
		for (String gridtag: gridtagList){
			for (String key :describeCoverageMap.keySet()){
				if (key.toLowerCase().contains(gridtag.toLowerCase())){
					gridCRS += "<" + gridtag + ">";
					gridCRS += describeCoverageMap.get(key).trim();
					gridCRS += "</" + gridtag + ">";
				}
			}
		}

		gridCRS += "</GridCRS>";
		
		return gridCRS;
	}

}
