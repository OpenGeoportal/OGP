package org.OpenGeoPortal.Ogc.Wfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.OpenGeoPortal.Download.Types.BoundingBox;

public class WfsGetFeature {

    	final static Logger logger = LoggerFactory.getLogger(WfsGetFeature.class);



	public static String createWfsGetFeatureRequest(String layerName, String workSpace, String nameSpace, String outputFormat, String filter) throws Exception {
		return createWfsGetFeatureRequest(layerName, workSpace, nameSpace, -1, "", outputFormat, filter);
		 
	}
	
	private static String getAttributeString(String attrName, int value){
		String attrString = "";
		if(value > 0){
			attrString = " " + attrName + "=\"" + Integer.toString(value) + "\""; 
		}
		return attrString;
	}
	
	private static String getAttributeString(String attrName, String value){
		String attrString = "";
		if(!value.trim().isEmpty()){
			attrString = " " + attrName + "=\"" + value.trim() + "\""; 
		}
		return attrString;
	}
	
	public static String createWfsGetFeatureRequest(String layerName, String workSpace, String nameSpace, int maxFeatures, String epsgCode, String outputFormat, String filter) throws Exception {


	    logger.info("In createWfsGetFeatureRequest passed in, layerName: " + layerName + ", workSpace: " + workSpace);


	    //--generate POST message
	    //info needed: geometry column, bbox coords, epsg code, workspace & layername
		
	    if (!workSpace.trim().isEmpty()){
		if (layerName.contains(":")){
		    layerName = layerName.substring(layerName.indexOf(":") + 1);
		}
		layerName = workSpace + ":" + layerName;
	    } else {
	    }


	    /* old code
		if (!workSpace.trim().isEmpty()){
			layerName = workSpace + ":" + layerName;
		} else {
			if (layerName.contains(":")){
				layerName = layerName.substring(layerName.indexOf(":"));
			}
		}
	    */
		logger.info("In createWfsGetFeatureRequest converted, layerName: " + layerName); 

		String getFeatureRequest = "<wfs:GetFeature service=\"WFS\" version=\"1.0.0\""
			+ " outputFormat=\"" + outputFormat + "\""
			+ getAttributeString("maxfeatures", maxFeatures)
			+ getAttributeString("srsName", epsgCode)	
       			+ getNameSpaceString(workSpace, nameSpace)
  			+ " xmlns:wfs=\"http://www.opengis.net/wfs\""
  			+ " xmlns:ogc=\"http://www.opengis.net/ogc\""
  			+ " xmlns:gml=\"http://www.opengis.net/gml\""
  			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
  			+ " xsi:schemaLocation=\"http://www.opengis.net/wfs"
            + " http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd\">"
  			+ "<wfs:Query typeName=\"" + layerName + "\">"
  			+ filter
  			+ "</wfs:Query>"
			+ "</wfs:GetFeature>";

		logger.info("Feature Request: " + getFeatureRequest);


    	return getFeatureRequest;
	}
	
	public static String getMethod(){
		return "POST";
	}
	
	private static String getNameSpaceString(String workSpace, String nameSpace){
		//if either is missing, skip the whole thing
		String nsString = "";
		if (!workSpace.trim().isEmpty() && !nameSpace.trim().isEmpty()){
			nsString = " xmlns:" + workSpace + "=\"" + nameSpace + "\"";
		}
		return nsString;
	}
	
	public static String getBboxFilter(BoundingBox bounds, String geometryColumn, int epsgCode){

		String bboxFilter = "<ogc:Filter>"
  		+		"<ogc:BBOX>"
    	+			"<ogc:PropertyName>" + geometryColumn + "</ogc:PropertyName>"
    	+			bounds.generateGMLBox(epsgCode)
    	+		"</ogc:BBOX>"
  		+	"</ogc:Filter>";
			
		return bboxFilter;
	}
	

}
