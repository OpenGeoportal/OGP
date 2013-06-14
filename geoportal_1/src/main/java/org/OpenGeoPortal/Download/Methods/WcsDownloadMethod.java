package org.OpenGeoPortal.Download.Methods;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.OpenGeoPortal.Download.Types.BoundingBox;
import org.OpenGeoPortal.Download.Types.LayerRequest;
import org.OpenGeoPortal.Ogc.OgcInfoRequest;
import org.OpenGeoPortal.Solr.SolrRecord;
import org.OpenGeoPortal.Utilities.OgpUtils;
import org.codehaus.jackson.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class WcsDownloadMethod extends AbstractDownloadMethod implements PerLayerDownloadMethod {	
	private static final Boolean INCLUDES_METADATA = false;
	private static final String METHOD = "POST";

	@Autowired
	@Qualifier("ogcInfoRequest.wcs_1_0_0")
	private OgcInfoRequest ogcInfoRequest;
	@Override
	public String getMethod(){
		return METHOD;
	}
	
	@Override
	public Set<String> getExpectedContentType(){
		Set<String> expectedContentType = new HashSet<String>();
		expectedContentType.add("application/zip");
		expectedContentType.add("image/tiff");
		expectedContentType.add("image/tiff; subtype=\"geotiff\"");
		return expectedContentType;
	}
	
	public String createDownloadRequest() throws Exception {
		//--generate POST message
		//info needed: geometry column, bbox coords, epsg code, workspace & layername
	 	//all client bboxes should be passed as lat-lon coords.  we will need to get the appropriate epsg code for the layer
	 	//in order to return the file in original projection to the user (will also need to transform the bbox)
    	//all client bboxes should be passed as lat-lon coords.  we will need to get the appropriate epsg code for the layer
		//in order to return the file in original projection to the user 

		SolrRecord layerInfo = this.currentLayer.getLayerInfo();
		BoundingBox nativeBounds = new BoundingBox(layerInfo.getMinX(), layerInfo.getMinY(), layerInfo.getMaxX(), layerInfo.getMaxY());
		BoundingBox bounds = nativeBounds.getIntersection(this.currentLayer.getRequestedBounds());
		String layerName = this.currentLayer.getLayerNameNS();

		Map<String, String> describeLayerInfo = getWcsDescribeLayerInfo();
		String epsgCode = describeLayerInfo.get("SRS");
		String domainSubset = "";

		//wcs requires this info, even for full extent
			String gmlLow = describeLayerInfo.get("gridEnvelopeLow");
			String gmlHigh = describeLayerInfo.get("gridEnvelopeHigh");
			String axes = "";
			if (describeLayerInfo.containsKey("axis1")){
				axes += "<gml:axisName>";
				axes += describeLayerInfo.get("axis1");
				axes += "</gml:axisName>";
				if (describeLayerInfo.containsKey("axis2")){
					axes += "<gml:axisName>";
					axes += describeLayerInfo.get("axis2");
					axes += "</gml:axisName>";
				}
			}
			domainSubset = "<domainSubset>"
				+				"<spatialSubset>"
				+					bounds.generateGMLEnvelope(4326)
				+					"<gml:Grid dimension=\"2\">"
				+						"<gml:limits>"
				+							"<gml:GridEnvelope>"
				+								"<gml:low>" + gmlLow + "</gml:low>"
				+                				"<gml:high>" + gmlHigh + "</gml:high>"
				+							"</gml:GridEnvelope>"
				+						"</gml:limits>"
				+						axes
				+					"</gml:Grid>"
				+				"</spatialSubset>"
				+			"</domainSubset>";
		
		String format = "GeoTIFF";

		String getCoverageRequest = "<GetCoverage service=\"WCS\" version=\"1.0.0\" "
			+  "xmlns=\"http://www.opengis.net/wcs\" "
	  		+  "xmlns:ogc=\"http://www.opengis.net/ogc\" "
	  		+  "xmlns:gml=\"http://www.opengis.net/gml\" " 
	  		+  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " 
	  		+  "xsi:schemaLocation=\"http://www.opengis.net/wcs http://schemas.opengis.net/wcs/1.0.0/getCoverage.xsd\">"
			+		"<sourceCoverage>" + layerName + "</sourceCoverage>"
			+ 		domainSubset
			+		"<output>"
		    +			"<crs>" + epsgCode + "</crs>"
			+			"<format>" + format + "</format>"
			+		"</output>"
			+	"</GetCoverage>";
			
		return getCoverageRequest;	 
	}
	
	@Override
	public List<String> getUrls(LayerRequest layer) throws MalformedURLException, JsonParseException{
		String url = layer.getWcsUrl();
		this.checkUrl(url);
		return urlToUrls(url);
	}
	
	 Map<String, String> getWcsDescribeLayerInfo()
	 	throws Exception {

			String layerName = this.currentLayer.getLayerNameNS();
			
			InputStream inputStream = this.httpRequester.sendRequest(OgpUtils.filterQueryString(this.getUrl(this.currentLayer)), ogcInfoRequest.createRequest(layerName), ogcInfoRequest.getMethod());
			//parse the returned XML and return needed info as a map
			return ogcInfoRequest.parseResponse(inputStream);
	 }

	 
		@Override
		public Boolean includesMetadata() {
			return INCLUDES_METADATA;
		}


}
